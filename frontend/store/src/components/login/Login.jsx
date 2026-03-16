import styles from "./Login.module.css";
import {useEffect, useRef, useState} from "react";
import {useDispatch} from "react-redux";
import {Turnstile} from "@marsidev/react-turnstile";
import {setAuth, buildAuthFromToken} from "../store.js";
import useFetch, {fetchUserSettings} from "../useFetch.js";
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
    const [showPassword, setShowPassword] = useState(false);

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
            fetchUserSettings(dispatch, result.accessToken);
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
            {location.state?.error && <p className={styles.errorMsg}>{location.state.error}</p>}
            {location.state?.success && <p className={styles.successMsg}>{location.state.success}</p>}

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
                    <div className={styles.passwordWrapper}>
                        <input
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Password"
                            type={showPassword ? "text" : "password"}
                            id="password"
                            name="password"
                            autoComplete="current-password"
                            value={password}
                            required
                        />
                        <button
                            type="button"
                            className={styles.showPasswordBtn}
                            onMouseDown={() => setShowPassword(true)}
                            onMouseUp={() => setShowPassword(false)}
                            onMouseLeave={() => setShowPassword(false)}
                            onTouchStart={() => setShowPassword(true)}
                            onTouchEnd={() => setShowPassword(false)}
                            tabIndex={-1}
                        >
                            👁
                        </button>
                    </div>
                </div>

                <div className={styles.turnstileWrapper}>
                    <Turnstile
                        ref={tsRef}
                        siteKey={import.meta.env.VITE_TURNSTILE_VISIBLE_SITEKEY}
                        onSuccess={(token) => setTsToken(token)}
                        onExpire={() => setTsToken(null)}
                        onError={() => setTsToken(null)}
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>

            <div className={styles.links}>
                <Link to={paths.forgotPassword()}>Forgot password?</Link>
                <Link to={paths.register()}>Register here</Link>
            </div>
            <div className={styles.divider}><span>or</span></div>
            <button type="button" className={styles.googleBtn} onClick={handleGoogleLogin}>
                Continue with Google
            </button>

        </div>
    );
}

export default Login;
