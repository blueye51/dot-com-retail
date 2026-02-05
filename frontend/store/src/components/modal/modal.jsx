import {createPortal} from "react-dom";
import styles from './modal.module.css';
import {useEffect} from "react";

function Modal(
    {
        open,
        onClose,
        children,
        closeOnBackdrop = true,
        closeOnEsc = true,
        className = "",
    }
) {
    useEffect(() => {
        if (!open) return;

        const original = document.body.style.overflow;
        document.body.style.overflow = "hidden";

        return () => {
            document.body.style.overflow = original;
        };
    }, [open]);


    // ESC key close
    useEffect(() => {
        if (!open || !closeOnEsc) return;

        const handleKeyDown = (e) => {
            if (e.key === "Escape") {
                onClose();
            }
        };

        document.addEventListener("keydown", handleKeyDown);
        return () => document.removeEventListener("keydown", handleKeyDown);
    }, [open, closeOnEsc, onClose]);

    if (!open) return null;

    const modal = (
        <div
            className={styles.backdrop}
            onClick={closeOnBackdrop ? onClose : undefined}
            role="dialog" aria-modal="true" // Accessibility for screen readers
        >
            <button
                className={styles.close}
                onClick={onClose}
                aria-label="Close modal"
            >
                ×
            </button>
            <div
                className={`${styles.window} ${className}`}
                onClick={(e) => e.stopPropagation()}
            >
                {children}
            </div>
        </div>
    )

    return createPortal(modal, document.getElementById("modal-root"));
}

export default Modal;