import {useEffect, useState} from "react";
import {useParams, useNavigate} from "react-router-dom";
import {useDispatch} from "react-redux";
import {refreshAccessToken} from "../useFetch.js";
import {paths} from "../routes.js";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function EmailVerifyToken() {
    const {token} = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [error, setError] = useState(null);

    useEffect(() => {
        const verify = async () => {
            const res = await fetch(`${BASE_URL}/api/email-verification/verify?token=${token}`, {
                credentials: "include"
            });
            if (!res.ok) {
                setError("Invalid or expired verification link.");
                return;
            }
            try {
                await refreshAccessToken(dispatch);
            } catch {}
            navigate(paths.home(), {replace: true});
        };
        verify();
    }, [token, navigate, dispatch]);

    if (error) {
        return <p>{error}</p>;
    }

    return <p>Verifying your email...</p>;
}
