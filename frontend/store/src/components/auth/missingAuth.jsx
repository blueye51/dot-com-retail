import {Navigate, Outlet} from "react-router-dom";
import {useSelector} from "react-redux";

function MissingAuth () {
    const token = useSelector((s) => s.auth.token);

    if (token) {
        return <Navigate to="/" replace />;
    }

    return <Outlet/>;
}

export default MissingAuth;