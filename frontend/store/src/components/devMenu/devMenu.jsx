import {Link} from "react-router-dom";

function DevMenu () {

    return (
        <div>
            <h1>Developer Menu</h1>
            <p>This is a placeholder for developer tools and options.</p>
            <Link to="/product">Go to Product</Link>
        </div>
    )
}

export default DevMenu;