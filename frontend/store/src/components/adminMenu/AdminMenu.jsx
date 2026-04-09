import { Link } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import { paths } from "../routes.js"

function AdminMenu () {

    return (
        <div>
            <Helmet>
                <title>Admin Panel - Electronics Store</title>
            </Helmet>
            <h1>Admin Panel</h1>
            <p>This is a placeholder for a admin control panel.</p>
            <Link to={paths.productList()}>Go to Product List</Link>
            <br />
            <Link to={paths.createProduct()}>Create Product</Link>
            <br />
            <Link to={paths.categoryAdmin()}>Manage Categories</Link>
            <br />
            <Link to={paths.brandCreate()}>Create brand</Link>
            <br />
            <Link to={paths.adminOrders()}>Manage Orders</Link>
            <br />
            <Link to={paths.adminUsers()}>Manage Users</Link>
            <br />
            <Link to={paths.bulkUpload()}>Bulk Product Upload</Link>
            <br />
            <Link to={paths.adminReviews()}>Moderate Reviews</Link>
        </div>
    )
}

export default AdminMenu;