import styles from './Header.module.css'
import {useState} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import {useLogout} from "../useLogout.js";
import defaultPfp from "../../assets/default_pfp.svg"

function Header () {
    const {roles, token, emailVerified} = useSelector((state) => state.auth);
    const isAdmin = roles.includes("ADMIN");

    const [search, setSearch] = useState("");
    const navigate = useNavigate();
    const {handleLogout, loading} = useLogout();

    const handleSearch = (e) => {
        e.preventDefault();
        if (search.trim()) navigate(`${paths.home()}?search=${encodeURIComponent(search.trim())}`);
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
                        {!emailVerified && <Link to={paths.verifyEmail()}>Verify Email</Link>}
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
                <Link to={paths.profile()}>
                    <img className={styles.profileImage} src={defaultPfp} alt="Profile image"/>
                </Link>
            </nav>
        </header>
    );
}

export default Header;
