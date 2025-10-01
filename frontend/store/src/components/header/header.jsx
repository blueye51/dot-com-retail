import styles from './header.module.css'
import {useState} from "react";


function Header () {
    const [search, setSearch] = useState("");

    function handleSearch() {
        console.log("Searching for:", search);
    }

    return (
        <header>
            <h1>LOGO</h1>
            <div className={styles.searchBar}>
                <input className={styles.inputField}
                       id="search"
                       autoComplete="off"
                       type="text"
                       placeholder="Search for products..."
                       value={search}
                       onChange={(e) => setSearch(e.target.value)}/>
                <span onClick={handleSearch} className={styles.searchIcon} >🔍</span>
            </div>
            <h1>🛒</h1>
        </header>
    )
}

export default Header