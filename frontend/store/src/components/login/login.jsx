import styles from "./login.module.css";
import {useEffect, useRef, useState} from "react";
import {useDispatch} from "react-redux";
import {Turnstile} from "@marsidev/react-turnstile";
import {setAuth, buildAuthFromToken} from "../store.js";
import useFetch from "../useFetch.js";
import {useNavigate, useLocation, Link} from "react-router-dom";
import {paths} from "../routes.js";

function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from?.pathname || paths.home();
    const dispatch = useDispatch();

    const tsRef = useRef(null);
    const [tsToken, setTsToken] = useState(null);

    const backend = import.meta.env.VITE_API_BASE;
    const redirectUri = `${window.location.origin}/oauth2/callback`;

    const handleGoogleLogin = () => {
        window.location.href =
            `${backend}/oauth2/authorization/google?redirect_uri=${encodeURIComponent(redirectUri)}`;
    };

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const {error, loading, reFetch} = useFetch("/api/auth/login", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    useEffect(() => {
        if (!error) return;
        console.error("Login error:", error);
        alert("Login failed: " + (error.message || "Unknown error"));
    }, [error]);

    const handleSubmit = async (event) => {
        event.preventDefault();

        const payload = {
            email: email.trim(),
            password,
            turnstileToken: tsToken,
        }

        if (!payload.turnstileToken) {
            alert("you need to complete the CAPTCHA")
            return;
        };

        try {
            const result = await reFetch({ body: payload });
            if (result["2faRequired"]) {
                navigate(paths.verify2fa(), { replace: true, state: { tempCode: result.tempCode, from } });
                return;
            }
            dispatch(setAuth(buildAuthFromToken(result.accessToken)));
            navigate(from, { replace: true });
        } catch (err) {
            alert("Login failed: " + (err.message || "Unknown error"));
        }


        tsRef.current?.reset?.();
        setTsToken(null);
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

                <Turnstile
                    ref={tsRef}
                    siteKey={import.meta.env.VITE_TURNSTILE_VISIBLE_SITEKEY}
                    onSuccess={(token) => setTsToken(token)}
                    onExpire={() => setTsToken(null)}
                    onError={() => setTsToken(null)}
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>

            <Link to={paths.register()}>Register here</Link>
            <button type="button" onClick={handleGoogleLogin}>
                Continue with Google
            </button>

        </div>
    );
}

export default Login;
