import './App.css'
import {BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import {useDispatch} from "react-redux";
import {useEffect, useState} from "react";
import {refreshAccessToken} from "./components/useFetch.jsx";
import { PATHS, paths } from './components/routes.jsx'

import Login from './components/login/login.jsx'
import MainLayout from "./components/layouts/mainLayout.jsx";
import AdminMenu from "./components/adminMenu/adminMenu.jsx";
import RequiredRole from "./components/auth/requiredRole.jsx";
import RequiredAuth from "./components/auth/requiredAuth.jsx";
import Unauthorized from "./components/auth/unauthorized.jsx";
import Home from "./components/home/home.jsx";
import ProductList from "./components/product/productLayout/productList.jsx";
import MissingAuth from "./components/auth/missingAuth.jsx";
import ProductCreation from "./components/product/productCreation/productCreation.jsx";
import CategoryTree from "./components/category/categoryTree/categoryTree.jsx";
import {Register} from "./components/register/register.jsx";


function App() {
    const dispatch = useDispatch();
    const [loading, setLoading] = useState(true); // <— wait state

    useEffect(() => {
        const checkToken = async () => {
            try {
                await refreshAccessToken(dispatch);
            } catch {
                console.log("No valid refresh token found.");
            } finally {
                setLoading(false); // ✅ release UI after check
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
                <Route element={<MissingAuth />}>
                    <Route path={PATHS.login} element={<Login />} />
                    <Route path={PATHS.register} element={<Register />} />

                </Route>
                <Route path={PATHS.unauthorized} element={<Unauthorized />} />

                <Route element={<RequiredAuth />}>
                    <Route element={<MainLayout />}>
                        <Route path={PATHS.home} element={<Home />} />
                    </Route>
                </Route>

                <Route element={<RequiredRole allowed={["ADMIN"]} />}>
                    <Route element={<MainLayout />}>
                        <Route path={PATHS.admin} element={<AdminMenu />} />
                        <Route path={PATHS.productList} element={<ProductList />} />
                        <Route path={PATHS.createProduct} element={<ProductCreation/>} />
                        <Route path={PATHS.categoryTree} element={<CategoryTree/>} />
                    </Route>
                </Route>

                <Route path={PATHS.any} element={<Navigate to={paths.home()} replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
