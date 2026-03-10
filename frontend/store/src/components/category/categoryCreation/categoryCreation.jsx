import styles from './categoryCreation.module.css';
import useFetch from "../../useFetch.js";
import {useState, useEffect} from "react";

function CategoryCreation({parentId = null, onSuccess}) {

    const [name, setName] = useState("")
    const [isLeaf, setIsLeaf] = useState(false)
    const {data, error, loading, reFetch} = useFetch('/api/categories', {
        method: "POST",
        body: {
            name,
            isLeaf,
            parentId
        },
        withAuth: true,
        immediate: false,
    })

    useEffect(() => {
        if (data && onSuccess) onSuccess();
    }, [data]);

    function handleSubmit(e) {
        e.preventDefault()
        reFetch()
    }

    return (

        <div className={styles.main}>
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
            <label htmlFor="isleaf">is Leaf:</label>
            <input
                id="isleaf"
                type="checkbox"
                checked={isLeaf}
                onChange={e => setIsLeaf(e.target.checked)}
            />

            {error && <p>{error}</p>}
            <button onClick={handleSubmit} disabled={loading || !name}>Create</button>
        </div>

    );
}

export default CategoryCreation;