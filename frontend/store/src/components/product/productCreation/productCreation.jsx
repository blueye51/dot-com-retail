import { Link } from "react-router-dom";
import useFetch from "../../useFetch.jsx";
import {useEffect, useState} from "react";

function ProductCreation () {

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
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [price, setPrice] = useState(0);
    const [currency, setCurrency] = useState('EUR');
    const [width, setWidth] = useState(0);
    const [height, setHeight] = useState(0);
    const [depth, setDepth] = useState(0);
    const [weight, setWeight] = useState(0);
    const [stock, setStock] = useState(0);
    const [categoryId, setCategoryId] = useState('');

    function handleSubmit(event) {
        event.preventDefault();
    }

    return (
        <div>
            <h2>Product Creation Page</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="name">Name:</label>
                    <input
                        onChange={(e) => setName(e.target.value)}
                        placeholder="name"
                        type="text"
                        id="name"
                        name="name"
                        value={name}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="description">Description:</label>
                    <input
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="description"
                        type="text"
                        id="description"
                        name="description"
                        value={description}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="price">Price:</label>
                    <input
                        onChange={(e) => setPrice(parseFloat(e.target.value))}
                        placeholder="price"
                        type="number"
                        id="price"
                        name="price"
                        value={price}
                        required
                    />
                </div>
                <button type="submit">Create</button>
            </form>
        </div>
    )
}

export default ProductCreation;
