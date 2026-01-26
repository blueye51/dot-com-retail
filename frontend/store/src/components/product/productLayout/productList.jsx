import { Link } from "react-router-dom";
import useFetch from "../../useFetch.jsx";
import {useEffect, useState} from "react";

function ProductList () {

    // Product object{
    //     id: string,
    //     name: string,
    //     description: string,
    //     price: number,
    //     currency: string, (e.g., "USD", "EUR")
    //     width: number,
    //     height: number,
    //     depth: number,
    //     weight: number,
    //     stock: number,
    //     category: string,
    //     createdAt: string,
    //     images:[image{
    //         id: string,
    //         imageUrl: string,
    //         sortOrder: number
    //     }]
    const [pageSize, setPageSize] = useState(100);
    const [pageNumber, setPageNumber] = useState(0);
    const [products, setProducts] = useState([]);

    const { data, error, loading, reFetch } = useFetch(`/api/products/page?page=${pageNumber}&size=${pageSize}`, {});

    useEffect(() => {
        if (!error) return;
        console.error('Failed to fetch products:', error);
        alert('Failed to fetch products: ' + (error.message || 'Unknown error'));
    }, [error]);

    useEffect(() => {
        if (!data) return;
        console.log(data)
        setProducts(data.products || []);

    }, [data]);

    function renderProduct(id, name, price, currency, stock, category, createdAt, frontImage) {
        return (
            <div>
                <img src={frontImage} alt={name} height="100"/>
                <div>
                    <h3>{name}</h3>
                    <p>Price: {price} {currency}</p>
                    <p>Stock: {stock}</p>
                    <p>Category: {category}</p>
                    <p>Created At: {new Date(createdAt).toLocaleDateString()}</p>
                </div>
            </div>
        )

    }

    return (
        <div>
            {products.map((product) => {
                const frontImage =
                    product.images && product.images.length > 0
                        ? product.images[0].imageUrl
                        : "";

                return (
                    <Link key={product.id} to={`/products/${product.id}`}>
                        {renderProduct(
                            product.id,
                            product.name,
                            product.price,
                            product.currency,
                            product.stock,
                            product.category,
                            product.createdAt,
                            frontImage
                        )}
                    </Link>
                );
            })}
        </div>
    );
}

export default ProductList;