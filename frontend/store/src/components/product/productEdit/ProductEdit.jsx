import {useNavigate, useParams} from "react-router-dom";
import useFetch from "../../useFetch.js";
import {useCallback, useEffect, useState} from "react";
import {useSelector} from "react-redux";
import styles from '../productCreation/ProductCreation.module.css';
import ProductImageMenu from "../imageMenu/ProductImageMenu.jsx";
import {paths} from "../../routes.js";
import {inToCm, lbToKg, cmToIn, kgToLb} from "../../units.js";
import {Helmet} from "react-helmet-async";

function ProductEdit() {
    const {id} = useParams();
    const navigate = useNavigate();
    const imperial = useSelector((s) => s.settings.imperialUnits);

    const {data: product, loading: loadingProduct} = useFetch(`/api/products/${id}`, {withAuth: true});
    const {data: categories, loading: loadingCategories} = useFetch('/api/categories', {});
    const {data: brands, loading: loadingBrands} = useFetch('/api/brands', {});

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
    const [brandId, setBrandId] = useState('');
    const [images, setImages] = useState([]);
    const [initialized, setInitialized] = useState(false);

    const {data: updateResult, error: updateError, loading: updating, reFetch: submitUpdate} = useFetch(`/api/products/${id}`, {
        method: 'PUT',
        withAuth: true,
        immediate: false,
    });

    // Populate form when product loads
    useEffect(() => {
        if (!product || initialized) return;
        setName(product.name || '');
        setDescription(product.description || '');

        const priceStr = product.price?.toFixed(2) || '0.00';
        const [maj, min] = priceStr.split('.');
        setPriceMajor(maj);
        setPriceMinor(min);
        setCurrency(product.currency || 'EUR');

        const dimConvert = (v) => v != null ? String(imperial ? cmToIn(Number(v)) : Number(v)) : '';
        setWidth(dimConvert(product.width));
        setHeight(dimConvert(product.height));
        setDepth(dimConvert(product.depth));
        setWeight(product.weight != null ? String(imperial ? kgToLb(Number(product.weight)) : Number(product.weight)) : '');

        setStock(product.stock != null ? String(product.stock) : '');
        setCategoryId(product.categoryId || '');
        setBrandId(product.brandId || '');

        if (product.images?.length > 0) {
            setImages(product.images
                .sort((a, b) => a.sortOrder - b.sortOrder)
                .map(img => ({key: img.key, url: img.url}))
            );
        }
        setInitialized(true);
    }, [product, initialized, imperial]);

    useEffect(() => {
        if (updateResult) navigate(paths.product(id));
    }, [updateResult]);

    const onMajorChange = (e, setValue) => {
        setValue(e.target.value.replace(/\D/g, ""));
    };
    const normalizeMajor = (value, setValue) => {
        if (value === "") return;
        const normalized = value.replace(/^0+/, "");
        setValue(normalized === "" ? "0" : normalized);
    };
    const onMinorChange = (e, setValue) => {
        setValue(e.target.value.replace(/\D/g, "").slice(0, 2));
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
            Object.entries(obj).filter(([, v]) => v !== null && v !== "" && v !== undefined)
        );

    const toMetricDimension = (v) => {
        const n = toOptionalNumber(v);
        return n == null ? null : imperial ? inToCm(n) : n;
    };
    const toMetricWeight = (v) => {
        const n = toOptionalNumber(v);
        return n == null ? null : imperial ? lbToKg(n) : n;
    };

    const buildProduct = () => {
        const base = {
            name: name.trim(),
            price: buildPrice(priceMajor, priceMinor),
            currency,
            description: description.trim() || null,
            width: toMetricDimension(width),
            height: toMetricDimension(height),
            depth: toMetricDimension(depth),
            weight: toMetricWeight(weight),
            brandId: brandId || null,
            stock: stock.trim() === "" ? null : Number(stock),
            categoryId: categoryId || null,
            images: images.map((img, index) => ({
                fileKey: img.key,
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
            alert("missing required fields");
            return;
        }
        const product = buildProduct();
        await submitUpdate({body: product});
    }, [submitUpdate, name, priceMajor, priceMinor, currency, description, width, height, depth, weight, stock, categoryId, brandId, images]);

    if (loadingProduct) return <div className={styles.main}>Loading...</div>;

    return (
        <div className={styles.main}>
            <Helmet><title>Edit Product - Admin</title></Helmet>
            <h1>Edit Product</h1>
            {updateError && <p style={{color: 'red'}}>Failed to update product</p>}
            <div>
                <label htmlFor="name">Name:</label>
                <input onChange={(e) => setName(e.target.value)} placeholder="name" id="name" value={name}/>
            </div>
            <div>
                <label htmlFor="priceMajor">Price:</label>
                <input
                    onChange={(e) => onMajorChange(e, setPriceMajor)}
                    onBlur={() => normalizeMajor(priceMajor, setPriceMajor)}
                    placeholder="0" id="priceMajor" value={priceMajor}
                    className={styles.priceMajor}
                /> <span> . </span>
                <input
                    onChange={(e) => onMinorChange(e, setPriceMinor)}
                    onBlur={() => normalizeMinor(priceMinor, setPriceMinor)}
                    placeholder="00" id="priceMinor" value={priceMinor}
                    className={styles.priceMinor}
                /> <span> </span>
                <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
                    <option value="EUR">EUR</option>
                </select>
            </div>
            <label>Category:</label>
            <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                {categoryId === "" && <option value="" hidden>Select category</option>}
                {loadingCategories && <option disabled>Loading categories...</option>}
                {!loadingCategories && categories
                    ?.filter(cat => cat.isLeaf === true)
                    .map(cat => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
            </select>
            <div>
                <label htmlFor="stock">Stock:</label>
                <input
                    onChange={(e) => onMajorChange(e, setStock)}
                    onBlur={() => normalizeMajor(stock, setStock)}
                    placeholder="0" id="stock" value={stock}
                    className={styles.priceMajor}
                />
            </div>

            <p>Optional:</p>
            <div>
                <label htmlFor="brand">Brand:</label>
                <select value={brandId} onChange={(e) => setBrandId(e.target.value)}>
                    <option value="">No brand</option>
                    {loadingBrands && <option disabled>Loading brands...</option>}
                    {!loadingBrands && brands?.map(brand => (
                        <option key={brand.id} value={brand.id}>{brand.name}</option>
                    ))}
                </select>
            </div>
            <div>
                <label htmlFor="description">Description:</label>
                <input onChange={(e) => setDescription(e.target.value)} placeholder="description" id="description" value={description}/>
            </div>
            <div>
                <label htmlFor="width">Width ({imperial ? "in" : "cm"}):</label>
                <input onChange={(e) => setWidth(e.target.value)} placeholder="width" id="width" value={width}/>
            </div>
            <div>
                <label htmlFor="height">Height ({imperial ? "in" : "cm"}):</label>
                <input onChange={(e) => setHeight(e.target.value)} placeholder="height" id="height" value={height}/>
            </div>
            <div>
                <label htmlFor="depth">Depth ({imperial ? "in" : "cm"}):</label>
                <input onChange={(e) => setDepth(e.target.value)} placeholder="depth" id="depth" value={depth}/>
            </div>
            <div>
                <label htmlFor="weight">Weight ({imperial ? "lb" : "kg"}):</label>
                <input onChange={(e) => setWeight(e.target.value)} placeholder="weight" id="weight" value={weight}/>
            </div>
            <p className={styles.smalltext}>max 10 images, top one is the thumbnail</p>
            <ProductImageMenu maxFiles={10} setImages={setImages} images={images}/>
            <button onClick={handleSubmit} disabled={updating}>
                {updating ? "Saving..." : "Save Changes"}
            </button>
        </div>
    );
}

export default ProductEdit;
