import styles from './Order.module.css';
import {useState} from "react";
import {Link} from "react-router-dom";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";

export default function Orders() {
    const [status, setStatus] = useState("");
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");
    const [page, setPage] = useState(0);

    const statusParam = status ? `&status=${status}` : "";
    const fromParam = from ? `&from=${from}` : "";
    const toParam = to ? `&to=${to}` : "";
    const {data, loading, error} = useFetch(`/api/orders?page=${page}&size=10${statusParam}${fromParam}${toParam}`, {
        withAuth: true,
        deps: [page, status, from, to],
    });

    const orders = data?.content || [];
    const totalPages = data?.totalPages || 0;

    return (
        <div className={styles.page}>
            <h1>My Orders</h1>

            <div className={styles.filters}>
                <select value={status} onChange={(e) => {setStatus(e.target.value); setPage(0);}}>
                    <option value="">All Statuses</option>
                    <option value="PENDING_PAYMENT">Pending</option>
                    <option value="PAID">Paid</option>
                    <option value="PAYMENT_FAILED">Failed</option>
                    <option value="CANCELLED">Cancelled</option>
                    <option value="REFUNDED">Refunded</option>
                </select>
                <label>
                    From
                    <input type="date" value={from} onChange={(e) => {setFrom(e.target.value); setPage(0);}}/>
                </label>
                <label>
                    To
                    <input type="date" value={to} onChange={(e) => {setTo(e.target.value); setPage(0);}}/>
                </label>
            </div>

            {loading && <p>Loading...</p>}
            {error && <p className={styles.error}>Failed to load orders.</p>}

            {!loading && orders.length === 0 && <p>No orders found.</p>}

            <div className={styles.orderList}>
                {orders.map((order) => (
                    <Link to={paths.order(order.id)} key={order.id} className={styles.orderCard}>
                        <div className={styles.orderHeader}>
                            <span>#{order.id.substring(0, 8)}</span>
                            <span className={styles[`status_${order.status}`]}>{order.status}</span>
                        </div>
                        <p>{Number(order.totalPrice).toFixed(2)} {order.currency}</p>
                        <p className={styles.date}>{new Date(order.createdAt).toLocaleDateString()}</p>
                        <p className={styles.itemCount}>{order.itemCount} item{order.itemCount !== 1 ? "s" : ""}</p>
                    </Link>
                ))}
            </div>

            {totalPages > 1 && (
                <div className={styles.pagination}>
                    <button onClick={() => setPage(p => p - 1)} disabled={page === 0}>Previous</button>
                    <span>Page {page + 1} of {totalPages}</span>
                    <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>Next</button>
                </div>
            )}
        </div>
    );
}
