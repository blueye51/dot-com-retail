import styles from './header.module.css'
import {useState} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useDispatch, useSelector} from "react-redux";
import {logout} from "../store.jsx";
import useFetch from "../useFetch.jsx";



function Header () {
    const token = useSelector((state) => state.auth.token);
    const [search, setSearch] = useState("");
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const {data, error, loading, reFetch, abort} = useFetch("/api/auth/refresh/logout", {
        method: "DELETE",
        withAuth: true,
        immediate: false,
    })

    function handleSearch() {
        console.log("Searching for:", search);
    }

    const handleLogout = async () => {
        try {
            await reFetch();
        } catch (e) {
            alert(e)
            return;
        }
        dispatch(logout());
        navigate("/login", { replace: true });
    };

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
                <button onClick={handleLogout}>Logout</button>
                <Link to="/admin">admin</Link>
                <h1>🛒</h1>
            </div>
        </header>
    )
}

export default Header