import {useDispatch} from "react-redux";
import {useNavigate} from "react-router-dom";
import {logout, clearSettings} from "./store.js";
import useFetch from "./useFetch.js";
import {paths} from "./routes.js";

export function useLogout() {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const {loading, reFetch} = useFetch("/api/auth/refresh/logout", {
        method: "DELETE",
        withAuth: true,
        immediate: false,
    });

    const handleLogout = async () => {
        try {
            await reFetch();
        } catch (e) {
            alert(e);
            return;
        }
        dispatch(logout());
        dispatch(clearSettings());
        navigate(paths.login(), {replace: true});
    };

    return {handleLogout, loading};
}
