import {useParams} from "react-router-dom";
import useFetch from "../useFetch.jsx";
import styles from "./productPage.module.css"
import Modal from "../modal/modal.jsx";
import {useState} from "react";
import defaultImage from "../../assets/default.png";


export default function ProductPage() {
    const {id} = useParams();
    const {data: product, loading, error} = useFetch(`/api/products/${id}`);

    const [imageOpen, setImageOpen] = useState(false)

    const closeImage = () => {
        setImageOpen(false)
    }

    if (loading) return <div className={styles.page}>Loading…</div>;
    if (error) {
        console.log(error)
        return <div className={styles.page}>Failed to load.</div>;
    }
    if (!product) return <div className={styles.page}>Not found.</div>;

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
            <div className={styles.product}>
                <div className={styles.media}>
                    {renderImage()}
                </div>

                <div className={styles.info}>
                    <h1>{product.name}</h1>
                    <p>stock: {product.stock}</p>
                    <div>price: {product.price.toFixed(2)} {product.currency}</div>
                    <p>Description: {product.description}</p>

                </div>
            </div>
        </div>
    );
}
