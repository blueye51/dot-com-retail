import { Outlet, Navigate, useLocation } from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.jsx";

function RequiredAuth() {
    const token = useSelector((state) => state.auth.token);
    const location = useLocation();

    console.log("reqAuth, token: "+ token);

    if (!token) {
        return <Navigate to={paths.login()} replace state={{ from: location }} />;
    }

    return <Outlet />;
}

export default RequiredAuth;