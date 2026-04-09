import {Link} from "react-router-dom";
import {paths} from "../routes.js";
import styles from "./Error.module.css";

export default function ErrorMessage({message = "Something went wrong.", onRetry}) {
    return (
        <div className={styles.page}>
            <h1 className={styles.code}>Error</h1>
            <p className={styles.message}>{message}</p>
            <div className={styles.actions}>
                {onRetry && <button onClick={onRetry} className={styles.retryBtn}>Try Again</button>}
                <Link to={paths.home()} className={styles.homeLink}>Go Home</Link>
            </div>
        </div>
    );
}
