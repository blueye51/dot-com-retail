import { Outlet, Navigate, useLocation } from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.js";

function RequiredAuth() {
    const { token, emailVerified } = useSelector((state) => state.auth);
    const location = useLocation();

    if (!token) {
        return <Navigate to={paths.login()} replace state={{ from: location }} />;
    }
    if (!emailVerified) {
        alert("verify email please")
        return <Navigate to={paths.verifyEmail()} replace state={{ from: location }} />;
    }

    return <Outlet />;
}

export default RequiredAuth;