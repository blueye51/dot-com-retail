import Footer from "../footer/footer.jsx";
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