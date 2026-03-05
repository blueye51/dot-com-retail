import styles from './register.module.css';
import useFetch from "../useFetch.js";
import {useRef, useState} from "react";
import { Turnstile } from "@marsidev/react-turnstile";
import {Link} from "react-router-dom";
import {paths} from "../routes.js";

export function Register() {
    const tsRef = useRef(null);
    const [tsToken, setTsToken] = useState(null);

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");

    const { data, error, loading, reFetch } = useFetch("/api/auth/register", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    const handleRegister = (e) => {
        e.preventDefault();

        const payload = {
            email: email.trim(),
            password,
            name: name.trim(),
            turnstileToken: tsToken,
        }

        if (!payload.turnstileToken) {
            alert("you need to complete the CAPTCHA")
            return;
        };

        reFetch({
            body: payload,
        });

        tsRef.current?.reset?.();
        setTsToken(null);
    };

    return (
        <div className={styles.main}>
            <h1>Register</h1>

            <form onSubmit={handleRegister}>
                <input
                    type="text"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                />

                <input
                    type="text"
                    placeholder="Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    autoComplete="name"
                />

                <Turnstile
                    ref={tsRef}
                    siteKey={import.meta.env.VITE_TURNSTILE_VISIBLE_SITEKEY}
                    onSuccess={(token) => setTsToken(token)}
                    onExpire={() => setTsToken(null)}
                    onError={() => setTsToken(null)}
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Registering..." : "Register"}
                </button>

                {error && <div>{String(error)}</div>}
                {data && (<div>Registered
                <Link to={paths.login()}>Login here</Link>
                </div>)}
            </form>
        </div>
    );
}
