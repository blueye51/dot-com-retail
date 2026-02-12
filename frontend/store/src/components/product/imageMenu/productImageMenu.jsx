import styles from './productImageMenu.module.css';
import Modal from "../../modal/modal.jsx";
import ImageUpload from "./imageUpload/imageUpload.jsx";
import {useEffect, useState} from "react";
import ImageEditing from "./imageEditing/imageEditing.jsx";
import useFetch from "../../useFetch.jsx";

function ProductImageMenu({maxFiles = 1, setImages, images}) {
    const [uploaderOpen, setUploaderOpen] = useState(false)
    const [editorOpen, setEditorOpen] = useState(false)

    const closeUploader = () => setUploaderOpen(false);
    const closeEditor = () => setEditorOpen(false);

    const [rawFile, setRawFile] = useState(null);

    const {data, error, loading, reFetch, abort} = useFetch("/api/images/public", {
        method: "POST",
        withAuth: true,
        immediate: false,
    })

    useEffect(() => {
        if (error) {
            console.error("Image upload error:", error);
            alert("Failed to upload image: " + (error.message || "Unknown error"));
            return;
        }
        if (!data) return;

        setImages((prev) => [
            ...prev,
            {key: data.key, url: data.url}
        ]);

    }, [data, error]);

    const handlePickedFile = (file) => {
        setRawFile(file);
        setUploaderOpen(false);
        setEditorOpen(true);
    };

    const baseName = (name) => (name ? name.replace(/\.[^.]+$/, "") : "product_image");

    const handleEditorDone = async (blob) => {
        setEditorOpen(false);

        const form = new FormData();
        const filename = `${baseName(rawFile?.name)}.webp`;
        form.append("file", blob, filename);

        await reFetch({body: form}).catch()

        setRawFile(null);
    }


    const removeAt = (index) => {
        setImages(prev => prev.filter((_, i) => i !== index));
    };

    const move = (from, to) => {
        setImages(prev => {
            if (to < 0 || to >= prev.length) return prev;

            const next = [...prev];
            [next[from], next[to]] = [next[to], next[from]];
            return next;
        });
    };

    const handleMoveUp = (index) => move(index, index - 1);
    const handleMoveDown = (index) => move(index, index + 1);


    const renderImageList = () => {
        return images.map((img, index) => (
            <div key={img.key} className={styles.imageContainer}>
                <img
                    src={img.url}
                    alt={`Product Image ${index + 1}`}
                    className={styles.imagePreview}/>
                <div className={styles.buttonsContainer}>
                    <button
                        className={styles.deleteButton}
                        onClick={() => removeAt(index)}
                    >
                        🗑
                    </button>
                    <button
                        className={styles.moveButton}
                        onClick={() => handleMoveUp(index)}
                    >
                        ⬆
                    </button>
                    <button
                        className={styles.moveButton}
                        onClick={() => handleMoveDown(index)}
                    >
                        ⬇
                    </button>
                </div>
            </div>
        ));
    }


    return (
        <div className={styles.menu}>
            <button onClick={() => setUploaderOpen(true)}>Add Image</button>
            <Modal open={uploaderOpen && !editorOpen} onClose={closeUploader}>
                <ImageUpload onPickFile={handlePickedFile}/>
            </Modal>
            <Modal open={editorOpen} onClose={closeEditor}>
                <ImageEditing onDone={handleEditorDone} file={rawFile} loading={loading}/>
            </Modal>
            <div className={styles.imageList}>
                {renderImageList()}
            </div>
        </div>
    );
}

export default ProductImageMenu;