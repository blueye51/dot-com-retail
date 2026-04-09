import { Link, useSearchParams } from "react-router-dom";
import useFetch from "../../useFetch.js";
import {useEffect, useMemo, useState} from "react";
import {useSelector} from "react-redux";
import {paths} from "../../routes.js";
import styles from "./ProductList.module.css";
import ProductRow from "../productUI/productRow/ProductRow.jsx";
import ErrorMessage from "../../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const BASE_URL = import.meta.env.VITE_API_BASE;

function ProductList () {
    const {token} = useSelector((s) => s.auth);
    const [pageSize] = useState(100);
    const [pageNumber] = useState(0);
    const [searchParams] = useSearchParams();
    const search = searchParams.get("search") ?? "";

    const url = useMemo(() => {
        const params = new URLSearchParams();
        params.set("page", pageNumber);
        params.set("size", pageSize);
        if (search) params.set("search", search);
        return `/api/products/page?${params}`;
    }, [pageNumber, pageSize, search]);

    const { data, error, loading, reFetch } = useFetch(url, {});
    const products = data?.content ?? [];

    const [deleting, setDeleting] = useState(null);

    useEffect(() => {
        if (!error) return;
        console.error("Failed to fetch products:", error);
    }, [error]);

    const handleDelete = async (e, productId, productName) => {
        e.preventDefault();
        e.stopPropagation();
        if (!confirm(`Delete "${productName}"? This cannot be undone.`)) return;

        setDeleting(productId);
        try {
            const res = await fetch(`${BASE_URL}/api/products/${productId}`, {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            if (res.ok) {
                await reFetch();
            } else {
                alert("Failed to delete product");
            }
        } catch {
            alert("Failed to delete product");
        } finally {
            setDeleting(null);
        }
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <ErrorMessage message="Failed to load products." />;

    return (
        <div className={styles.list}>
            <Helmet><title>Product List - Admin</title></Helmet>
            {products.map((p) => (
                <div key={p.id} className={styles.productRow}>
                    <Link to={paths.product(p.id)} className={styles.link}>
                        <ProductRow product={p} />
                    </Link>
                    <div className={styles.actions}>
                        <Link to={paths.editProduct(p.id)} className={styles.editBtn}>Edit</Link>
                        <button
                            className={styles.deleteBtn}
                            disabled={deleting === p.id}
                            onClick={(e) => handleDelete(e, p.id, p.name)}
                        >
                            {deleting === p.id ? "..." : "Delete"}
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
}

export default ProductList;
