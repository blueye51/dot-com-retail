import useFetch from "../../useFetch.js";
import {useEffect, useMemo, useState} from "react";
import {useSearchParams, Link} from "react-router-dom";
import styles from "./ProductGrid.module.css"
import {ProductCard} from "../productUI/ProductCard.jsx";
import CategoryTree from "../../category/categoryTree/CategoryTree.jsx";
import Stars from "../productUI/Stars.jsx";
import defaultImage from "../../../assets/default_image.png";
import {useDispatch, useSelector} from "react-redux";
import {addToGuestCart, setCart} from "../../store.js";
import {paths} from "../../routes.js";

const BASE_URL = import.meta.env.VITE_API_BASE;


function ProductListRow({ id, name, price, currency, brand, stock, averageRating, totalRatings, imageUrl }) {
    const dispatch = useDispatch();
    const { token } = useSelector((s) => s.auth);
    const [adding, setAdding] = useState(false);

    const handleAddToCart = async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (token) {
            setAdding(true);
            try {
                const res = await fetch(`${BASE_URL}/api/cart`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
                    credentials: "include",
                    body: JSON.stringify({ productId: id, quantity: 1 }),
                });
                if (res.ok) dispatch(setCart(await res.json()));
            } catch {} finally { setAdding(false); }
        } else {
            dispatch(addToGuestCart({ productId: id, productName: name, price, currency, quantity: 1, stock, imageUrl }));
        }
    };

    return (
        <Link to={paths.product(id)} className={styles.listRow}>
            <img className={styles.listThumb} src={imageUrl || defaultImage} alt={name} loading="lazy" />
            <div className={styles.listInfo}>
                <h3 className={styles.listName}>{name}</h3>
                {brand && <span className={styles.listBrand}>{brand}</span>}
                <Stars rating={averageRating} count={totalRatings} />
            </div>
            <div className={styles.listRight}>
                <span className={styles.listPrice}>{Number(price).toFixed(2)} {currency}</span>
                <span className={stock > 0 ? styles.inStock : styles.outOfStock}>
                    {stock > 0 ? "In Stock" : "Out of Stock"}
                </span>
                <button
                    className={styles.listCartBtn}
                    disabled={stock <= 0 || adding}
                    onClick={handleAddToCart}
                >
                    {adding ? "Adding..." : "Add to Cart"}
                </button>
            </div>
        </Link>
    );
}

export default function ProductGrid() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [view, setView] = useState("grid");

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
                <div className={styles.toolbar}>
                    <span className={styles.resultCount}>{loading ? "" : `${products.length} product${products.length !== 1 ? "s" : ""}`}</span>
                    <div className={styles.viewToggle}>
                        <button
                            className={`${styles.toggleBtn} ${view === "grid" ? styles.toggleActive : ""}`}
                            onClick={() => setView("grid")}
                            title="Grid view"
                        >⊞</button>
                        <button
                            className={`${styles.toggleBtn} ${view === "list" ? styles.toggleActive : ""}`}
                            onClick={() => setView("list")}
                            title="List view"
                        >☰</button>
                    </div>
                </div>

                {loading ? (
                    <div>Loading...</div>
                ) : products.length === 0 ? (
                    <div>No products found</div>
                ) : view === "grid" ? (
                    <div className={styles.grid}>
                        {products.map((p) => <ProductCard key={p.id} {...p} />)}
                    </div>
                ) : (
                    <div className={styles.list}>
                        {products.map((p) => <ProductListRow key={p.id} {...p} />)}
                    </div>
                )}

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
