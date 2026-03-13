import useFetch from "../useFetch.js";
import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import Modal from "../modal/modal.jsx";
import Settings from "./settings.jsx";


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

    if (error) {
        return <p>Error</p>
    }
    if (loading) {
        return <p>Loading...</p>
    }
    return (
        <div>
            <p>profile</p>
            <p>name: {user.name}</p>
            <p>email: {user.email}</p>
            {!emailVerified && <Link to={paths.verifyEmail()}>Verify Email</Link>}
            {user.provider === "LOCAL" && <Link to={paths.forgotPassword()}>Change Password</Link>}
            <button onClick={toggleSettings}>settings</button>
            <Modal open={settingsOpen} onClose={toggleSettings}>
                <Settings />
            </Modal>
        </div>
    )
}