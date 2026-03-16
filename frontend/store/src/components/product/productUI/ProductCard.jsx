import styles from './ProductCard.module.css';
import {Link} from "react-router-dom";
import {paths} from "../../routes.js";
import Stars from "./Stars.jsx";

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
    return (
        <Link to={paths.product(id)} className={styles.card}>
            <img className={styles.thumbnail} src={imageUrl} alt={name}/>
            <p>&gt;{category}</p>
            <h2>{name}</h2>
            <Stars rating={averageRating} count={totalRatings} />
            <h3>{price} {currency}</h3>
            <button className={styles.addToCartButton} disabled={stock <= 0}>Add to Cart</button>
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