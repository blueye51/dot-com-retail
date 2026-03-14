import { Link } from "react-router-dom";
import { paths } from "../routes.js"

function AdminMenu () {

    return (
        <div>
            <h1>Admin Panel</h1>
            <p>This is a placeholder for a admin control panel.</p>
            <Link to={paths.productList()}>Go to Product List</Link>
            <br />
            <Link to={paths.createProduct()}>Create Product</Link>
            <br />
            <Link to={paths.categoryAdmin()}>Manage Categories</Link>
            <br />
            <Link to={paths.brandCreate()}>Create brand</Link>
        </div>
    )
}

export default AdminMenu;