import {useSelector} from "react-redux";
import {Navigate, Outlet, useLocation} from "react-router-dom";
import {paths} from "../routes.js";


function RequiredRole ({ allowed = [] }) {
    if (!Array.isArray(allowed)) {
        throw new Error("RequiredRole: 'allowed' prop must be an array");
    }

    const { token, emailVerified, roles } = useSelector((state) => state.auth);
    const location = useLocation();

    if (!token) {
        return <Navigate to={paths.login()} replace state={{ from: location }} />;
    }
    if (!emailVerified) {
        return <Navigate to={paths.verifyEmail()} replace state={{ from: location }} />;
    }
    if (!roles.some(r => allowed.includes(r))) {
        alert("access denied")
        return <Navigate to={paths.home()} replace />;
    }
    return <Outlet />;
}

export default RequiredRole;