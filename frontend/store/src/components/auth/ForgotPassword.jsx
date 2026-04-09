import {useState} from "react";
import useFetch from "../useFetch.js";

export default function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [sent, setSent] = useState(false);

    const {loading, reFetch} = useFetch("/api/auth/password-reset/send", {
        method: "POST",
        immediate: false
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        await reFetch({body: {email: email.trim()}});
        setSent(true);
    };

    if (sent) {
        return (
            <div>
                <p>If an account exists with that email, a reset link has been sent.</p>
                <p>Check your inbox.</p>
            </div>
        );
    }

    return (
        <div>
            <h1>Forgot Password</h1>
            <form onSubmit={handleSubmit}>
                <label htmlFor="email">Email:</label>
                <input
                    type="email"
                    id="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    required
                />
                <button type="submit" disabled={loading}>
                    {loading ? "Sending..." : "Send Reset Link"}
                </button>
            </form>
        </div>
    );
}
