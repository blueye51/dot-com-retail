import {useLocation, useNavigate, Navigate} from "react-router-dom";
import useFetch, {fetchUserSettings} from "../useFetch.js";
import {useState} from "react";
import {useDispatch} from "react-redux";
import {buildAuthFromToken, setAuth} from "../store.js";
import {paths} from "../routes.js";
import {mergeGuestCart} from "../useCartMerge.js";

export default function TwoFactorVerification() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { state } = useLocation();

    const [otpCode, setOtpCode] = useState("");

    const { reFetch, loading } = useFetch("/api/auth/login/verify-2fa", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    if (!state?.tempCode) {
        return <Navigate to={paths.login()} replace />;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const result = await reFetch({ body: { tempCode: state.tempCode, otpCode } });
            dispatch(setAuth(buildAuthFromToken(result.accessToken)));
            fetchUserSettings(dispatch, result.accessToken);
            mergeGuestCart(dispatch, result.accessToken);
            navigate(state.from || paths.home(), { replace: true });
        } catch (err) {
            alert("Verification failed: " + (err.message || "Unknown error"));
        }
    };

    return (
        <div>
            <h1>Two-Factor Verification</h1>
            <p>A code has been sent to your email.</p>
            <form onSubmit={handleSubmit}>
                <input
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value)}
                    placeholder="Enter code"
                    required
                />
                <button type="submit" disabled={loading}>
                    {loading ? "Verifying..." : "Verify"}
                </button>
            </form>
        </div>
    );
}