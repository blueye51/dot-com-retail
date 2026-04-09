import styles from './ProductCard.module.css';
import {Link} from "react-router-dom";
import {useSelector, useDispatch} from "react-redux";
import {paths} from "../../routes.js";
import {addToGuestCart, setCart} from "../../store.js";
import Stars from "./Stars.jsx";
import {useState} from "react";

const BASE_URL = import.meta.env.VITE_API_BASE;

export function ProductCard({
                                id,
                                name = "N/A",
                                price = -1,
                                currency = "N/A",
                                stock = 0,
                                brand = "",
                                category = "N/A",
                                imageUrl = "",
                                averageRating = 0,
                                totalRatings = 0,
                            }) {
    const dispatch = useDispatch();
    const {token} = useSelector((s) => s.auth);
    const [adding, setAdding] = useState(false);

    const handleAddToCart = async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (token) {
            setAdding(true);
            try {
                const res = await fetch(`${BASE_URL}/api/cart`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json", Authorization: `Bearer ${token}`},
                    credentials: "include",
                    body: JSON.stringify({productId: id, quantity: 1}),
                });
                if (res.ok) dispatch(setCart(await res.json()));
            } catch {} finally { setAdding(false); }
        } else {
            dispatch(addToGuestCart({productId: id, productName: name, price, currency, quantity: 1, stock, imageUrl}));
        }
    };

    return (
        <Link to={paths.product(id)} className={styles.card}>
            <img className={styles.thumbnail} src={imageUrl} alt={name}/>
            <p>&gt;{category}</p>
            <h3>{name}</h3>
            <Stars rating={averageRating} count={totalRatings} />
            <p className={styles.price}>{price} {currency}</p>
            <button className={styles.addToCartButton} disabled={stock <= 0 || adding} onClick={handleAddToCart}>
                {adding ? "Adding..." : "Add to Cart"}
            </button>
        </Link>
    )
}

// UUID id,
//     String name,
//     String description,
//     String price,
//     String currency,
//     String width,
//     String height,
//     String depth,
//     String weight,
//     Integer stock,
//     String brand,
// String thumnail