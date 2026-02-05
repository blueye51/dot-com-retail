import styles from './productImageMenu.module.css';
import Modal from "../../modal/modal.jsx";
import ImageUpload from "./imageUpload/imageUpload.jsx";
import {useEffect, useState} from "react";
import ImageEditing from "./imageEditing/imageEditing.jsx";
import useFetch from "../../useFetch.jsx";

function ProductImageMenu({maxFiles = 1, setImageUrls}) {

    const [uploaderOpen, setUploaderOpen] = useState(false)
    const [editorOpen, setEditorOpen] = useState(false)

    const closeUploader = () => setUploaderOpen(false);
    const closeEditor = () => setEditorOpen(false);

    const [rawFile, setRawFile] = useState(null);
    const [editedBlob, setEditedBlob] = useState(null);

    const {data, error, loading, reFetch, abort} = useFetch("/api/images/public", {
        method: "POST",
        withAuth: true,
        immediate: false,
    })

    useEffect(() => {
        if (!data) return;

        setImageUrls((prev) => [...prev, data.url]);
    }, [data, setImageUrls]);

    const handlePickedFile = (file) => {
        setRawFile(file);
        setUploaderOpen(false);
        setEditorOpen(true);
    };

    const handleEditorDone = async (blob) => {
        setEditedBlob(blob);
        setEditorOpen(false);

        const form = new FormData();
        form.append("file", blob, rawFile?.name ?? product_image.webp);

        reFetch({body: form})

        setRawFile(null);
        setEditedBlob(null);
    }


    return (
        <div className={styles.menu}>
            <button onClick={() => setUploaderOpen(true)}>Add Image</button>
            <Modal open={uploaderOpen && !editorOpen} onClose={closeUploader}>
                <ImageUpload onPickFile={handlePickedFile}/>
            </Modal>
            <Modal open={editorOpen} onClose={closeEditor}>
                <ImageEditing onDone={handleEditorDone} file={rawFile}/>
            </Modal>
        </div>
    );
}

export default ProductImageMenu;