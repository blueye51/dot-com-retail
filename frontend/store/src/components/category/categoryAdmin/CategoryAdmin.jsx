import { useMemo, useState } from "react";
import useFetch from "../../useFetch.js";
import CategoryAdminNode from "./CategoryAdminNode.jsx";
import styles from "./CategoryAdmin.module.css";

function buildTree(categories) {
    const map = {};
    categories.forEach(c => map[c.id] = { ...c, children: [] });

    const roots = [];
    categories.forEach(c => {
        if (c.parentId === null) {
            roots.push(map[c.id]);
        } else {
            map[c.parentId]?.children.push(map[c.id]);
        }
    });
    return roots;
}

export default function CategoryAdmin() {
    const { data, reFetch, loading } = useFetch("/api/categories");
    const tree = useMemo(() => buildTree(data || []), [data]);

    const [adding, setAdding] = useState(false);
    const [newName, setNewName] = useState("");
    const [newIsLeaf, setNewIsLeaf] = useState(false);

    const {reFetch: createNew} = useFetch("/api/categories", {
        method: "POST",
        body: { name: newName, isLeaf: newIsLeaf, parentId: null },
        withAuth: true,
        immediate: false,
    });

    const handleAddRoot = async (e) => {
        e.preventDefault();
        if (!newName.trim()) return;
        try {
            await createNew();
            setAdding(false);
            setNewName("");
            setNewIsLeaf(false);
            reFetch();
        } catch {}
    };

    if (loading) return <div className={styles.page}>Loading categories...</div>;

    return (
        <div className={styles.main}>
            <div className={styles.header}>
                <h1>Categories</h1>
                <button onClick={() => setAdding(p => !p)}>+ New Root Category</button>
            </div>

            {adding && (
                <form onSubmit={handleAddRoot} className={styles.inlineForm}>
                    <input
                        type="text"
                        placeholder="Category name"
                        value={newName}
                        onChange={e => setNewName(e.target.value)}
                        autoFocus
                    />
                    <label>
                        <input type="checkbox" checked={newIsLeaf} onChange={e => setNewIsLeaf(e.target.checked)} />
                        Leaf
                    </label>
                    <button type="submit" disabled={!newName.trim()}>Save</button>
                    <button type="button" onClick={() => setAdding(false)}>Cancel</button>
                </form>
            )}

            <div className={styles.tree}>
                {tree.length === 0
                    ? <p>No categories yet.</p>
                    : tree.map(node => (
                        <CategoryAdminNode key={node.id} node={node} onRefresh={reFetch} />
                    ))
                }
            </div>
        </div>
    );
}
