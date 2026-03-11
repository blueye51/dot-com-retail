import useFetch from "../../useFetch.js";
import {useEffect, useState} from "react";


export default function BrandCreate() {
    const [name, setName] = useState("")
    const [brands, setBrands] = useState([])
    const {data, reFetch: refetchBrands} = useFetch("/api/brands", {})

    useEffect(() => {
        if (data) setBrands(data);
    }, [data]);

    const {reFetch} = useFetch("/api/brands", {
        method: "POST",
        immediate: false,
        withAuth: true
    })

    const handleSubmit = async () => {
        if (!name?.trim()) {
            alert("name cannot be empty");
            return;
        }
        await reFetch({ body: { name } })

        setName("")
        refetchBrands()
    }

    return (
        <div>
            <input
                type="text"
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="Brand name"
            />
            <button onClick={handleSubmit}>Create</button>
            <p>brands:</p>
            {brands.length === 0
                ? <p>No brands yet</p>
                : brands.map((b) => (
                    <div key={b.id}>
                        <p>{b.name}</p>
                    </div>
                ))
            }
        </div>
    )
}