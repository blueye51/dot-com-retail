import { generatePath } from "react-router-dom";

export const PATHS = {
    admin: "/admin",
    home: "/",
    login: "/login",
    register: "/register",
    unauthorized: "/unauthorized",
    productList: "/products",
    product: "/product/:id",
    createProduct: "/createProduct",
    categoryTree: "/categoryTree",
    categoryAdmin: "/categoryAdmin",
    oauth2Callback: "/oauth2/callback",
    any: "*",
    profile: "/profile",
    verifyEmail: "/verifyEmail",
    verify2fa: "/verify-2fa",
    brandCreate: "/brandCreate"
};

export const paths = {
    admin: () => PATHS.admin,
    home: () => PATHS.home,
    login: () => PATHS.login,
    register: () => PATHS.register,
    unauthorized: () => PATHS.unauthorized,
    productList: () => PATHS.productList,
    product: (id) => generatePath(PATHS.product, {id}),
    createProduct: () => PATHS.createProduct,
    categoryTree: () => PATHS.categoryTree,
    categoryAdmin: () => PATHS.categoryAdmin,
    oauth2Callback: () => PATHS.oauth2Callback,
    profile: () => PATHS.profile,
    verifyEmail: () => PATHS.verifyEmail,
    verify2fa: () => PATHS.verify2fa,
    brandCreate: () => PATHS.brandCreate,
}