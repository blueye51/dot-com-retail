import { Link } from "react-router-dom";
import useFetch from "../useFetch.jsx";
import {useEffect, useState} from "react";

function ProductList () {
    const [pageSize, setPageSize] = useState(100);
    const [pageNumber, setPageNumber] = useState(0);


    const { data, error, loading, reFetch } = useFetch(`/api/products/page?page=${pageNumber}&size=${pageSize}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        withAuth: true,
        immediate: true,
    });

    useEffect(() => {
        if (!error) return;
        console.error('Login error:', error);
        alert('Login failed: ' + (error.message || 'Unknown error'));
    }, [error]);

    useEffect(() => {
        if (!data) return;
        console.error('Login error:', error);
        alert('Login failed: ' + (error.message || 'Unknown error'));
    }, [data]);

    return (
        <div>
            <Link to="/product">Go to Product</Link>
        </div>
    );
}

export default ProductList;