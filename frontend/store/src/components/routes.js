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
    oauth2Callback: "/oauth2/callback",
    any: "*",
    profile: "/profile"
};

export const paths = {
    admin: () => PATHS.admin,
    home: () => PATHS.home,
    login: () => PATHS.login,
    register: () => PATHS.register,
    unauthorized: () => PATHS.unauthorized,
    productList: () => PATHS.productList,
    product: (id) => generatePath(PATHS.product, { id }),
    createProduct: () => PATHS.createProduct,
    categoryTree: () => PATHS.categoryTree,
    oauth2Callback: () => PATHS.oauth2Callback,
    profile: () => PATHS.profile,
};