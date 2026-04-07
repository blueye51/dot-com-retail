import styles from './Order.module.css';
import {useParams, Link} from "react-router-dom";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import {useSelector} from "react-redux";
import {useState} from "react";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function OrderDetail() {
    const {orderId} = useParams();
    const {token} = useSelector((s) => s.auth);
    const {data: order, loading, error, reFetch} = useFetch(`/api/orders/${orderId}`, {
        withAuth: true,
    });
    const [cancelLoading, setCancelLoading] = useState(false);

    const handleCancel = async () => {
        setCancelLoading(true);
        try {
            const res = await fetch(`${BASE_URL}/api/orders/${orderId}/cancel`, {
                method: "POST",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            if (res.ok) {
                reFetch().catch(() => {});
            }
        } catch {} finally {
            setCancelLoading(false);
        }
    };

    if (loading) return <div className={styles.page}>Loading...</div>;
    if (error) return <div className={styles.page}>Failed to load order.</div>;
    if (!order) return <div className={styles.page}>Order not found.</div>;

    return (
        <div className={styles.page}>
            <h1>Order #{order.id.substring(0, 8)}</h1>

            <div className={styles.orderCard}>
                <p>Status: <strong className={styles[`status_${order.status}`]}>{order.status}</strong></p>
                <p>Total: <strong>{Number(order.totalPrice).toFixed(2)} {order.currency}</strong></p>
                {order.shippingMethod && (
                    <p>Shipping: {order.shippingMethod} ({Number(order.shippingCost).toFixed(2)} {order.currency})</p>
                )}
                <p>Placed: {new Date(order.createdAt).toLocaleString()}</p>
                {order.failureReason && <p className={styles.error}>Failure: {order.failureReason}</p>}

                {order.shippingAddress && (
                    <>
                        <h3>Shipping Address</h3>
                        <p>{order.shippingAddress.name}</p>
                        <p>{order.shippingAddress.addressLine1}{order.shippingAddress.addressLine2 ? `, ${order.shippingAddress.addressLine2}` : ""}</p>
                        <p>{order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.zip}</p>
                        <p>{order.shippingAddress.country}</p>
                    </>
                )}

                <h3>Items</h3>
                <ul className={styles.itemList}>
                    {order.items.map((item) => (
                        <li key={item.id}>
                            <Link to={paths.product(item.productId)}>{item.productName}</Link>
                            {" "}x{item.quantity} — {Number(item.price).toFixed(2)} each
                            = {Number(item.totalPrice).toFixed(2)}
                        </li>
                    ))}
                </ul>

                {(order.status === "PENDING_PAYMENT" || order.status === "PAID") && (
                    <button onClick={handleCancel} disabled={cancelLoading} className={styles.cancelBtn}>
                        {cancelLoading
                            ? (order.status === "PAID" ? "Refunding..." : "Cancelling...")
                            : (order.status === "PAID" ? "Cancel & Refund" : "Cancel Order")}
                    </button>
                )}
            </div>

            <div className={styles.links}>
                <Link to={paths.orders()}>Back to Orders</Link>
            </div>
        </div>
    );
}
