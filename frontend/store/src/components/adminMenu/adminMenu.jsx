import {Link} from "react-router-dom";

function AdminMenu () {

    return (
        <div>
            <h1>Developer Menu</h1>
            <p>This is a placeholder for developer tools and options.</p>
            <Link to="/product">Go to Product List</Link>
            <br />
            <Link to="/createproduct">Create Product</Link>
            <br />
            <Link to="/categorytree">Category</Link>
        </div>
    )
}

export default AdminMenu;