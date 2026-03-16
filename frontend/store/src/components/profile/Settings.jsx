import useFetch from "../useFetch.js";
import styles from "./Settings.module.css";
import {useEffect, useState} from "react";
import {useDispatch} from "react-redux";
import {setSettings} from "../store.js";

export default function Settings() {
    const dispatch = useDispatch();
    const {data, loading, reFetch} = useFetch("/api/users/me/settings", {
        withAuth: true
    });

    const {reFetch: saveSettings, loading: saving} = useFetch("/api/users/me/settings", {
        method: "PUT",
        immediate: false,
        withAuth: true
    });

    const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);
    const [imperialUnits, setImperialUnits] = useState(false);

    useEffect(() => {
        if (data) {
            setTwoFactorEnabled(data.twoFactorEnabled ?? false);
            setImperialUnits(data.imperialUnits ?? false);
        }
    }, [data]);

    const handleSave = async () => {
        try {
            await saveSettings({body: {twoFactorEnabled, imperialUnits}});
            dispatch(setSettings({imperialUnits}));
            reFetch();
        } catch (e) {
            console.error("Failed to save settings", e);
        }
    };

    if (loading) return <div>Loading settings...</div>;

    return (
        <div className={styles.container}>
            <h3>Settings</h3>
            <label>
                <input
                    type="checkbox"
                    checked={twoFactorEnabled}
                    onChange={(e) => setTwoFactorEnabled(e.target.checked)}
                />
                Two-Factor Authentication
            </label>
            <label>
                <input
                    type="checkbox"
                    checked={imperialUnits}
                    onChange={(e) => setImperialUnits(e.target.checked)}
                />
                Imperial Units (lbs, oz)
            </label>
            <div>
                <button onClick={handleSave} disabled={saving}>
                    {saving ? "Saving..." : "Save"}
                </button>
            </div>
        </div>
    );
}
