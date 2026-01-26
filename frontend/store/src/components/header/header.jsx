import styles from './header.module.css'
import {useState} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useSelector} from "react-redux";



function Header () {
    const token = useSelector((state) => state.auth.token);
    const [search, setSearch] = useState("");
    const navigate = useNavigate();

    function handleSearch() {
        console.log("Searching for:", search);
    }

    return (
        <header>
            <h1 onClick={() => navigate("/")}>LOGO</h1>
            <div className={styles.searchBar}>
                <input className={styles.inputField}
                       id="search"
                       autoComplete="off"
                       type="text"
                       placeholder="Search for products..."
                       value={search}
                       onChange={(e) => setSearch(e.target.value)}
                />
                <span onClick={handleSearch} className={styles.searchIcon} >🔍</span>
            </div>
            {token && <div>AUTH</div>}
            <div className={styles.extraButtons}>
                <Link to="/login">Login</Link>
                <Link to="/dev">dev</Link>
                <h1>🛒</h1>
            </div>
        </header>
    )
}

export default Header