import {Link, useNavigate} from "react-router-dom";
import useFetch from "../../useFetch.jsx";
import {useCallback, useEffect, useState} from "react";
import styles from './productCreation.module.css';
import ProductImageMenu from "../imageMenu/productImageMenu.jsx";
import {PATHS, paths} from "../../routes.jsx";

function ProductCreation() {

// Product object{
//     name: string, (req.)
//     price: number, (req.)
//     currency: string, (e.g., "USD", "EUR") (req.)
//     description: string,
//     width: number,
//     height: number,
//     depth: number,
//     weight: number,
//     stock: number, (req.)
//     categoryId: string, (req.)
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

    const [images, setImages] = useState([]);

    const navigate = useNavigate();

    const {data: categories, loading: loadingCategories} = useFetch('/api/categories', {})

    const {data, error, loading, reFetch} = useFetch('/api/products', {
        method: 'POST',
        withAuth: true,
        immediate: false,
    })

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

    const toOptionalNumber = (s) => (s.trim() === "" ? null : Number(s));


    const buildPrice = (major, minor) => {
        const majorNum = major.trim() === "" ? 0 : Number(major);
        const minorNum = minor.trim() === "" ? 0 : Number(minor.padEnd(2, "0"));
        return majorNum + minorNum / 100;
    };

    const omitEmpty = (obj) =>
        Object.fromEntries(
            Object.entries(obj).filter(
                ([, v]) => v !== null && v !== "" && v !== undefined
            )
        );

    const buildProduct = () => {
        const base = {
            name: name.trim(),
            price: buildPrice(priceMajor, priceMinor),
            currency,
            description: description.trim() || null,

            width: toOptionalNumber(width),
            height: toOptionalNumber(height),
            depth: toOptionalNumber(depth),
            weight: toOptionalNumber(weight),

            stock: stock.trim() === "" ? null : Number(stock),
            categoryId: categoryId || null,

            images: images.map((img, index) => ({
                fileKey: img.key,      // or img if images is array of strings
                sortOrder: index,
            })),
        };

        return omitEmpty(base);
    };


    const requiredCheck = () => {
        if (!name.trim()) return false;
        if (!currency.trim()) return false;
        if (!stock.trim()) return false;
        if (!categoryId) return false;
        return true;
    };

    const handleSubmit = useCallback(async () => {
        if (!requiredCheck()) {
            console.log("missing required fields")
            return
        }

        const product = buildProduct();
        await reFetch({body: product})

    }, [reFetch, name, priceMajor, priceMinor, currency, description, width, height, depth, weight, stock, categoryId, images])

    useEffect(() => {
        if (!data) return;
        navigate(paths.product(data))
    }, [data]);

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
                <label htmlFor="priceMajor">Price:</label>
                <input
                    onChange={(e) => onMajorChange(e, setPriceMajor)}
                    onBlur={() => normalizeMajor(priceMajor, setPriceMajor)}
                    placeholder="0"
                    id="priceMajor"
                    value={priceMajor}
                    className={styles.priceMajor}

                /> <span> . </span>
                <input
                    onChange={(e) => onMinorChange(e, setPriceMinor)}
                    onBlur={() => normalizeMinor(priceMinor, setPriceMinor)}
                    placeholder="00"
                    id="priceMinor"
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
                {categoryId === "" && (
                    <option value="" hidden>
                        Select category
                    </option>
                )}

                {loadingCategories && (
                    <option disabled>Loading categories...</option>
                )}

                {!loadingCategories &&
                    categories
                        ?.filter(cat => cat.isLeaf === true)
                        .map(cat => (
                            <option key={cat.id} value={cat.id}>
                                {cat.name}
                            </option>
                        ))}
            </select>



            <Link to={paths.categoryTree()}>Create categories here</Link>
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
            <ProductImageMenu maxFiles={10} setImages={setImages} images={images}/>
            <button onClick={handleSubmit} disabled={loading}>
                {loading ? "Creating..." : "Create"}
            </button>

        </div>
    )
}

export default ProductCreation;
