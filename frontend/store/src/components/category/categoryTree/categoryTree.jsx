import useFetch from "../../useFetch.jsx";
import {useEffect, useState} from "react";
import styles from './categoryTree.module.css';
import CategoryCreation from "../categoryCreation/categoryCreation.jsx";
import Modal from "../../modal/modal.jsx";


function categoryTree() {


    const [creating, setCreating] = useState(false)
    const [focusedNodeId, setFocusedNodeId] = useState(null)
    function close() {
        setFocusedNodeId(null);
        setCreating(false);
    }

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


    function renderCategories(node) {
        const children = categories[node.id] ?? [];

        return (
            <div key={node.id} className={node.isLeaf ? styles.leaf : styles.nonLeaf}>
                <div>
                    <p>{node.name}</p>
                    {!node.isLeaf && <button onClick={() => toggleCreateChild(node.id)}>Add Category</button>}
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


            <Modal open={creating} onClose={close}>
                <CategoryCreation parentId={focusedNodeId}/>
            </Modal>
        </div>
    );
}

export default categoryTree;