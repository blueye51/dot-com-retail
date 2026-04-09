import useFetch from "../useFetch.js";
import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import Modal from "../modal/Modal.jsx";
import Settings from "./Settings.jsx";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";


export default function Profile() {
    const [user, setUser] = useState({})
    const [settingsOpen, setSettingsOpen] = useState(false)
    const {emailVerified} = useSelector((state) => state.auth);
    const {data, error, loading} = useFetch("/api/users/me", {
        method: "GET",
        immediate: true,
        withAuth: true,
    })
    const toggleSettings = () => setSettingsOpen(p => !p)

    useEffect(() => {
        if (!data) return;
        setUser(data)
    }, [data]);

    if (error) return <ErrorMessage message="Failed to load profile." />;
    if (loading) {
        return <p>Loading...</p>
    }
    return (
        <div>
            <Helmet>
                <title>Profile - Electronics Store</title>
                <meta name="description" content="Manage your account settings, addresses, and preferences." />
            </Helmet>
            <p>profile</p>
            <p>name: {user.name}</p>
            <p>email: {user.email}</p>
            {!emailVerified && <Link to={paths.verifyEmail()}>Verify Email</Link>}
            {user.provider === "LOCAL" && <Link to={paths.forgotPassword()}>Change Password</Link>}
            {user.provider === "LOCAL" && <Link to={paths.deleteAccount()}>Delete Account</Link>}
            <button onClick={toggleSettings}>settings</button>
            <Modal open={settingsOpen} onClose={toggleSettings}>
                <Settings />
            </Modal>
        </div>
    )
}