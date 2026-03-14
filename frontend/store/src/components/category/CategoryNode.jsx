import { useState } from "react";

export default function CategoryNode({ node, onSelect }) {
    const [open, setOpen] = useState(false);

    const handleClick = () => {
        if (node.isLeaf) {
            onSelect(node.id);
        } else {
            setOpen(prev => !prev);
        }
    };

    return (
        <div>
            <div onClick={handleClick} style={{ cursor: "pointer", userSelect: "none" }}>
                {!node.isLeaf && <span>{open ? "▼" : "►"} </span>}
                {node.name}
            </div>

            {open && node.children?.map(child => (
                <div key={child.id} style={{ paddingLeft: "1rem" }}>
                    <CategoryNode node={child} onSelect={onSelect} />
                </div>
            ))}
        </div>
    );
}
