import { Link } from "react-router-dom";
import { paths } from "../routes.jsx"

function AdminMenu () {

    return (
        <div>
            <h1>Developer Menu</h1>
            <p>This is a placeholder for developer tools and options.</p>
            <Link to={paths.productList()}>Go to Product List</Link>
            <br />
            <Link to={paths.createProduct()}>Create Product</Link>
            <br />
            <Link to={paths.categoryTree()}>Category</Link>
        </div>
    )
}

export default AdminMenu;