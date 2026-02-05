import styles from './imageEditing.module.css';
import {useCallback, useEffect, useRef, useState} from "react";

function ImageEditing({file, onDone, outputType = "image/webp", quality = 0.9}) {
    const canvasRef = useRef(null);
    const imgRef = useRef(null);
    const urlRef = useRef(null);

    const [backgroundColor, setBackgroundColor] = useState("#FFFFFF");
    const [scale, setScale] = useState(1);

    const [isDragging, setIsDragging] = useState(false);

    const dragStartRef = useRef({ x: 0, y: 0 });
    const offsetRef = useRef({ x: 0, y: 0 });

    // const draw = useCallback(() => {
    //     const canvas = canvasRef.current;
    //     const ctx = canvas?.getContext("2d");
    //     const img = imgRef.current;
    //     if (!canvas || !ctx || !img) return;
    //
    //     ctx.fillStyle = backgroundColor;
    //     ctx.fillRect(0, 0, canvas.width, canvas.height);
    //
    //
    //     const drawW = img.width * scale;
    //     const drawH = img.height * scale;
    //
    // }, [backgroundColor, scale]);

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");
        const img = new Image();
        img.onload = () => {
            canvas.width = img.width;
            canvas.height = img.height;
            ctx.fillStyle = backgroundColor;
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(img, 0, 0);

        };
        img.src = URL.createObjectURL(file);

        return () => {
            URL.revokeObjectURL(img.src);
        }
    }, []);



    return (
        <div className={styles.main}>
            <canvas ref={canvasRef} />
            <label htmlFor="scale">Image scale:</label>
            <input id="scale" type="range" min="0" max="100" />
            <label htmlFor="color">Background color:</label>
            <input id="color" type="text" value={backgroundColor}/>
            <button>Submit</button>
        </div>
    );
}

export default ImageEditing;