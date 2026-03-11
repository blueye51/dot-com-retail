import { Link, useSearchParams } from "react-router-dom";
import useFetch from "../../useFetch.js";
import {useEffect, useMemo, useState} from "react";
import {paths} from "../../routes.js";
import styles from "./productList.module.css";
import ProductRow from "../productUI/productRow/ProductRow.jsx";

function ProductList () {

// Product object{
//     name: string, (req.)
//     price: number, (req.)
//     currency: string, (e.g., "USD", "EUR") (req.)
//     description: string,
//     width: number,
//     height: number,
//     depth: number,
//     weight: number,
//     stock: number, (req.)
//     categoryId: string, (req.)
//     String brand,
//     images:[{
//         fileKey: string,
//         sortOrder: number
//     }...]
// }
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

    const { data, error, loading } = useFetch(url, {});
    const products = data?.content ?? [];

    useEffect(() => {
        if (!error) return;
        console.error("Failed to fetch products:", error);
        alert(`Failed to fetch products: ${error.message ?? "Unknown error"}`);
    }, [error]);

    if (loading) return <div>Loading…</div>;
    if (error) return <div>Failed to load.</div>;

    return (
        <div className={styles.list}>
            {products.map((p) => (
                <Link key={p.id} to={paths.product(p.id)} className={styles.link}>
                    <ProductRow product={p} />
                </Link>
            ))}
        </div>
    );
}

export default ProductList;