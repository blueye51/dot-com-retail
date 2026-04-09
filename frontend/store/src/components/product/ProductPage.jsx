import {useParams} from "react-router-dom";
import useFetch from "../useFetch.js";
import styles from "./ProductPage.module.css"
import Modal from "../modal/Modal.jsx";
import {useState} from "react";
import {useSelector, useDispatch} from "react-redux";
import defaultImage from "../../assets/default_image.png";
import Stars from "./productUI/Stars.jsx";
import {formatDimension, kgToLb} from "../units.js";
import {addToGuestCart, setCart} from "../store.js";
import NotFound from "../error/NotFound.jsx";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";


export default function ProductPage() {
    const {id} = useParams();
    const dispatch = useDispatch();
    const {data: product, loading, error} = useFetch(`/api/products/${id}`);
    const {data: reviews, reFetch: refreshReviews} = useFetch(`/api/ratings/product/${id}/reviews`, {withAuth: true});
    const imperial = useSelector((s) => s.settings.imperialUnits);
    const {token} = useSelector((s) => s.auth);

    const [imageOpen, setImageOpen] = useState(false)
    const [ratingOpen, setRatingOpen] = useState(false);
    const [score, setScore] = useState(5);
    const [comment, setComment] = useState("");
    const [votingId, setVotingId] = useState(null);

    const {reFetch: addToCartApi, loading: cartLoading} = useFetch("/api/cart", {
        method: "POST",
        withAuth: true,
        immediate: false,
    });

    const {reFetch: submitRating, loading: ratingLoading, error: ratingError} = useFetch("/api/ratings", {
        method: "POST",
        withAuth: true,
        immediate: false,
    });

    const BASE_URL = import.meta.env.VITE_API_BASE;

    const handleVote = async (ratingId) => {
        if (!token) return;
        setVotingId(ratingId);
        try {
            await fetch(`${BASE_URL}/api/ratings/${ratingId}/helpful`, {
                method: "POST",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            await refreshReviews();
        } catch {} finally { setVotingId(null); }
    };

    const closeImage = () => {
        setImageOpen(false)
    }

    if (loading) return <div className={styles.page}>Loading…</div>;
    if (error) return <ErrorMessage message="Failed to load product." />;
    if (!product) return <NotFound />;

    const renderImage = () => {
        const images = product.images

        if (images.length === 0){
            return(
                <img
            className={styles.image}
            src={defaultImage}
            alt={product.name}
            />
                )
        }

        return (
            <>
                <img
                    className={styles.image}
                    src={images[0].url}
                    alt={product.name}
                    onClick={() => setImageOpen(true)}
                />
                <Modal open={imageOpen} onClose={closeImage}>
                    <img
                        className={styles.image}
                        src={images[0].url}
                        alt={product.name}
                        loading="lazy"
                    />
                </Modal>
            </>
        )
    }

    return (
        <div className={styles.page}>
            <Helmet>
                <title>{product.name} - Electronics Store</title>
                <meta name="description" content={`Buy ${product.name} for ${product.price.toFixed(2)} ${product.currency}. ${product.description?.substring(0, 120) || ""}`} />
            </Helmet>
            <div className={styles.product}>
                <div className={styles.media}>
                    {renderImage()}
                </div>

                <div className={styles.info}>
                    <h1>{product.name}</h1>
                    <p>{product.brand}</p>
                    <Stars rating={product.averageRating} count={product.totalRatings} />

                    <div>
                        <span>{product.price.toFixed(2)} {product.currency}</span>
                        <p>{product.stock > 0 ? `In Stock: ${product.stock}` : "Out of Stock"}</p>
                        <button disabled={product.stock <= 0 || cartLoading} onClick={async () => {
                            if (token) {
                                try {
                                    const result = await addToCartApi({body: {productId: id, quantity: 1}});
                                    if (result) dispatch(setCart(result));
                                } catch {}
                            } else {
                                const imageUrl = product.images?.length > 0 ? product.images[0].url : null;
                                dispatch(addToGuestCart({
                                    productId: id,
                                    productName: product.name,
                                    price: product.price,
                                    currency: product.currency,
                                    quantity: 1,
                                    stock: product.stock,
                                    imageUrl,
                                }));
                            }
                        }}>
                            {cartLoading ? "Adding..." : "Add to Cart"}
                        </button>
                    </div>

                    <p>{product.description}</p>

                    <button onClick={() => setRatingOpen(true)}>Rate this product</button>
                    <Modal open={ratingOpen} onClose={() => setRatingOpen(false)}>
                        <form className={styles.ratingForm} onSubmit={async (e) => {
                            e.preventDefault();
                            try {
                                await submitRating({body: {productId: id, score, comment}});
                                setRatingOpen(false);
                                setScore(5);
                                setComment("");
                                await refreshReviews();
                            } catch {}
                        }}>
                            <h2>Rate {product.name}</h2>
                            <label>
                                Score
                                <select value={score} onChange={(e) => setScore(Number(e.target.value))}>
                                    {[5, 4, 3, 2, 1].map(n => (
                                        <option key={n} value={n}>{n}</option>
                                    ))}
                                </select>
                            </label>
                            <label>
                                Comment
                                <textarea
                                    value={comment}
                                    onChange={(e) => setComment(e.target.value)}
                                    placeholder="Write a review..."
                                    rows={3}
                                />
                            </label>
                            {ratingError && <p className={styles.error}>Failed to submit rating</p>}
                            <button type="submit" disabled={ratingLoading}>
                                {ratingLoading ? "Submitting..." : "Submit"}
                            </button>
                        </form>
                    </Modal>

                    <div>
                        <h2>Dimensions</h2>
                        <p>Width: {formatDimension(product.width, imperial)}</p>
                        <p>Height: {formatDimension(product.height, imperial)}</p>
                        <p>Depth: {formatDimension(product.depth, imperial)}</p>
                        <p>Weight: {product.weight != null ? (imperial ? `${kgToLb(product.weight)} lb` : `${product.weight} kg`) : null}</p>
                    </div>
                </div>
            </div>

            {reviews && reviews.length > 0 && (
                <section className={styles.reviews}>
                    <h2>Customer Reviews</h2>
                    {reviews.map((r) => (
                        <div key={r.id} className={styles.reviewCard}>
                            <div className={styles.reviewHeader}>
                                <strong>{r.userName}</strong>
                                <Stars rating={r.score} />
                                <span className={styles.reviewDate}>
                                    {new Date(r.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                            <p className={styles.reviewComment}>{r.comment}</p>
                            <button
                                className={`${styles.helpfulBtn} ${r.votedByCurrentUser ? styles.helpfulActive : ""}`}
                                disabled={!token || votingId === r.id}
                                onClick={() => handleVote(r.id)}
                            >
                                Helpful ({r.helpfulCount})
                            </button>
                        </div>
                    ))}
                </section>
            )}
        </div>
    );
}
