import useFetch from "../../useFetch.js";
import {useEffect, useMemo, useState} from "react";
import {useSearchParams} from "react-router-dom";
import styles from "./productGrid.module.css"
import {ProductCard} from "../productUI/productCard.jsx";
import CategoryTree from "../../category/categoryTree/categoryTree.jsx";


export default function ProductGrid() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [pageSize, setPageSize] = useState(24);
    const [pageNumber, setPageNumber] = useState(0);
    const sort = searchParams.get("sort") ?? "createdAt";
    const descending = searchParams.get("descending") !== "false";
    const search = searchParams.get("search") ?? "";
    const categoryId = searchParams.get("categoryId") ?? "";

    const url = useMemo(() => {
        const params = new URLSearchParams({
            page: String(pageNumber),
            size: String(pageSize),
            ...(sort && { sort }),
            ...(descending && { descending: String(descending) }),
            ...(search && { search }),
            ...(categoryId && { categoryId }),
        });
        return `/api/products/page?${params}`;
    }, [pageNumber, pageSize, search, sort, descending, categoryId]);

    const [products, setProducts] = useState([])

    const { data, loading, reFetch, error } = useFetch(url, {});

    useEffect(() => {
        if (data) setProducts(data.content);
    }, [data]);

    const handleFilterChange = (key, value) => {
        setPageNumber(0);
        setSearchParams(prev => {
            const next = new URLSearchParams(prev);
            if (value) {
                next.set(key, value);
            } else {
                next.delete(key);
            }
            return next;
        });
    };

    return (
        <div className={styles.main}>
            <div className={styles.sidebar}>
                <CategoryTree onSelect={(id) => handleFilterChange("categoryId", id)} />
            </div>
            <div className={styles.grid}>
                {products.length !== 0
                    ? products.map((p) => <ProductCard key={p.id} {...p}/>)
                    : "no products"
                }

            </div>
        </div>
    )
}