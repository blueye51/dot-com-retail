import {useSelector} from "react-redux";
import {Navigate, Outlet, useLocation} from "react-router-dom";


function RequiredRole ({ allowed }) {
    const token = useSelector((state) => state.auth.token);
    const roles = useSelector((state) => state.auth.roles);
    const location = useLocation();

    console.log("reqRole, token: "+ token +", roles: " + roles);
    if (!token) {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }
    const ok = Array.isArray(allowed) && allowed.some((r) => roles.includes(r));
    if (!ok) return <Navigate to="/unauthorized" replace />;
    return <Outlet />;
}

export default RequiredRole;