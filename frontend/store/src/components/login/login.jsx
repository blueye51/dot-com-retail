import styles from "./login.module.css";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { setRoles, setToken } from "../store.jsx";
import useFetch from "../useFetch.jsx";
import { useNavigate, useLocation, Link } from "react-router-dom";
import {paths} from "../routes.jsx";

function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from?.pathname || paths.home();
    const dispatch = useDispatch();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const { data, error, loading, reFetch } = useFetch("/api/auth/login", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    useEffect(() => {
        if (!data) return;
        dispatch(setToken(data.accessToken));
        dispatch(setRoles(data.roles || []));
        navigate(from, { replace: true });
    }, [data, dispatch, navigate, from]);

    useEffect(() => {
        if (!error) return;
        console.error("Login error:", error);
        alert("Login failed: " + (error.message || "Unknown error"));
    }, [error]);

    const handleSubmit = (event) => {
        event.preventDefault();

        reFetch({
            body: {
                email: email.trim(),
                password,
            },
        });
    };

    return (
        <div className={styles.loginContainer}>
            <h2>Login</h2>

            <form onSubmit={handleSubmit} className={styles.loginForm}>
                <div className={styles.formField}>
                    <label htmlFor="email">Email:</label>
                    <input
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Email"
                        type="email"
                        id="email"
                        name="email"
                        inputMode="email"
                        autoComplete="email"
                        value={email}
                        required
                    />
                </div>

                <div className={styles.formField}>
                    <label htmlFor="password">Password:</label>
                    <input
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Password"
                        type="password"
                        id="password"
                        name="password"
                        autoComplete="current-password"
                        value={password}
                        required
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>

            <Link to="/register">Register here</Link>
        </div>
    );
}

export default Login;
