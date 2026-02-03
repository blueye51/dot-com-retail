import styles from './imageMenu.module.css';
import Modal from "../../modal/modal.jsx";
import ImageUpload from "./imageUpload/imageUpload.jsx";
import {useState} from "react";

function imageMenu({maxFiles = 1}) {

    const [open, setOpen] = useState(false)

    function close() {
        setOpen(false)
    }

    return (
        <div className={styles.menu}>
            <button onClick={() => setOpen(true)}>Add Image</button>
            <Modal open={open} onClose={close}>
                <ImageUpload isPublic={true}/>
            </Modal>
        </div>
    );
}

export default imageMenu;