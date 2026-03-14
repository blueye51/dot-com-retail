import useFetch from "../useFetch.js";
import {useEffect, useState} from "react";

export default function Settings() {
    const {data, loading, reFetch} = useFetch("/api/users/me/settings", {
        withAuth: true
    });

    const {reFetch: saveSettings, loading: saving} = useFetch("/api/users/me/settings", {
        method: "PUT",
        immediate: false,
        withAuth: true
    });

    const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);

    useEffect(() => {
        if (data) {
            setTwoFactorEnabled(data.twoFactorEnabled);
        }
    }, [data]);

    const handleSave = async () => {
        try {
            await saveSettings({body: {twoFactorEnabled}});
            reFetch();
        } catch (e) {
            console.error("Failed to save settings", e);
        }
    };

    if (loading) return <div>Loading settings...</div>;

    return (
        <div>
            <h3>Settings</h3>
            <label>
                <input
                    type="checkbox"
                    checked={twoFactorEnabled}
                    onChange={(e) => setTwoFactorEnabled(e.target.checked)}
                />
                Two-Factor Authentication
            </label>
            <div>
                <button onClick={handleSave} disabled={saving}>
                    {saving ? "Saving..." : "Save"}
                </button>
            </div>
        </div>
    );
}
