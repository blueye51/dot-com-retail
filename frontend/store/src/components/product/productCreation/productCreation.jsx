import {Link} from "react-router-dom";
import useFetch from "../../useFetch.jsx";
import {useEffect, useState} from "react";
import styles from './productCreation.module.css';
import ImageUpload from "../imageMenu/productImageMenu.jsx";

function ProductCreation() {

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
//     images:[{
//         fileKey: string,
//         sortOrder: number
//     }...]
// }
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [priceMajor, setPriceMajor] = useState('');
    const [priceMinor, setPriceMinor] = useState('');
    const [currency, setCurrency] = useState('EUR');
    const [width, setWidth] = useState('');
    const [height, setHeight] = useState('');
    const [depth, setDepth] = useState('');
    const [weight, setWeight] = useState('');
    const [stock, setStock] = useState('');
    const [categoryId, setCategoryId] = useState('');

    const {data : categories, error, loading, reFetch, abort} = useFetch('/api/categories', {})

    const onMajorChange = (e, setValue) => {
        const digitsOnly = e.target.value.replace(/\D/g, "");
        setValue(digitsOnly);
    };
    const normalizeMajor = (value, setValue) => {
        if (value === "") return;

        const normalized = value.replace(/^0+/, "");
        setValue(normalized === "" ? "0" : normalized);
    };
    const onMinorChange = (e, setValue) => {
        const digitsOnly = e.target.value.replace(/\D/g, "").slice(0, 2);
        setValue(digitsOnly);
    };
    const normalizeMinor = (value, setValue) => {
        if (value === "") return;
        setValue(value.padEnd(2, "0"));
    };



    function handleSubmit(event) {
    }

    return (
        <div className={styles.main}>
            <h2>Product Creation Page</h2>
            <div>
                <label htmlFor="name">Name:</label>
                <input
                    onChange={(e) => setName(e.target.value)}
                    placeholder="name"
                    id="name"
                    value={name}
                />
            </div>
            <div>
                <label htmlFor="price">Price:</label>
                <input
                    onChange={(e) => onMajorChange(e, setPriceMajor)}
                    onBlur={() => normalizeMajor(priceMajor, setPriceMajor)}
                    placeholder="0"
                    id="price"
                    value={priceMajor}
                    className={styles.priceMajor}

                /> <span> . </span>
                <input
                    onChange={(e) => onMinorChange(e, setPriceMinor)}
                    onBlur={() => normalizeMinor(priceMinor, setPriceMinor)}
                    placeholder="00"
                    id="price"
                    value={priceMinor}
                    className={styles.priceMinor}
                /> <span> </span>
                <select
                    value={currency}
                    onChange={(e) => setCurrency(e.target.value)}
                >
                    <option value="EUR">EUR</option>
                </select>
            </div>
            <label htmlFor="price">Category:</label>
            <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
            >
                {loading && <option disabled>Loading categories...</option>}

                {!loading && categories
                    ?.filter(cat => cat.isLeaf === true)
                    .map(cat => (
                        <option key={cat.id} value={cat.id}>
                            {cat.name}
                        </option>
                    ))}
            </select>
            <div>
            <label htmlFor="price">Stock:</label>
            <input
                onChange={(e) => onMajorChange(e, setStock)}
                onBlur={() => normalizeMajor(stock, setStock)}
                placeholder="0"
                id="stock"
                value={stock}
                className={styles.priceMajor}
            />
            </div>



            <p>Optional:</p>
            <div>
                <label htmlFor="description">Description:</label>
                <input
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="description"
                    id="description"
                    value={description}
                />
            </div>
            <div>
                <label htmlFor="width">Width:</label>
                <input
                    onChange={(e) => setWidth(e.target.value)}
                    placeholder="width"
                    id="width"
                    value={width}
                />
            </div>
            <div>
                <label htmlFor="height">Height:</label>
                <input
                    onChange={(e) => setHeight(e.target.value)}
                    placeholder="height"
                    id="height"
                    value={height}
                />
            </div>
            <div>
                <label htmlFor="depth">Depth:</label>
                <input
                    onChange={(e) => setDepth(e.target.value)}
                    placeholder="depth"
                    id="depth"
                    value={depth}
                />
            </div>
            <div>
                <label htmlFor="weight">Weight:</label>
                <input
                    onChange={(e) => setWeight(e.target.value)}
                    placeholder="weight"
                    id="weight"
                    value={weight}
                />
            </div>
            <ImageUpload maxFiles={10}/>
            <button onClick={handleSubmit}>Create</button>
        </div>
    )
}

export default ProductCreation;
