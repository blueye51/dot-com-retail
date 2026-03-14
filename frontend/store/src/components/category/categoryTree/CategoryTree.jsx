import CategoryNode from "../CategoryNode.jsx";
import {useMemo} from "react";
import useFetch from "../../useFetch.js";

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

export default function CategoryTree({ onSelect }) {
    const {data, reFetch, loading} = useFetch("/api/categories")

    const tree = useMemo(() => buildTree(data || []), [data]);

    if (loading) {
        return (
            <div>Loading categories</div>
        )
    }

    return (
        <div>
            {tree.length === 0
                ? <p>No categories found</p>
                : tree.map(node => (
                    <CategoryNode key={node.id} node={node} onSelect={onSelect} />
                ))
            }
        </div>
    );
}