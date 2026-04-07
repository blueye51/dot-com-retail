import styles from './Order.module.css';
import {useParams, useSearchParams, Link} from "react-router-dom";
import {useSelector, useDispatch} from "react-redux";
import {useEffect} from "react";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import {clearCart} from "../store.js";

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
    if (error) return <div className={styles.page}>Failed to load order.</div>;
    if (!order) return <div className={styles.page}>Order not found.</div>;

    return (
        <div className={styles.page}>
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
