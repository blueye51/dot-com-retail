import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useDispatch } from "react-redux";
import { setAuth, buildAuthFromToken } from "./store.js";
import { paths } from "./routes.js";
import useFetch from "./useFetch.js";

const API_BASE = import.meta.env.VITE_API_BASE;

function OAuth2Callback() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const { data, error: fetchError, loading, reFetch } = useFetch("/api/auth/oauth2/exchange", {
        method: "POST",
        withAuth: false,
        immediate: false,
    });

    useEffect(() => {
        const error = searchParams.get("error");
        if (error) {
            const message = error === "provider_conflict"
                ? "This email is already registered with a password. Please log in with your email and password."
                : "OAuth2 login failed.";
            navigate(paths.login(), { replace: true, state: { error: message } });
            return;
        }

        const code = searchParams.get("code");
        if (!code) {
            navigate(paths.login(), { replace: true });
            return;
        }

        reFetch({body: {code}})

    }, [searchParams, dispatch, navigate]);

    useEffect(() => {
        if (!data) return;
        dispatch(setAuth(buildAuthFromToken(data.accessToken)));
        navigate(paths.home(), { replace: true });
    }, [data]);

    useEffect(() => {
        if (!fetchError) return;
        navigate(paths.login(), { replace: true });
    }, [fetchError]);

    return <div>Signing you in...</div>;
}

export default OAuth2Callback;
