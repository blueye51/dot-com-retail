import {useState} from "react";
import {Link} from "react-router-dom";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import styles from "./AdminOrders.module.css";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const STATUSES = [
    "PENDING_PAYMENT", "PAID", "PROCESSING", "SHIPPED", "DELIVERED",
    "PAYMENT_FAILED", "CANCELLED", "REFUNDED"
];

export default function AdminOrders() {
    const [status, setStatus] = useState("");
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");
    const [page, setPage] = useState(0);

    const statusParam = status ? `&status=${status}` : "";
    const fromParam = from ? `&from=${from}` : "";
    const toParam = to ? `&to=${to}` : "";

    const {data, loading, error} = useFetch(
        `/api/admin/orders?page=${page}&size=20${statusParam}${fromParam}${toParam}`,
        {withAuth: true, deps: [page, status, from, to]}
    );

    const orders = data?.content || [];
    const totalPages = data?.totalPages || 0;

    return (
        <div className={styles.page}>
            <Helmet><title>Order Management - Admin</title></Helmet>
            <h1>Order Management</h1>

            <div className={styles.filters}>
                <select value={status} onChange={(e) => {setStatus(e.target.value); setPage(0);}}>
                    <option value="">All Statuses</option>
                    {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
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
            {error && <ErrorMessage message="Failed to load orders." />}
            {!loading && orders.length === 0 && <p>No orders found.</p>}

            <table className={styles.table}>
                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Status</th>
                        <th>Total</th>
                        <th>Items</th>
                        <th>Date</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {orders.map((order) => (
                        <tr key={order.id}>
                            <td>#{order.id.substring(0, 8)}</td>
                            <td>
                                <span className={styles.customerName}>{order.userName}</span>
                                <br/>
                                <span className={styles.customerEmail}>{order.userEmail}</span>
                            </td>
                            <td>
                                <span className={styles[`status_${order.status}`]}>{order.status}</span>
                            </td>
                            <td>{Number(order.totalPrice).toFixed(2)} {order.currency}</td>
                            <td>{order.itemCount}</td>
                            <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                            <td>
                                <Link to={paths.adminOrder(order.id)} className={styles.viewBtn}>
                                    View
                                </Link>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

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
