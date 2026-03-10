import useFetch, {getClaimsFromToken, refreshAccessToken} from "../useFetch.js";
import {useState} from "react";
import {useDispatch} from "react-redux";
import {setAuth} from "../store.js";
import {useNavigate} from "react-router-dom";
import {paths} from "../routes.js";

export default function EmailVerification() {
    const [code, setCode] = useState("");
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const {data, loading, reFetch} = useFetch("/api/email-verification/send", {
        method: "POST",
        withAuth: true,
        immediate: true
    });

    const {reFetch: verify, loading: verifying} = useFetch("/api/email-verification/verify", {
        method: "POST",
        withAuth: true,
        immediate: false
    });

    const handleVerify = async () => {
        try {
            await verify({body: {code}});
            await refreshAccessToken(dispatch);
            navigate(paths.home(), {replace: true});
        } catch (err) {
            alert("Verification failed: " + (err.message || "Unknown error"));
        }
    };


    if (loading) {
        return <p>sending code...</p>;
    }
    if (verifying) {
        return <p>verifying...</p>;
    }

    return (
        <div>
            <p>Code sent to {data?.email}</p>
            <input value={code} onChange={e => setCode(e.target.value)} placeholder="Enter code"/>
            <button onClick={handleVerify}>Verify</button>
            <button onClick={() => reFetch()}>Resend</button>
        </div>
    );
}