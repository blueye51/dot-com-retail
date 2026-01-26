import useFetch from "../../useFetch.jsx";
import {useEffect, useState} from "react";
import styles from './categoryTree.module.css';
import CategoryCreation from "../categoryCreation/categoryCreation.jsx";


function categoryTree() {


    const [creating, setCreating] = useState(false)
    const [editing, setEditing] = useState(false)
    const [deleting, setDeleting] = useState(false)

    const [focusedNodeId, setFocusedNodeId] = useState(null)


    const [categories, setCategories] = useState({})
    const {data, error, loading, reFetch, abort} = useFetch('/api/categories', {})

    useEffect(() => {
        if (!data) return;

        const next = {};

        for (const cat of data) {
            const parent = cat.parentId ?? "root";
            if (next[parent] == null) next[parent] = [];
            next[parent].push(cat);
        }

        setCategories(next);
    }, [data]);


    function toggleCreateChild(parentId) {
        setCreating(prev => !prev)
        setFocusedNodeId(parentId)
    }

    function toggleEdit(id) {
        setEditing(prev => !prev)
    }

    function toggleDelete(id) {
        setDeleting(prev => !prev)
    }

    function renderCategories(node) {
        const children = categories[node.id] ?? [];

        return (
            <div key={node.id} className={node.isLeaf ? styles.leaf : styles.nonLeaf}>
                <div>
                    <p>{node.name}</p>
                    {!node.isLeaf && <button onClick={() => toggleCreateChild(node.id)}>Add Category</button>}
                    <button onClick={() => toggleEdit(node.id)}>Edit</button>
                    <button onClick={() => toggleDelete(node.id)}>Delete</button>
                </div>
                {children.map(renderCategories)}
            </div>
        )
    }

    const rootCategories = categories.root ?? [];

    return (
        <div>
            <button onClick={() => toggleCreateChild(null)}>
                Create New Root Category
            </button>
            <div className={styles.categoryTree}>
                {rootCategories.map(renderCategories)}
            </div>


            {(creating || deleting || editing) && (
                <div className={styles.backdrop} onClick={() => toggleCreateChild(null)}>
                    <div className={styles.window} onClick={e => e.stopPropagation()}>
                        <CategoryCreation parentId={focusedNodeId}/>
                    </div>
                    {}
                </div>
            )}
        </div>
    );
}

export default categoryTree;