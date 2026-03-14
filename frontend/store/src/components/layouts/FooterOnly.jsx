import Footer from "../footer/Footer.jsx";
import {Outlet} from "react-router-dom";

function FooterOnly () {
    return (
        <>
            <Outlet />
            <Footer />
        </>
    )

}
export default FooterOnly