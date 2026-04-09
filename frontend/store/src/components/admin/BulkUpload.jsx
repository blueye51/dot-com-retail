import {useState} from "react";
import {useSelector} from "react-redux";
import {Helmet} from "react-helmet-async";
import styles from "./BulkUpload.module.css";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function BulkUpload() {
    const {token} = useSelector((s) => s.auth);
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState(null);

    const handleUpload = async () => {
        if (!file) return;
        setUploading(true);
        setResult(null);
        try {
            const form = new FormData();
            form.append("file", file);
            const res = await fetch(`${BASE_URL}/api/admin/products/bulk`, {
                method: "POST",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
                body: form,
            });
            const data = await res.json();
            setResult(data);
        } catch {
            setResult({created: 0, failed: 0, errors: [{row: 0, name: "", messages: ["Upload failed"]}]});
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className={styles.page}>
            <Helmet><title>Bulk Upload - Admin</title></Helmet>
            <h1>Bulk Product Upload</h1>
            <p className={styles.hint}>
                Upload a <strong>.csv</strong> or <strong>.json</strong> file. Use category and brand <strong>names</strong> (not IDs).
            </p>
            <p className={styles.hint}>
                Required columns: <code>name</code>, <code>price</code>, <code>currency</code>, <code>stock</code>, <code>category</code>
            </p>
            <p className={styles.hint}>
                Optional columns: <code>brand</code>, <code>description</code>, <code>width</code>, <code>height</code>, <code>depth</code>, <code>weight</code>
            </p>

            <div className={styles.example}>
                <h2>CSV Example</h2>
                <pre>{`name,price,currency,stock,category,brand,description
Widget,9.99,EUR,50,Electronics,Samsung,A cool widget
Gadget,19.99,EUR,30,Laptops,,No brand needed`}</pre>
            </div>

            <div className={styles.uploadArea}>
                <input
                    type="file"
                    accept=".csv,.json"
                    onChange={(e) => {setFile(e.target.files[0]); setResult(null);}}
                />
                <button onClick={handleUpload} disabled={!file || uploading}>
                    {uploading ? "Uploading..." : "Upload"}
                </button>
            </div>

            {result && (
                <div className={styles.result}>
                    <p className={styles.summary}>
                        <span className={styles.success}>Created: {result.created}</span>
                        {result.failed > 0 && <span className={styles.failure}> Failed: {result.failed}</span>}
                    </p>

                    {result.errors?.length > 0 && (
                        <div className={styles.errorList}>
                            <h2>Errors</h2>
                            {result.errors.map((err, i) => (
                                <div key={i} className={styles.errorRow}>
                                    <strong>Row {err.row}{err.name ? ` ("${err.name}")` : ""}:</strong>
                                    <ul>
                                        {err.messages.map((msg, j) => (
                                            <li key={j}>{msg}</li>
                                        ))}
                                    </ul>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
