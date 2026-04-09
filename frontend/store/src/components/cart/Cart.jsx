import styles from './Cart.module.css';
import {useSelector, useDispatch} from "react-redux";
import {useCallback, useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {paths} from "../routes.js";
import useFetch from "../useFetch.js";
import {setCart, updateGuestCartQuantity, removeFromGuestCart} from "../store.js";
import defaultImage from "../../assets/default_image.png";
import {ProductCard} from "../product/productUI/ProductCard.jsx";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function Cart() {
    const dispatch = useDispatch();
    const {token} = useSelector((s) => s.auth);
    const cart = useSelector((s) => s.cart);
    const navigate = useNavigate();
    const [actionLoading, setActionLoading] = useState(false);
    const [related, setRelated] = useState([]);

    const {data, loading, error, reFetch} = useFetch("/api/cart", {
        withAuth: true,
        immediate: false,
    });

    useEffect(() => {
        if (token) reFetch().catch(() => {});
    }, [token]);

    useEffect(() => {
        if (data) dispatch(setCart(data));
    }, [data, dispatch]);

    useEffect(() => {
        const productIds = cart.items.map((i) => i.productId);
        if (productIds.length === 0) { setRelated([]); return; }
        const params = productIds.map((id) => `productIds=${id}`).join("&");
        fetch(`${BASE_URL}/api/products/related?${params}&limit=8`)
            .then((res) => res.ok ? res.json() : [])
            .then(setRelated)
            .catch(() => setRelated([]));
    }, [cart.items]);

    const authFetch = useCallback(async (url, method) => {
        const res = await fetch(`${BASE_URL}${url}`, {
            method,
            headers: {Authorization: `Bearer ${token}`},
            credentials: "include",
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        if (res.status === 204) return null;
        return res.json();
    }, [token]);

    const handleQuantityChange = useCallback(async (item, newQty) => {
        if (newQty < 1) return;
        if (token) {
            setActionLoading(true);
            try {
                const result = await authFetch(`/api/cart/${item.id}?quantity=${newQty}`, "PUT");
                if (result) dispatch(setCart(result));
            } catch {} finally {
                setActionLoading(false);
            }
        } else {
            dispatch(updateGuestCartQuantity({productId: item.productId, quantity: newQty}));
        }
    }, [token, authFetch, dispatch]);

    const handleRemove = useCallback(async (item) => {
        if (token) {
            setActionLoading(true);
            try {
                await authFetch(`/api/cart/${item.id}`, "DELETE");
                const result = await reFetch();
                if (result) dispatch(setCart(result));
            } catch {} finally {
                setActionLoading(false);
            }
        } else {
            dispatch(removeFromGuestCart(item.productId));
        }
    }, [token, authFetch, reFetch, dispatch]);

    if (loading) return <div className={styles.page}>Loading...</div>;
    if (error && token) return <ErrorMessage message="Failed to load cart." />;

    const items = cart.items;
    const total = cart.total;

    if (!items || items.length === 0) {
        return (
            <div className={styles.page}>
                <h1>Your Cart</h1>
                <p>Your cart is empty.</p>
                <Link to={paths.home()}>Continue Shopping</Link>
            </div>
        );
    }

    return (
        <div className={styles.page}>
            <Helmet>
                <title>Cart - Electronics Store</title>
                <meta name="description" content="Review items in your shopping cart and proceed to checkout." />
            </Helmet>
            <h1>Your Cart</h1>
            <div className={styles.cartLayout}>
                <div className={styles.itemList}>
                    {items.map((item) => (
                        <div key={item.productId} className={styles.cartItem}>
                            <Link to={paths.product(item.productId)}>
                                <img
                                    className={styles.thumbnail}
                                    src={item.imageUrl || defaultImage}
                                    alt={item.productName}
                                />
                            </Link>
                            <div className={styles.itemInfo}>
                                <Link to={paths.product(item.productId)} className={styles.itemName}>
                                    {item.productName}
                                </Link>
                                <p className={styles.itemPrice}>{Number(item.price).toFixed(2)} {item.currency}</p>
                                {item.stock != null && item.stock <= 0 && (
                                    <p className={styles.outOfStock}>Out of Stock</p>
                                )}
                            </div>
                            <div className={styles.quantityControls}>
                                <button onClick={() => handleQuantityChange(item, item.quantity - 1)}
                                        disabled={item.quantity <= 1 || actionLoading}>-
                                </button>
                                <span>{item.quantity}</span>
                                <button onClick={() => handleQuantityChange(item, item.quantity + 1)}
                                        disabled={actionLoading}>+
                                </button>
                            </div>
                            <p className={styles.lineTotal}>
                                {(Number(item.price) * item.quantity).toFixed(2)} {item.currency}
                            </p>
                            <button className={styles.removeBtn} onClick={() => handleRemove(item)}
                                    disabled={actionLoading}>Remove
                            </button>
                        </div>
                    ))}
                </div>
                <div className={styles.summary}>
                    <h2>Order Summary</h2>
                    <div className={styles.summaryRow}>
                        <span>Total</span>
                        <span>{Number(total).toFixed(2)}</span>
                    </div>
                    <button className={styles.checkoutBtn}
                            disabled={!token}
                            onClick={() => navigate(paths.checkout())}>
                        {token ? "Checkout" : "Login to Checkout"}
                    </button>
                </div>
            </div>

            {related.length > 0 && (
                <section className={styles.relatedSection}>
                    <h2>You Might Also Like</h2>
                    <div className={styles.relatedScroll}>
                        {related.map((p) => (
                            <div key={p.id} className={styles.relatedCard}>
                                <ProductCard {...p} />
                            </div>
                        ))}
                    </div>
                </section>
            )}
        </div>
    );
}
