import {useParams, Link} from "react-router-dom";
import {useSelector} from "react-redux";
import {useState} from "react";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import styles from "./AdminOrders.module.css";
import NotFound from "../error/NotFound.jsx";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const BASE_URL = import.meta.env.VITE_API_BASE;

const STATUSES = [
    "PENDING_PAYMENT", "PAID", "PROCESSING", "SHIPPED", "DELIVERED",
    "PAYMENT_FAILED", "CANCELLED", "REFUNDED"
];

export default function AdminOrderDetail() {
    const {orderId} = useParams();
    const {token} = useSelector((s) => s.auth);
    const {data: order, loading, error, reFetch} = useFetch(`/api/admin/orders/${orderId}`, {withAuth: true});

    const [newStatus, setNewStatus] = useState("");
    const [updating, setUpdating] = useState(false);

    const handleStatusUpdate = async () => {
        if (!newStatus) return;
        setUpdating(true);
        try {
            const res = await fetch(`${BASE_URL}/api/admin/orders/${orderId}/status`, {
                method: "PUT",
                headers: {"Content-Type": "application/json", Authorization: `Bearer ${token}`},
                credentials: "include",
                body: JSON.stringify({status: newStatus}),
            });
            if (res.ok) {
                setNewStatus("");
                await reFetch();
            } else {
                alert("Failed to update status");
            }
        } catch {
            alert("Failed to update status");
        } finally {
            setUpdating(false);
        }
    };

    if (loading) return <div className={styles.page}>Loading...</div>;
    if (error) return <ErrorMessage message="Failed to load order." />;
    if (!order) return <NotFound />;

    return (
        <div className={styles.page}>
            <Helmet><title>Order #{order.id.substring(0, 8)} - Admin</title></Helmet>
            <h1>Order #{order.id.substring(0, 8)}</h1>

            <div className={styles.detailCard}>
                <p>Status: <strong className={styles[`status_${order.status}`]}>{order.status}</strong></p>
                <p>Total: <strong>{Number(order.totalPrice).toFixed(2)} {order.currency}</strong></p>
                {order.shippingMethod && (
                    <p>Shipping: {order.shippingMethod} ({Number(order.shippingCost).toFixed(2)} {order.currency})</p>
                )}
                <p>Placed: {new Date(order.createdAt).toLocaleString()}</p>
                {order.failureReason && <p className={styles.error}>Failure: {order.failureReason}</p>}

                {order.shippingAddress && (
                    <>
                        <h2>Shipping Address</h2>
                        <p>{order.shippingAddress.name}</p>
                        <p>{order.shippingAddress.addressLine1}{order.shippingAddress.addressLine2 ? `, ${order.shippingAddress.addressLine2}` : ""}</p>
                        <p>{order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.zip}</p>
                        <p>{order.shippingAddress.country}</p>
                    </>
                )}

                <h2>Items</h2>
                <ul className={styles.itemList}>
                    {order.items.map((item) => (
                        <li key={item.id}>
                            <Link to={paths.product(item.productId)}>{item.productName}</Link>
                            {" "}x{item.quantity} — {Number(item.price).toFixed(2)} each
                            = {Number(item.totalPrice).toFixed(2)}
                        </li>
                    ))}
                </ul>

                <h2>Update Status</h2>
                <div className={styles.statusUpdate}>
                    <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
                        <option value="">Select new status</option>
                        {STATUSES.filter(s => s !== order.status).map(s => (
                            <option key={s} value={s}>{s}</option>
                        ))}
                    </select>
                    <button onClick={handleStatusUpdate} disabled={!newStatus || updating}>
                        {updating ? "Updating..." : "Update Status"}
                    </button>
                </div>
            </div>

            <div className={styles.links}>
                <Link to={paths.adminOrders()}>Back to All Orders</Link>
            </div>
        </div>
    );
}
