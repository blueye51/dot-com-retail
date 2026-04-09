import styles from './Header.module.css'
import {useState, useRef, useEffect} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import {useLogout} from "../useLogout.js";
import defaultPfp from "../../assets/default_pfp.svg"

function Header () {
    const {roles, token, emailVerified} = useSelector((state) => state.auth);
    const cartItems = useSelector((s) => s.cart.items);
    const cartCount = cartItems.reduce((sum, i) => sum + i.quantity, 0);
    const isAdmin = roles.includes("ADMIN");

    const [search, setSearch] = useState("");
    const [cartOpen, setCartOpen] = useState(false);
    const cartRef = useRef(null);
    const navigate = useNavigate();
    const {handleLogout, loading} = useLogout();

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (cartRef.current && !cartRef.current.contains(e.target)) {
                setCartOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

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
                        <Link to={paths.orders()}>Orders</Link>
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
                <div className={styles.cartWrapper} ref={cartRef}>
                    <button
                        className={styles.cartLink}
                        onClick={() => setCartOpen(prev => !prev)}
                    >
                        🛒
                        {cartCount > 0 && <span className={styles.cartBadge}>{cartCount}</span>}
                    </button>
                    {cartOpen && (
                        <div className={styles.cartDropdown}>
                            {cartItems.length === 0 ? (
                                <p className={styles.emptyCart}>Your cart is empty</p>
                            ) : (
                                <>
                                    <div className={styles.cartItems}>
                                        {cartItems.slice(0, 5).map((item) => (
                                            <div key={item.productId} className={styles.cartItem}>
                                                {item.imageUrl && (
                                                    <img src={item.imageUrl} alt="" className={styles.cartItemImg} />
                                                )}
                                                <div className={styles.cartItemInfo}>
                                                    <span className={styles.cartItemName}>{item.productName}</span>
                                                    <span className={styles.cartItemQty}>
                                                        {item.quantity} × ${item.price.toFixed(2)}
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                        {cartItems.length > 5 && (
                                            <p className={styles.moreItems}>+{cartItems.length - 5} more items</p>
                                        )}
                                    </div>
                                    <button
                                        className={styles.goToCartBtn}
                                        onClick={() => { setCartOpen(false); navigate(paths.cart()); }}
                                    >
                                        Go to Cart
                                    </button>
                                </>
                            )}
                        </div>
                    )}
                </div>
                <Link to={paths.profile()}>
                    <img className={styles.profileImage} src={defaultPfp} alt="Profile image"/>
                </Link>
            </nav>
        </header>
    );
}

export default Header;
