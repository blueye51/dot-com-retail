import styles from './productRow.module.css';
import {useMemo} from "react";
import defaultImage from "../../../../assets/default.png";


export default function ProductRow ({ product }) {
    const createdAt = useMemo(
        () => new Date(product.createdAt).toLocaleDateString(),
        [product.createdAt]
    );

    return (
        <div className={styles.row}>
            <img
                className={styles.thumb}
                src={product.imageUrl ?? defaultImage}
                alt={product.name}
                loading="lazy"
            />

            <div className={styles.meta}>
                <h3 className={styles.name}>{product.name}</h3>

                <div className={styles.line}>
                    <span>Price:</span>
                    <span>{product.price} {product.currency}</span>
                </div>

                <div className={styles.line}>
                    <span>Stock:</span>
                    <span>{product.stock}</span>
                </div>

                <div className={styles.line}>
                    <span>Category:</span>
                    <span>{product.category}</span>
                </div>

                <div className={styles.line}>
                    <span>Created:</span>
                    <span>{createdAt}</span>
                </div>
            </div>
        </div>
    );
};