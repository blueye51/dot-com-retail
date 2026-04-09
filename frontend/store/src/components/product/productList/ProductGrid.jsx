import useFetch from "../../useFetch.js";
import {useEffect, useMemo, useState} from "react";
import {useSearchParams} from "react-router-dom";
import styles from "./ProductGrid.module.css"
import {ProductCard} from "../productUI/ProductCard.jsx";
import CategoryTree from "../../category/categoryTree/CategoryTree.jsx";


export default function ProductGrid() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [pageSize, setPageSize] = useState(24);
    const [pageNumber, setPageNumber] = useState(0);
    const sort = searchParams.get("sort") ?? "createdAt";
    const descending = searchParams.get("descending") !== "false";
    const search = searchParams.get("search") ?? "";
    const categoryId = searchParams.get("categoryId") ?? "";
    const brandId = searchParams.get("brandId") ?? "";
    const minPrice = searchParams.get("minPrice") ?? "";
    const maxPrice = searchParams.get("maxPrice") ?? "";

    const [searchInput, setSearchInput] = useState(search);
    const [minPriceInput, setMinPriceInput] = useState(minPrice);
    const [maxPriceInput, setMaxPriceInput] = useState(maxPrice);

    const url = useMemo(() => {
        const params = new URLSearchParams({
            page: String(pageNumber),
            size: String(pageSize),
            ...(sort && { sort }),
            descending: String(descending),
            ...(search && { search }),
            ...(categoryId && { categoryId }),
            ...(brandId && { brandId }),
            ...(minPrice && { minPrice }),
            ...(maxPrice && { maxPrice }),
        });
        return `/api/products/page?${params}`;
    }, [pageNumber, pageSize, search, sort, descending, categoryId, brandId, minPrice, maxPrice]);

    const [products, setProducts] = useState([])
    const [totalPages, setTotalPages] = useState(0);

    const { data, loading, reFetch, error } = useFetch(url, {});

    const { data: brandsData } = useFetch("/api/brands", {});

    useEffect(() => {
        if (data) {
            setProducts(data.content);
            setTotalPages(data.totalPages);
        }
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

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        handleFilterChange("search", searchInput.trim());
    };

    const handlePriceApply = () => {
        setPageNumber(0);
        setSearchParams(prev => {
            const next = new URLSearchParams(prev);
            if (minPriceInput) next.set("minPrice", minPriceInput);
            else next.delete("minPrice");
            if (maxPriceInput) next.set("maxPrice", maxPriceInput);
            else next.delete("maxPrice");
            return next;
        });
    };

    return (
        <div className={styles.main}>
            <div className={styles.sidebar}>
                <form onSubmit={handleSearchSubmit} className={styles.searchForm}>
                    <input
                        type="text"
                        placeholder="Search products..."
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        className={styles.searchInput}
                    />
                    <button type="submit" className={styles.searchButton}>Search</button>
                </form>

                <div className={styles.filterSection}>
                    <h2 className={styles.filterTitle}>Sort By</h2>
                    <select
                        value={sort}
                        onChange={(e) => handleFilterChange("sort", e.target.value)}
                        className={styles.select}
                    >
                        <option value="createdAt">Newest</option>
                        <option value="name">Name</option>
                        <option value="price">Price</option>
                        <option value="averageRating">Rating</option>
                        <option value="viewCount">Relevance</option>
                    </select>
                    <label className={styles.checkboxLabel}>
                        <input
                            type="checkbox"
                            checked={descending}
                            onChange={(e) => handleFilterChange("descending", e.target.checked ? "true" : "false")}
                        />
                        Descending
                    </label>
                </div>

                <div className={styles.filterSection}>
                    <h2 className={styles.filterTitle}>Brand</h2>
                    <select
                        value={brandId}
                        onChange={(e) => handleFilterChange("brandId", e.target.value)}
                        className={styles.select}
                    >
                        <option value="">All Brands</option>
                        {brandsData && brandsData.map((b) => (
                            <option key={b.id} value={b.id}>{b.name}</option>
                        ))}
                    </select>
                </div>

                <div className={styles.filterSection}>
                    <h2 className={styles.filterTitle}>Price Range</h2>
                    <div className={styles.priceInputs}>
                        <input
                            type="number"
                            placeholder="Min"
                            value={minPriceInput}
                            onChange={(e) => setMinPriceInput(e.target.value)}
                            className={styles.priceInput}
                            min="0"
                        />
                        <span>-</span>
                        <input
                            type="number"
                            placeholder="Max"
                            value={maxPriceInput}
                            onChange={(e) => setMaxPriceInput(e.target.value)}
                            className={styles.priceInput}
                            min="0"
                        />
                    </div>
                    <button onClick={handlePriceApply} className={styles.applyButton}>Apply</button>
                </div>

                <div className={styles.filterSection}>
                    <div className={styles.filterHeader}>
                        <h2 className={styles.filterTitle}>Category</h2>
                        {categoryId && (
                            <button
                                onClick={() => handleFilterChange("categoryId", "")}
                                className={styles.clearButton}
                            >
                                Clear
                            </button>
                        )}
                    </div>
                    <CategoryTree onSelect={(id) => handleFilterChange("categoryId", id)} />
                </div>
            </div>

            <div className={styles.content}>
                <div className={styles.grid}>
                    {loading
                        ? "Loading..."
                        : products.length !== 0
                            ? products.map((p) => <ProductCard key={p.id} {...p}/>)
                            : "No products found"
                    }
                </div>

                {totalPages > 1 && (
                    <div className={styles.pagination}>
                        <button
                            disabled={pageNumber === 0}
                            onClick={() => setPageNumber(p => p - 1)}
                            className={styles.pageButton}
                        >
                            Previous
                        </button>
                        <span>Page {pageNumber + 1} of {totalPages}</span>
                        <button
                            disabled={pageNumber >= totalPages - 1}
                            onClick={() => setPageNumber(p => p + 1)}
                            className={styles.pageButton}
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>
        </div>
    )
}
