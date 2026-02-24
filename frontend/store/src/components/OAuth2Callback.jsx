import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useDispatch } from "react-redux";
import { setToken, setRoles } from "./store.jsx";
import { paths } from "./routes.jsx";
import useFetch from "./useFetch.jsx";

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
            navigate(paths.login(), { replace: true });
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
        dispatch(setToken(data.accessToken));
        dispatch(setRoles(data.roles ?? []));
        navigate(paths.home(), { replace: true });
    }, [data]);

    useEffect(() => {
        if (!fetchError) return;
        navigate(paths.login(), { replace: true });
    }, [fetchError]);

    return <div>Signing you in...</div>;
}

export default OAuth2Callback;
