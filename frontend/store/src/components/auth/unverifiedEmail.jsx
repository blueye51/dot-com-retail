import { Outlet, Navigate, useLocation } from "react-router-dom";
import {useSelector} from "react-redux";
import {paths} from "../routes.js";

function UnverifiedEmail() {
    const { token, emailVerified } = useSelector((state) => state.auth);
    const location = useLocation();

    if (!token) {
        return <Navigate to={paths.login()} replace state={{ from: location }} />;
    }
    if (emailVerified) {
        return <Navigate to={paths.home()} replace state={{ from: location }} />
    }

    return <Outlet />;
}

export default UnverifiedEmail;