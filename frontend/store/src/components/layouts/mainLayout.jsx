import styles from './mainLayout.module.css'
import Header from "../header/header.jsx";
import {ProductCard} from "../productCard/productCard.jsx";
import {Outlet} from "react-router-dom";
import Footer from "../footer/footer.jsx";

function MainLayout () {
    return (
        <>
            <Header />
            <Outlet />
            <Footer />
        </>
    )
}

export default MainLayout