import styles from './createProduct.module.css'
import useFetch from "../useFetch.jsx";
import {useState} from "react";

export function createProduct() {
    const [product, setProduct] = useState({
        name: "",
        description: "",
        price: 0,
        category: "",
        imageUrl: ""
    })
    const { data, error, refetch} = useFetch('/api/products', { method: 'POST', body: product, headers: { 'Content-Type': 'application/json' }, withAuth: true, immediate: false, });
    return
}