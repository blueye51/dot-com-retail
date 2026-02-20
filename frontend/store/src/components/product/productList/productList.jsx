import { Link } from "react-router-dom";
import useFetch from "../../useFetch.jsx";
import {useEffect, useMemo, useState} from "react";
import {paths} from "../../routes.jsx";
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
//     images:[{
//         fileKey: string,
//         sortOrder: number
//     }...]
// }
    const [pageSize] = useState(100);
    const [pageNumber] = useState(0);

    const url = useMemo(
        () => `/api/products/page?page=${pageNumber}&size=${pageSize}`,
        [pageNumber, pageSize]
    );

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