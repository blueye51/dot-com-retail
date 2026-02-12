import styles from './imageEditing.module.css';
import {useCallback, useEffect, useRef, useState} from "react";
import EditorCanvas from "./editorCanvas.jsx";

function ImageEditing({file, onDone, outputType = "image/webp", quality = 0.9, loading = false}) {
    const wrapRef = useRef(null);
    const editorRef = useRef(null);

    const [bitmap, setBitmap] = useState(null);
    const [backgroundColor, setBackgroundColor] = useState("#FFFFFF");

    const [scaleUi, setScaleUi] = useState(1);
    const [submitting, setSubmitting] = useState(false);

    const isDraggingRef = useRef(false);
    const dragStartRef = useRef({x: 0, y: 0});

    useEffect(() => {
        const el = wrapRef.current;
        if (!el) return;

        const onWheel = (e) => {
            e.preventDefault();
            editorRef.current?.zoomBy(-e.deltaY * 0.002);

            const s = editorRef.current?.getScale?.();
            if (typeof s === "number") setScaleUi(s);
        };

        const onPointerDown = (e) => {
            el.setPointerCapture(e.pointerId);
            dragStartRef.current = {x: e.clientX, y: e.clientY};
            isDraggingRef.current = true;
        };

        const onPointerUp = (e) => {
            isDraggingRef.current = false;
            try {
                el.releasePointerCapture(e.pointerId);
            } catch {
            }
        };

        const onPointerMove = (e) => {
            if (!isDraggingRef.current) return;
            const dx = e.clientX - dragStartRef.current.x;
            const dy = e.clientY - dragStartRef.current.y;
            dragStartRef.current = {x: e.clientX, y: e.clientY};
            editorRef.current?.panBy(dx, dy);
        };

        el.addEventListener("wheel", onWheel, {passive: false});
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

    const handleSubmit = useCallback(async () => {
        if (loading || submitting) return;
        setSubmitting(true);
        try {
            const blob = await editorRef.current?.exportBlob?.(outputType, quality);
            if (!blob) return;
            onDone?.(blob);
        } finally {
            setSubmitting(false);
        }
    }, [loading, submitting, onDone, outputType, quality]);


    return (
        <div className={styles.main}>
            <div ref={wrapRef} className={styles.canvasWrapper}>
                <EditorCanvas
                    ref={editorRef}
                    backgroundColor={backgroundColor}
                    bitmap={bitmap}
                />
            </div>

            <label htmlFor="scale">Image scale:</label>
            <input
                id="scale"
                type="range"
                min="1"
                max="10"
                step="0.1"
                value={scaleUi}
                onChange={(e) => {
                    const v = Number(e.target.value);
                    setScaleUi(v);
                    editorRef.current?.setScale(v);
                }}
            />

            <label htmlFor="color" style={{fontSize: "0.7rem"}}>Background color picker</label>
            <input
                id="color"
                type="color"
                value={backgroundColor}
                onChange={e => setBackgroundColor(e.target.value)}
            />
            <button
                onClick={() => {
                    editorRef.current?.reset();
                    setScaleUi(1);
                }}
            >
                Reset
            </button>
            <button
                disabled={loading || submitting}
                onClick={handleSubmit}
            >
                {submitting || loading ? "Uploading..." : "Submit"}
            </button>
        </div>
    );
}

export default ImageEditing;