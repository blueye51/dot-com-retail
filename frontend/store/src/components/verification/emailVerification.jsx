import useFetch from "../useFetch.js";

export default function EmailVerification() {
    const {data, loading, reFetch} = useFetch("/api/email-verification/send", {
        method: "POST",
        withAuth: true,
        immediate: true
    });

    if (loading) {
        return <p>Sending verification link...</p>;
    }

    return (
        <div>
            <p>A verification link has been sent to <strong>{data?.email}</strong></p>
            <p>Check your inbox and click the link to verify your email address.</p>
            <button onClick={() => reFetch()}>Resend Link</button>
        </div>
    );
}
