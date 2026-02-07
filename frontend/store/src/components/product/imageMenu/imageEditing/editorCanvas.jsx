import {useCallback, useEffect, useLayoutEffect, useRef} from "react";
import styles from "./editorCanvas.module.css";


function EditorCanvas({backgroundColor="#FFFFFF", bitmap, scale = 1}) {
    const canvasRef = useRef(null);
    const wrapRef = useRef(null);
    const ctxRef = useRef(null);
    const sizeRef = useRef({ w: 0, h: 0 });

    const draw = useCallback(() => {
        const ctx = ctxRef.current;
        const { w, h } = sizeRef.current;
        if (!ctx || w === 0 || h === 0) return;

        ctx.clearRect(0, 0, w, h); // clear before redraw

        ctx.fillStyle = backgroundColor; // fill background
        ctx.fillRect(0, 0, w, h);

        if (!bitmap) return;

        const baseScale = Math.min(w / bitmap.width, h / bitmap.height);
        const dw = bitmap.width * baseScale * scale;
        const dh = bitmap.height * baseScale * scale;
        const dx = (w - dw) / 2;
        const dy = (h - dh) / 2;

        ctx.drawImage(bitmap, dx, dy, dw, dh);

    }, [backgroundColor, bitmap, scale]);


    useLayoutEffect(() => {
        const canvas = canvasRef.current;
        const wrap = wrapRef.current;
        const ctx = canvas.getContext("2d");
        ctxRef.current = ctx;

        const resizeObserver = new ResizeObserver(() => {
            const { width, height } = wrap.getBoundingClientRect();
            const dpr = window.devicePixelRatio || 1;

            canvas.style.width = `${width}px`;
            canvas.style.height = `${height}px`;
            canvas.width = Math.max(1, Math.round(width * dpr));
            canvas.height = Math.max(1, Math.round(height * dpr));

            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

            sizeRef.current = { w: width, h: height };
            draw();
        })
        resizeObserver.observe(wrap)

        return () => resizeObserver.disconnect();
    }, [draw])

    useEffect(() => {
        draw();
    }, [draw]);

    return (
        <div ref={wrapRef} className={styles.container}>
            <canvas ref={canvasRef}/>
        </div>
    )

}

export default EditorCanvas;