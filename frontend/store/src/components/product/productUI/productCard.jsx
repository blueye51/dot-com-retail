import styles from './productCard.module.css';

export function ProductCard({
                                id,
                                name = "N/A",
                                price = -1,
                                currency = "N/A",
                                stock = 0,
                                thumbnail = ""
                            }) {
    return(
        <div className={styles.card}>
            <img className={styles.thumbnail} src={thumbnail} alt={name} />
            <h2 className={styles.productName}>{name}</h2>
            <p className={styles.productPrice}>{price >= 0 ? `${price} ${currency}` : "Price not available"}</p>
            <p className={styles.productStock}>{stock > 0 ? `In Stock: ${stock}` : "Out of Stock"}</p>
            <button className={styles.addToCartButton} disabled={stock <= 0}>Add to Cart</button>
        </div>
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
// String thumnail