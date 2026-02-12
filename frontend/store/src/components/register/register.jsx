import styles from './register.module.css';
import useFetch from "../useFetch.jsx";
import { useState } from "react";
import {Link} from "react-router-dom";
import {paths} from "../routes.jsx";

export function Register() {
    const { data, error, loading, reFetch } = useFetch("/api/auth/register", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");

    const handleRegister = (e) => {
        e.preventDefault();

        reFetch({
            body: {
                email: email.trim(),
                password,
                name: name.trim(),
            },
        });
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
