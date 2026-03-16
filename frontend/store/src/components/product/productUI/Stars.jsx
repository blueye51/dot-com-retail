import styles from "./Stars.module.css";

export default function Stars({ rating = 0, count }) {
    return (
        <span className={styles.stars}>
            {[1, 2, 3, 4, 5].map(i => {
                const fill = Math.min(1, Math.max(0, rating - (i - 1)));
                return (
                    <span key={i} className={styles.starWrapper}>
                        <span className={styles.emptyStar}>&#9733;</span>
                        <span className={styles.filledStar} style={{ width: `${fill * 100}%` }}>&#9733;</span>
                    </span>
                );
            })}
            {count != null && <span className={styles.count}>({count})</span>}
        </span>
    );
}
