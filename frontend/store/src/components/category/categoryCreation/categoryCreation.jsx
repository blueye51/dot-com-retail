import styles from './categoryCreation.module.css';
import useFetch from "../../useFetch.jsx";
import {useState} from "react";

function CategoryCreation({parentId = null}) {

    // category object{
//     id: string,
//     name: string,
//     isLeaf: boolean,
//     parentId: string UUID,
    const [category, setCategory] = useState({})
    const [name, setName] = useState("")
    const [isLeaf, setIsLeaf] = useState(false)
    const {data, error, loading, reFetch: fetchPost} = useFetch('/api/categories', {
        method: "POST",
        body: {
            name,
            isLeaf,
            parentId
        },
        withAuth: true,
        immediate: false,
    })

    function handleSubmit(e) {
        e.preventDefault()
        console.log({
            name,
            isLeaf,
            parentId
        })
        fetchPost()
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
            <p>{parentId}</p>

            <button onClick={handleSubmit}>Create</button>
        </div>

    );
}

export default CategoryCreation;