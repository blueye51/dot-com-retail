import {Helmet} from "react-helmet-async";
import styles from "./Support.module.css";

export default function Support() {
    return (
        <div className={styles.page}>
            <Helmet>
                <title>Contact & Support - Electronics Store</title>
                <meta name="description" content="Get in touch with us via email or phone for any questions or support." />
            </Helmet>
            <h1>Contact & Support</h1>
            <p>Need help? Reach out to us through any of the channels below.</p>

            <div className={styles.contactCard}>
                <div className={styles.row}>
                    <span className={styles.label}>Email</span>
                    <a href="mailto:eric.rand66@gmail.com">eric.rand66@gmail.com</a>
                </div>
                <div className={styles.row}>
                    <span className={styles.label}>Phone</span>
                    <a href="tel:+37259193633">+372 5919 3633</a>
                </div>
            </div>

            <p className={styles.note}>
                We typically respond within 24 hours on business days.
            </p>
        </div>
    );
}
