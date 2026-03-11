import useFetch from "../../useFetch.js";
import {useMemo, useState} from "react";
import {useSearchParams} from "react-router-dom";


export default function ProductGrid() {
    const [pageSize] = useState(24);
    const [pageNumber] = useState(0);

}