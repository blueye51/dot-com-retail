import styles from './imageUpload.module.css';
import Modal from "../../../modal/modal.jsx";
import useFetch from "../../../useFetch.jsx";
import {useEffect} from "react";

const MAX = 20 * 1024 * 1024; // 20MB
const allowed = new Set([
    "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml"
]);

function imageUpload({onPickFile}) {

    async function handleFile(e) {
        const file = e.target.files?.[0];
        if (!file) {
            alert("No file selected");
            return;
        }

        if (!allowed.has(file.type)) {
            alert("Uploaded file is not an image");
            return;
        }

        if (file.size > MAX) {
            alert("File size exceeds limit");
            return;
        }

        const ok = await decodeTest(file);
        if (!ok) {
            e.target.value = "";
            return;
        }


        onPickFile(file);

        e.target.value = "";
    }

    async function decodeTest(file) {
        const url = URL.createObjectURL(file);

        try {
            const img = new Image();

            await new Promise((resolve, reject) => {
                img.onload = resolve;
                img.onerror = reject;
                img.src = url;
            });

            if (img.width * img.height > 40_000_000) {
                alert("Image has too many pixels");
                return false;
            }

            return true;
        } catch (err) {
            alert("Failed to decode image");
            return false;
        } finally {
            URL.revokeObjectURL(url);
        }
    }

    return (
        <div className={styles.uploadBox}>
            <input
                id="file"
                type="file"
                hidden
                onChange={handleFile}
                accept="image/jpeg, image/png, image/webp, image/gif, image/svg+xml"/>
            <label className={styles.input} htmlFor="file">Drop file here<br/>
                or click to browse</label>
        </div>
    );
}

export default imageUpload;