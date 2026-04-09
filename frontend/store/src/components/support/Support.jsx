import {Helmet} from "react-helmet-async";
import {useState} from "react";
import styles from "./Support.module.css";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function Support() {
    const [form, setForm] = useState({name: "", email: "", subject: "", message: ""});
    const [sending, setSending] = useState(false);
    const [sent, setSent] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (e) => setForm(prev => ({...prev, [e.target.name]: e.target.value}));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSending(true);
        setError(null);
        try {
            const res = await fetch(`${BASE_URL}/api/contact`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(form),
            });
            if (!res.ok) throw new Error("Failed to send");
            setSent(true);
        } catch {
            setError("Failed to send message. Please try again.");
        } finally {
            setSending(false);
        }
    };

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

            <h2>Send Us a Message</h2>

            {sent ? (
                <p className={styles.success}>Your message has been sent. We'll get back to you soon.</p>
            ) : (
                <form onSubmit={handleSubmit} className={styles.form}>
                    <label className={styles.field}>
                        Name
                        <input
                            type="text"
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            required
                            maxLength={100}
                        />
                    </label>
                    <label className={styles.field}>
                        Email
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            required
                            maxLength={100}
                        />
                    </label>
                    <label className={styles.field}>
                        Subject
                        <input
                            type="text"
                            name="subject"
                            value={form.subject}
                            onChange={handleChange}
                            required
                            maxLength={200}
                        />
                    </label>
                    <label className={styles.field}>
                        Message
                        <textarea
                            name="message"
                            value={form.message}
                            onChange={handleChange}
                            required
                            maxLength={2000}
                            rows={5}
                        />
                    </label>
                    {error && <p className={styles.error}>{error}</p>}
                    <button type="submit" disabled={sending} className={styles.submitBtn}>
                        {sending ? "Sending..." : "Send Message"}
                    </button>
                </form>
            )}

            <p className={styles.note}>
                We typically respond within 24 hours on business days.
            </p>
        </div>
    );
}
