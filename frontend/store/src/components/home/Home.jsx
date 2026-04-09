import {Helmet} from "react-helmet-async";
import ProductGrid from "../product/productList/ProductGrid.jsx";

export default function Home () {
    return(
        <>
            <Helmet>
                <title>Electronics Store - Fast Delivery, Cheap Prices</title>
                <meta name="description" content="Shop the latest electronics at unbeatable prices. Laptops, phones, headphones, and more with fast delivery." />
            </Helmet>
            <ProductGrid/>
        </>
    )
}
