import {Navigate, Outlet} from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.jsx"

function MissingAuth () {
    const token = useSelector((s) => s.auth.token);

    if (token) {
        return <Navigate to={paths.home()} replace />;
    }

    return <Outlet/>;
}

export default MissingAuth;