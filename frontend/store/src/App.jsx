import './App.css'
import Login from './components/login/login.jsx'
import MainLayout from "./components/layouts/mainLayout.jsx";
import AdminMenu from "./components/adminMenu/adminMenu.jsx";
import RequiredRole from "./components/auth/requiredRole.jsx";
import RequiredAuth from "./components/auth/requiredAuth.jsx";
import Unauthorized from "./components/auth/unauthorized.jsx";
import Home from "./components/home/home.jsx";
import ProductList from "./components/product/productLayout/productList.jsx";

import {BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import {useDispatch} from "react-redux";
import {useEffect, useState} from "react";
import {refreshAccessToken} from "./components/useFetch.jsx";
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
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                </Route>
                <Route path="/unauthorized" element={<Unauthorized />} />

                <Route element={<RequiredAuth />}>
                    <Route element={<MainLayout />}>
                        <Route path="/" element={<Home />} />
                    </Route>
                </Route>

                <Route element={<RequiredRole allowed={["ADMIN"]} />}>
                    <Route element={<MainLayout />}>
                        <Route path="/admin" element={<AdminMenu />} />
                        <Route path="/product" element={<ProductList />} />
                        <Route path="/createproduct" element={<ProductCreation/>} />
                        <Route path="/categorytree" element={<CategoryTree/>} />
                    </Route>
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
