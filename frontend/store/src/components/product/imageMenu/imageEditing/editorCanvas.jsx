import {forwardRef, useCallback, useEffect, useImperativeHandle, useLayoutEffect, useRef} from "react";
import styles from "./editorCanvas.module.css";

function clamp(v, min, max) {
    return Math.min(max, Math.max(min, v));
}

const EditorCanvas = forwardRef(({backgroundColor = "#FFFFFF", bitmap, maxScale = 10}, ref) => {
        const canvasRef = useRef(null);
        const wrapRef = useRef(null);
        const ctxRef = useRef(null);

        const sizeRef = useRef({w: 0, h: 0}); // CSS pixels

        const scaleRef = useRef(1);
        const offsetNormRef = useRef({x: 0, y: 0}); // normalized -1 to 1

        const draw = useCallback(() => {
            const ctx = ctxRef.current;
            const {w, h} = sizeRef.current;
            if (!ctx || w === 0 || h === 0) return;

            ctx.clearRect(0, 0, w, h);
            ctx.fillStyle = backgroundColor;
            ctx.fillRect(0, 0, w, h);

            if (!bitmap) return;

            const userScale = scaleRef.current;

            const baseScale = Math.min(w / bitmap.width, h / bitmap.height);
            const dw = bitmap.width * baseScale * userScale;
            const dh = bitmap.height * baseScale * userScale;

            const baseX = (w - dw) / 2;
            const baseY = (h - dh) / 2;

            // convert normalized offset -> pixels
            let ox = offsetNormRef.current.x * w;
            let oy = offsetNormRef.current.y * h;

            // clamp in pixels
            ox = dw <= w ? 0 : clamp(ox, baseX, -baseX);
            oy = dh <= h ? 0 : clamp(oy, baseY, -baseY);

            // write back clamped normalized offset
            offsetNormRef.current = {x: ox / w, y: oy / h};

            const dx = baseX + ox;
            const dy = baseY + oy;

            ctx.drawImage(bitmap, dx, dy, dw, dh);
        }, [backgroundColor, bitmap]);


        const setScale = useCallback((next) => {
            const v = clamp(Number(next) || 1, 1, maxScale);
            scaleRef.current = v;
            draw();
            return v;
        }, [draw]);


        const panBy = useCallback((dx, dy) => {
            const {w, h} = sizeRef.current;
            if (!w || !h) return;

            const o = offsetNormRef.current;
            offsetNormRef.current = {x: o.x + dx / w, y: o.y + dy / h};
            draw();
        }, [draw]);


        const zoomBy = useCallback((delta) => {
            const next = scaleRef.current + delta;
            return setScale(next);
        }, [setScale]);

        const reset = useCallback(() => {
            scaleRef.current = 1;
            offsetNormRef.current = {x: 0, y: 0};
            draw();
        }, [draw]);


        const exportBlob = useCallback(async (type = "image/webp", quality = 0.9) => {
            if (!bitmap) return null;

            // create a square canvas with size = max dimension of the image
            const S = Math.max(bitmap.width, bitmap.height);

            const exportCanvas = document.createElement("canvas");
            exportCanvas.width = S;
            exportCanvas.height = S;

            const ctx = exportCanvas.getContext("2d");

            // background
            ctx.fillStyle = backgroundColor;
            ctx.fillRect(0, 0, S, S);

            const userScale = scaleRef.current;

            const baseScale = Math.min(S / bitmap.width, S / bitmap.height);
            const dw = bitmap.width * baseScale * userScale;
            const dh = bitmap.height * baseScale * userScale;

            const baseX = (S - dw) / 2;
            const baseY = (S - dh) / 2;

            const ox = offsetNormRef.current.x * S;
            const oy = offsetNormRef.current.y * S;

            const dx = baseX + ox;
            const dy = baseY + oy;

            ctx.drawImage(bitmap, dx, dy, dw, dh);

            return await new Promise((resolve) => {
                exportCanvas.toBlob((blob) => resolve(blob), type, quality);
            });
        }, [bitmap, backgroundColor]);


        useImperativeHandle(
            ref,
            () => ({
                // draw/export
                draw,
                exportBlob,

                // transforms
                setScale,
                zoomBy,
                getScale: () => scaleRef.current,

                panBy,
                reset
            }),
            [draw, exportBlob, setScale, zoomBy, panBy, reset]
        );


        useLayoutEffect(() => {
            const canvas = canvasRef.current;
            const wrap = wrapRef.current;
            if (!canvas || !wrap) return;

            const ctx = canvas.getContext("2d");
            ctxRef.current = ctx;

            const resizeObserver = new ResizeObserver(() => {
                const {width, height} = wrap.getBoundingClientRect();
                const dpr = window.devicePixelRatio || 1;

                canvas.style.width = `${width}px`;
                canvas.style.height = `${height}px`;
                canvas.width = Math.max(1, Math.round(width * dpr));
                canvas.height = Math.max(1, Math.round(height * dpr));

                ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

                sizeRef.current = {w: width, h: height};
                draw();
            })
            resizeObserver.observe(wrap)

            return () => resizeObserver.disconnect();
        }, [draw])

        useEffect(() => {
            offsetNormRef.current = {x: 0, y: 0};
            draw();
        }, [bitmap, backgroundColor, draw]);

        return (
            <div ref={wrapRef} className={styles.container}>
                <canvas ref={canvasRef}/>
            </div>
        );
    }
);

export default EditorCanvas;