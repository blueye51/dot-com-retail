import styles from './imageUpload.module.css';
import ImageEditing from "../imageEditing/ImageEditing.jsx";
import Modal from "../../../modal/modal.jsx";
import useFetch from "../../../useFetch.jsx";
import {useEffect} from "react";

function imageUpload({isPublic = false}) {

    const [open, setOpen] = useState(false);
    const [originalFile, setOriginalFile] = useState(null);
    const [editedFile, setEditedFile] = useState(null);

    const uri = `/api/upload/${isPublic ? 'public' : 'private'}`;

    const {data, error, loading, reFetch, abort} = useFetch(uri, {
        method: "POST",
        body: undefined,
        withAuth: true,
        immediate: false,
    })


    function close() {
        setOpen(false);
        setOriginalFile(null);
    }


    function handleFile(e) {
        const file = e.target.files?.[0];
        if (!file) {
            alert("No file selected");
            return;
        }

        if (!file.type.startsWith("image/")) {
            alert("Uploaded file is not an image");
            return;
        }

        setOriginalFile(file);
        setOpen(true);

        e.target.value = "";
    }

    async function uploadFile(file) {
        const form = new FormData();
        form.append("file", file, file.name);

        reFetch({ body: form })
    }

    return (
        <div className={styles.uploadBox}>
            <input
                id="file"
                type="file"
                hidden
                onChange={handleFile}/>
            <label className={styles.input} htmlFor="file">Drop file here<br/>
                or click to browse</label>
            <Modal open={open} onClose={close}>
                <ImageEditing
                    file={originalFile}
                    onDone={async (editedFile) => {
                    await uploadFile(editedFile);
                    close();
                    }
                }/>
            </Modal>
        </div>
    );
}

export default imageUpload;