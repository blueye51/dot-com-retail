import {useState} from "react";
import {useSelector} from "react-redux";
import {Link} from "react-router-dom";
import useFetch from "../useFetch.js";
import {paths} from "../routes.js";
import Stars from "../product/productUI/Stars.jsx";
import styles from "./AdminReviews.module.css";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function AdminReviews() {
    const {token} = useSelector((s) => s.auth);
    const [page, setPage] = useState(0);
    const [acting, setActing] = useState(null);

    const {data, loading, error, reFetch} = useFetch(
        `/api/admin/reviews?page=${page}&size=20`,
        {withAuth: true, deps: [page]}
    );

    const reviews = data?.content || [];
    const totalPages = data?.totalPages || 0;

    const handleToggleHidden = async (reviewId) => {
        setActing(reviewId);
        try {
            await fetch(`${BASE_URL}/api/admin/reviews/${reviewId}/toggle-hidden`, {
                method: "POST",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            await reFetch();
        } catch {} finally { setActing(null); }
    };

    const handleDelete = async (reviewId) => {
        if (!confirm("Permanently delete this review?")) return;
        setActing(reviewId);
        try {
            await fetch(`${BASE_URL}/api/admin/reviews/${reviewId}`, {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            await reFetch();
        } catch {} finally { setActing(null); }
    };

    return (
        <div className={styles.page}>
            <Helmet><title>Review Moderation - Admin</title></Helmet>
            <h1>Review Moderation</h1>

            {loading && <p>Loading...</p>}
            {error && <ErrorMessage message="Failed to load reviews." />}
            {!loading && reviews.length === 0 && <p>No reviews found.</p>}

            <div className={styles.reviewList}>
                {reviews.map((r) => (
                    <div key={r.id} className={`${styles.reviewCard} ${r.hidden ? styles.hiddenReview : ""}`}>
                        <div className={styles.reviewHeader}>
                            <strong>{r.userName}</strong>
                            <Stars rating={r.score} />
                            <Link to={paths.product(r.productId)} className={styles.productLink}>
                                {r.productName}
                            </Link>
                            <span className={styles.date}>{new Date(r.createdAt).toLocaleDateString()}</span>
                            {r.hidden && <span className={styles.hiddenBadge}>HIDDEN</span>}
                        </div>
                        <p className={styles.comment}>{r.comment}</p>
                        <div className={styles.meta}>
                            <span>Helpful: {r.helpfulCount}</span>
                        </div>
                        <div className={styles.actions}>
                            <button
                                className={r.hidden ? styles.showBtn : styles.hideBtn}
                                disabled={acting === r.id}
                                onClick={() => handleToggleHidden(r.id)}
                            >
                                {acting === r.id ? "..." : r.hidden ? "Unhide" : "Hide"}
                            </button>
                            <button
                                className={styles.deleteBtn}
                                disabled={acting === r.id}
                                onClick={() => handleDelete(r.id)}
                            >
                                {acting === r.id ? "..." : "Delete"}
                            </button>
                        </div>
                    </div>
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
