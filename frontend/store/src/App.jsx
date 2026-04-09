import './App.css'
import {BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import {useDispatch} from "react-redux";
import {useEffect, useState} from "react";
import {refreshAccessToken, fetchUserSettings} from "./components/useFetch.js";
import {setCart} from "./components/store.js";
import {PATHS, paths} from './components/routes.js'

import Login from './components/login/Login.jsx'
import MainLayout from "./components/layouts/MainLayout.jsx";
import AdminMenu from "./components/adminMenu/AdminMenu.jsx";
import RequiredRole from "./components/auth/RequiredRole.jsx";
import RequiredAuth from "./components/auth/RequiredAuth.jsx";
import Unauthorized from "./components/auth/Unauthorized.jsx";
import Home from "./components/home/Home.jsx";
import ProductList from "./components/product/productList/ProductList.jsx";
import MissingAuth from "./components/auth/MissingAuth.jsx";
import ProductCreation from "./components/product/productCreation/ProductCreation.jsx";
import CategoryTree from "./components/category/categoryTree/CategoryTree.jsx";
import CategoryAdmin from "./components/category/categoryAdmin/CategoryAdmin.jsx";
import {Register} from "./components/register/Register.jsx";
import ProductPage from "./components/product/ProductPage.jsx";
import OAuth2Callback from "./components/OAuth2Callback.jsx";
import Profile from "./components/profile/Profile.jsx";
import UnverifiedEmail from "./components/auth/UnverifiedEmail.jsx";
import EmailVerification from "./components/verification/EmailVerification.jsx";
import EmailVerifyToken from "./components/verification/EmailVerifyToken.jsx";
import ForgotPassword from "./components/auth/ForgotPassword.jsx";
import ResetPassword from "./components/auth/ResetPassword.jsx";
import TwoFactorVerification from "./components/verification/TwoFactorVerification.jsx";
import BrandCreate from "./components/brand/brandCreate/BrandCreate.jsx";
import DeleteAccount from "./components/profile/DeleteAccount.jsx";
import ProductEdit from "./components/product/productEdit/ProductEdit.jsx";
import AdminOrders from "./components/admin/AdminOrders.jsx";
import AdminOrderDetail from "./components/admin/AdminOrderDetail.jsx";
import AdminUsers from "./components/admin/AdminUsers.jsx";
import BulkUpload from "./components/admin/BulkUpload.jsx";
import AdminReviews from "./components/admin/AdminReviews.jsx";
import Cart from "./components/cart/Cart.jsx";
import Checkout from "./components/checkout/Checkout.jsx";
import OrderConfirmation from "./components/order/OrderConfirmation.jsx";
import Orders from "./components/order/Orders.jsx";
import OrderDetail from "./components/order/OrderDetail.jsx";
import About from "./components/about/About.jsx";
import Support from "./components/support/Support.jsx";
import NotFound from "./components/error/NotFound.jsx";


function App() {
    const dispatch = useDispatch();
    const [loading, setLoading] = useState(true); // <— wait state

    useEffect(() => {
        const checkToken = async () => {
            try {
                const token = await refreshAccessToken(dispatch);
                await fetchUserSettings(dispatch, token);
                // Load server cart for logged-in users
                const BASE_URL = import.meta.env.VITE_API_BASE;
                const res = await fetch(`${BASE_URL}/api/cart`, {
                    headers: {Authorization: `Bearer ${token}`},
                    credentials: "include",
                });
                if (res.ok) {
                    const cart = await res.json();
                    dispatch(setCart(cart));
                }
            } catch {
                console.log("No valid refresh token found.");
            } finally {
                setLoading(false);
            }
        };
        void checkToken();
    }, [dispatch]);

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <BrowserRouter>
            <Routes>
                {/* Guest-only */}
                <Route element={<MissingAuth/>}>
                    <Route path={PATHS.login} element={<Login/>}/>
                    <Route path={PATHS.register} element={<Register/>}/>
                    <Route path={PATHS.verify2fa} element={<TwoFactorVerification/>}/>
                </Route>

                {/* No wrapper needed */}
                <Route path={PATHS.oauth2Callback} element={<OAuth2Callback/>}/>
                <Route path={PATHS.unauthorized} element={<Unauthorized/>}/>
                <Route path={PATHS.verifyEmailToken} element={<EmailVerifyToken/>}/>
                <Route path={PATHS.forgotPassword} element={<ForgotPassword/>}/>
                <Route path={PATHS.resetPassword} element={<ResetPassword/>}/>

                {/* Email verification (logged in but unverified) */}
                <Route element={<UnverifiedEmail/>}>
                    <Route path={PATHS.verifyEmail} element={<EmailVerification/>}/>
                </Route>

                {/* Everything with MainLayout */}
                <Route element={<MainLayout/>}>
                    {/* Public */}
                    <Route path={PATHS.home} element={<Home/>}/>
                    <Route path={PATHS.product} element={<ProductPage/>}/>
                    <Route path={PATHS.cart} element={<Cart/>}/>
                    <Route path={PATHS.about} element={<About/>}/>
                    <Route path={PATHS.support} element={<Support/>}/>

                    {/* Authenticated */}
                    <Route element={<RequiredAuth/>}>
                        <Route path={PATHS.profile} element={<Profile/>}/>
                        <Route path={PATHS.deleteAccount} element={<DeleteAccount/>}/>
                        <Route path={PATHS.checkout} element={<Checkout/>}/>
                        <Route path={PATHS.orderConfirmation} element={<OrderConfirmation/>}/>
                        <Route path={PATHS.orders} element={<Orders/>}/>
                        <Route path={PATHS.order} element={<OrderDetail/>}/>
                    </Route>

                    {/* Admin */}
                    <Route element={<RequiredRole allowed={["ADMIN"]}/>}>
                        <Route path={PATHS.admin} element={<AdminMenu/>}/>
                        <Route path={PATHS.brandCreate} element={<BrandCreate/>}/>
                        <Route path={PATHS.productList} element={<ProductList/>}/>
                        <Route path={PATHS.createProduct} element={<ProductCreation/>}/>
                        <Route path={PATHS.categoryTree} element={<CategoryTree/>}/>
                        <Route path={PATHS.categoryAdmin} element={<CategoryAdmin/>}/>
                        <Route path={PATHS.editProduct} element={<ProductEdit/>}/>
                        <Route path={PATHS.adminOrders} element={<AdminOrders/>}/>
                        <Route path={PATHS.adminOrder} element={<AdminOrderDetail/>}/>
                        <Route path={PATHS.adminUsers} element={<AdminUsers/>}/>
                        <Route path={PATHS.bulkUpload} element={<BulkUpload/>}/>
                        <Route path={PATHS.adminReviews} element={<AdminReviews/>}/>
                    </Route>
                </Route>

                {/* 404 */}
                <Route element={<MainLayout/>}>
                    <Route path={PATHS.any} element={<NotFound/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App
