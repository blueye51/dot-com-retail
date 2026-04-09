import {Link} from "react-router-dom";
import {paths} from "../routes.js";
import styles from './Footer.module.css';

function Footer() {
    return (
        <footer className={styles.footer}>
            <div className={styles.links}>
                <Link to={paths.home()}>Home</Link>
                <Link to={paths.about()}>About</Link>
                <Link to={paths.support()}>Contact & Support</Link>
            </div>
            <p className={styles.copy}>&copy; {new Date().getFullYear()} Electronics Store. All rights reserved.</p>
        </footer>
    );
}

export default Footer;
