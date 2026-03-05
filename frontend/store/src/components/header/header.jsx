import styles from './header.module.css'
import {useState} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import {useLogout} from "../useLogout.js";

function Header () {
    const token = useSelector((state) => state.auth.token);
    const roles = useSelector((state) => state.auth.roles);
    const isAdmin = roles.includes("ADMIN");

    const [search, setSearch] = useState("");
    const navigate = useNavigate();
    const {handleLogout, loading} = useLogout();

    const handleSearch = (e) => {
        e.preventDefault();
        if (search.trim()) navigate(`${paths.productList()}?search=${encodeURIComponent(search.trim())}`);
    };

    return (
        <header className={styles.header}>
            <Link to={paths.home()} className={styles.logo}>LOGO</Link>

            <form className={styles.searchBar} onSubmit={handleSearch}>
                <input
                    className={styles.inputField}
                    id="search"
                    type="text"
                    placeholder="Search for products..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <button type="submit" className={styles.searchIcon}>🔍</button>
            </form>

            <nav className={styles.extraButtons}>
                {token ? (
                    <>
                        {isAdmin && <Link to={paths.admin()}>Admin Panel</Link>}
                        <button onClick={handleLogout} disabled={loading}>
                            {loading ? "Logging out..." : "Logout"}
                        </button>
                    </>
                ) : (
                    <>
                        <Link to={paths.login()}>Login</Link>
                        <Link to={paths.register()}>Register</Link>
                    </>
                )}
                <Link to="#">🛒</Link>
            </nav>
        </header>
    );
}

export default Header;
