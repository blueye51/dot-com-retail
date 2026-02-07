import styles from './imageEditing.module.css';
import {useCallback, useEffect, useRef, useState} from "react";
import EditorCanvas from "./editorCanvas.jsx";

function ImageEditing({file, onDone, outputType = "image/webp", quality = 0.9}) {
    const wrapRef = useRef(null);
    const [bitmap, setBitmap] = useState(null);

    const [backgroundColor, setBackgroundColor] = useState("#FFFFFF");
    const [scale, setScale] = useState(1);

    const isDraggingRef = useRef(false);

    const dragStartRef = useRef({x: 0, y: 0});
    const offsetRef = useRef({x: 0, y: 0});

    useEffect(() => {
        const el = wrapRef.current;
        if (!el) return;

        const onWheel = (e) => {
            e.preventDefault();
            setScale((prev) => {
                const delta = -e.deltaY * 0.002;
                return Math.min(10, Math.max(1, prev + delta));
            });
        };

        const onPointerDown = (e) => {
            el.setPointerCapture(e.pointerId);
            dragStartRef.current = { x: e.clientX, y: e.clientY };
            isDraggingRef.current = true;
        };

        const onPointerUp = (e) => {
            isDraggingRef.current = false;
            try { el.releasePointerCapture(e.pointerId); } catch {}
        };

        const onPointerMove = (e) => {
            if (!isDraggingRef.current) return;
            const dx = e.clientX - dragStartRef.current.x;
            const dy = e.clientY - dragStartRef.current.y;
            dragStartRef.current = { x: e.clientX, y: e.clientY };
            offsetRef.current = {
                x: offsetRef.current.x + dx,
                y: offsetRef.current.y + dy,
            };
        };

        el.addEventListener("wheel", onWheel, { passive: false });
        el.addEventListener("pointerdown", onPointerDown);
        el.addEventListener("pointermove", onPointerMove);
        el.addEventListener("pointerup", onPointerUp);
        el.addEventListener("pointercancel", onPointerUp);

        return () => {
            el.removeEventListener("wheel", onWheel);
            el.removeEventListener("pointerdown", onPointerDown);
            el.removeEventListener("pointermove", onPointerMove);
            el.removeEventListener("pointerup", onPointerUp);
            el.removeEventListener("pointercancel", onPointerUp);
        };
    }, []);

    useEffect(() => {
        if (!file) return;
        let bmp;
        createImageBitmap(file)
            .then((b) => {
                bmp = b;
                setBitmap(b);
            })
            .catch((err) => {
                alert("Failed to load image");
                console.error(err);
            });

        return () => {
            bmp?.close?.();
        };
    }, [file]);


    return (
        <div className={styles.main}>
            <div ref={wrapRef} className={styles.canvasWrapper}>
                <EditorCanvas background={backgroundColor} bitmap={bitmap} scale={scale}/>
            </div>
            <label htmlFor="scale">Image scale:</label>
            <input
                id="scale"
                type="range"
                min="1"
                max="10"
                step="0.1"
                value={scale}
                onChange={e => setScale(Number(e.target.value))}
            />
            <label htmlFor="color">Background color:</label>
            <input id="color" type="text" value={backgroundColor}/>
            <button>Submit</button>
        </div>
    );
}

export default ImageEditing;