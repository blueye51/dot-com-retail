import {Link} from "react-router-dom";
import {Helmet} from "react-helmet-async";
import {paths} from "../routes.js";
import styles from "./Error.module.css";

export default function NotFound() {
    return (
        <div className={styles.page}>
            <Helmet>
                <title>Page Not Found - Electronics Store</title>
            </Helmet>
            <h1 className={styles.code}>404</h1>
            <p className={styles.message}>The page you're looking for doesn't exist.</p>
            <Link to={paths.home()} className={styles.homeLink}>Go Home</Link>
        </div>
    );
}
