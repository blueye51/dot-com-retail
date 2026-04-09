import {useState} from "react";
import {useParams, useNavigate} from "react-router-dom";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";

export default function ResetPassword() {
    const {token} = useParams();
    const navigate = useNavigate();
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");

    const {loading, reFetch} = useFetch("/api/auth/password-reset/reset", {
        method: "POST",
        immediate: false
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (password !== confirm) {
            alert("Passwords do not match");
            return;
        }
        try {
            await reFetch({body: {token, newPassword: password}});
            alert("Password reset successfully");
            navigate(paths.login(), {replace: true});
        } catch (err) {
            alert("Reset failed: " + (err.message || "Invalid or expired link"));
        }
    };

    return (
        <div>
            <h1>Reset Password</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="password">New Password:</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="New password"
                        required
                    />
                </div>
                <div>
                    <label htmlFor="confirm">Confirm Password:</label>
                    <input
                        type="password"
                        id="confirm"
                        value={confirm}
                        onChange={e => setConfirm(e.target.value)}
                        placeholder="Confirm password"
                        required
                    />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? "Resetting..." : "Reset Password"}
                </button>
            </form>
        </div>
    );
}
