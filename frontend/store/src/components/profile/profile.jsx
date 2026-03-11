import useFetch from "../useFetch.js";
import {useEffect, useState} from "react";


export default function Profile() {
    const [user, setUser] = useState({})
    const [settings, setSettings] = useState({})
    const {data, error, loading} = useFetch("/api/users/me", {
        method: "GET",
        immediate: true,
        withAuth: true,
    })

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
            <p>settings: twoFactorEnabled {user.settings?.twoFactorEnabled ? "true" : "false"}</p>
        </div>
    )
}