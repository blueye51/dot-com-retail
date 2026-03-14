import {useState} from "react";
import {useDispatch} from "react-redux";
import {useNavigate} from "react-router-dom";
import useFetch from "../useFetch.js";
import {logout} from "../store.js";
import {paths} from "../routes.js";

export default function DeleteAccount() {
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const {reFetch, loading} = useFetch("/api/users/me", {
        method: "DELETE",
        immediate: false,
        withAuth: true,
    });

    const handleDelete = async (e) => {
        e.preventDefault();
        setError(null);

        if (!password) {
            setError("Password is required");
            return;
        }

        try {
            await reFetch({body: {password}});
            dispatch(logout());
            navigate(paths.login(), {replace: true});
        } catch (err) {
            setError("Wrong password or failed to delete account");
        }
    };

    return (
        <div>
            <h2>Delete Account</h2>
            <p>This action is permanent and cannot be undone. All your data will be deleted.</p>
            <form onSubmit={handleDelete}>
                <label>
                    Confirm your password
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </label>
                {error && <p style={{color: "red"}}>{error}</p>}
                <button type="submit" disabled={loading} style={{color: "red"}}>
                    {loading ? "Deleting..." : "Delete My Account"}
                </button>
            </form>
        </div>
    );
}
