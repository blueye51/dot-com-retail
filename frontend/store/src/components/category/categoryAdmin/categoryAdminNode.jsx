import { useState } from "react";
import styles from "./categoryAdmin.module.css";
import useFetch from "../../useFetch.js";

export default function CategoryAdminNode({ node, onRefresh }) {
    const [open, setOpen] = useState(false);
    const [adding, setAdding] = useState(false);
    const [editing, setEditing] = useState(false);
    const [newName, setNewName] = useState("");
    const [newIsLeaf, setNewIsLeaf] = useState(false);
    const [editName, setEditName] = useState(node.name);

    const createFetch = useFetch("/api/categories", {
        method: "POST",
        body: { name: newName, isLeaf: newIsLeaf, parentId: node.id },
        withAuth: true,
        immediate: false,
    });

    const renameFetch = useFetch(`/api/categories/${node.id}?name=${encodeURIComponent(editName)}`, {
        method: "PUT",
        withAuth: true,
        immediate: false,
    });

    const deleteFetch = useFetch(`/api/categories/${node.id}`, {
        method: "DELETE",
        withAuth: true,
        immediate: false,
    });

    const handleAdd = async (e) => {
        e.preventDefault();
        if (!newName.trim()) return;
        try {
            await createFetch.reFetch();
            setAdding(false);
            setNewName("");
            setNewIsLeaf(false);
            onRefresh();
        } catch {}
    };

    const handleRename = async (e) => {
        e.preventDefault();
        if (!editName.trim() || editName === node.name) { setEditing(false); return; }
        try {
            await renameFetch.reFetch();
            setEditing(false);
            onRefresh();
        } catch {}
    };

    const handleDelete = async () => {
        if (!confirm(`Delete "${node.name}" and all its subcategories?`)) return;
        try {
            await deleteFetch.reFetch();
            onRefresh();
        } catch {}
    };

    return (
        <div className={styles.node}>
            <div className={styles.row}>
                {!node.isLeaf ? (
                    <button className={styles.toggle} onClick={() => setOpen(p => !p)}>
                        {open ? "▼" : "►"}
                    </button>
                ) : (
                    <span className={styles.toggle} />
                )}

                {editing ? (
                    <form onSubmit={handleRename} className={styles.inlineForm} style={{ padding: 0, flex: 1 }}>
                        <input
                            type="text"
                            value={editName}
                            onChange={e => setEditName(e.target.value)}
                            autoFocus
                            onBlur={() => setEditing(false)}
                            onKeyDown={e => e.key === "Escape" && setEditing(false)}
                        />
                    </form>
                ) : (
                    <span className={styles.name}>
                        {node.name}
                        {node.isLeaf && <span className={styles.leaf}>leaf</span>}
                    </span>
                )}

                <div className={styles.actions}>
                    {!node.isLeaf && (
                        <button onClick={() => { setAdding(p => !p); setOpen(true); }}>+ Add</button>
                    )}
                    <button onClick={() => { setEditing(true); setEditName(node.name); }}>Edit</button>
                    <button className={styles.deleteBtn} onClick={handleDelete}>Delete</button>
                </div>
            </div>

            {open && (
                <div className={styles.children}>
                    {adding && (
                        <form onSubmit={handleAdd} className={styles.inlineForm}>
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
                    {node.children?.map(child => (
                        <CategoryAdminNode key={child.id} node={child} onRefresh={onRefresh} />
                    ))}
                </div>
            )}
        </div>
    );
}
