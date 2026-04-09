import styles from './Order.module.css';
import {useParams, useSearchParams, Link} from "react-router-dom";
import {useSelector, useDispatch} from "react-redux";
import {useEffect} from "react";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import {clearCart} from "../store.js";
import NotFound from "../error/NotFound.jsx";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

function deliveryEstimate(shippingMethod) {
    if (!shippingMethod) return null;
    const m = shippingMethod.toLowerCase();
    if (m.includes("express")) return "2–3 business days";
    if (m.includes("standard")) return "5–7 business days";
    return null;
}

export default function OrderConfirmation() {
    const {orderId} = useParams();
    const [searchParams] = useSearchParams();
    const dispatch = useDispatch();
    const redirectStatus = searchParams.get("redirect_status");

    const {data: order, loading, error} = useFetch(`/api/orders/${orderId}`, {
        withAuth: true,
    });

    useEffect(() => {
        dispatch(clearCart());
    }, [dispatch]);

    if (loading) return <div className={styles.page}>Loading...</div>;
    if (error) return <ErrorMessage message="Failed to load order." />;
    if (!order) return <NotFound />;

    return (
        <div className={styles.page}>
            <Helmet><title>Order Confirmation - Electronics Store</title></Helmet>
            {redirectStatus === "succeeded" ? (
                <>
                    <h1>Payment Successful!</h1>
                    <p>Thank you for your order.</p>
                </>
            ) : (
                <>
                    <h1>Order Placed</h1>
                    <p>Payment status: {redirectStatus || order.status}</p>
                </>
            )}

            <div className={styles.orderCard}>
                <h2>Order #{order.id.substring(0, 8)}</h2>
                <p>Status: <strong>{order.status}</strong></p>
                <p>Total: <strong>{Number(order.totalPrice).toFixed(2)} {order.currency}</strong></p>
                {order.shippingMethod && (
                    <p>Shipping: {order.shippingMethod}</p>
                )}
                {deliveryEstimate(order.shippingMethod) && (
                    <p>Estimated delivery: <strong>{deliveryEstimate(order.shippingMethod)}</strong></p>
                )}

                <h3>Items</h3>
                <ul className={styles.itemList}>
                    {order.items.map((item) => (
                        <li key={item.id}>
                            {item.productName} x{item.quantity} — {Number(item.totalPrice).toFixed(2)}
                        </li>
                    ))}
                </ul>
            </div>

            <div className={styles.links}>
                <Link to={paths.orders()}>View All Orders</Link>
                <Link to={paths.home()}>Continue Shopping</Link>
            </div>
        </div>
    );
}
