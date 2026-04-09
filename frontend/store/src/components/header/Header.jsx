import styles from './Header.module.css'
import {useState, useRef, useEffect, useCallback} from "react";
import {Link, useNavigate} from 'react-router-dom';
import {useSelector} from "react-redux";
import {paths} from "../routes.js";
import {useLogout} from "../useLogout.js";
import defaultPfp from "../../assets/default_pfp.svg"

const BASE_URL = import.meta.env.VITE_API_BASE;

function IconSearch() {
    return (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
    );
}

function IconCart() {
    return (
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
    );
}

function IconMenu() {
    return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
    );
}

function IconClose() {
    return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
    );
}

function Header () {
    const {roles, token, emailVerified} = useSelector((state) => state.auth);
    const cartItems = useSelector((s) => s.cart.items);
    const cartCount = cartItems.reduce((sum, i) => sum + i.quantity, 0);
    const isAdmin = roles.includes("ADMIN");

    const [search, setSearch] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [cartOpen, setCartOpen] = useState(false);
    const [menuOpen, setMenuOpen] = useState(false);
    const cartRef = useRef(null);
    const searchRef = useRef(null);
    const debounceRef = useRef(null);
    const navigate = useNavigate();
    const {handleLogout, loading} = useLogout();

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (cartRef.current && !cartRef.current.contains(e.target)) setCartOpen(false);
            if (searchRef.current && !searchRef.current.contains(e.target)) setShowSuggestions(false);
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // Close drawer on route change
    useEffect(() => { setMenuOpen(false); }, [navigate]);

    const fetchSuggestions = useCallback((query) => {
        if (!query || query.length < 2) { setSuggestions([]); setShowSuggestions(false); return; }
        fetch(`${BASE_URL}/api/products/page?search=${encodeURIComponent(query)}&size=5&page=0`)
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (data?.content) { setSuggestions(data.content); setShowSuggestions(data.content.length > 0); }
            })
            .catch(() => {});
    }, []);

    const handleInputChange = (e) => {
        const value = e.target.value;
        setSearch(value);
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => fetchSuggestions(value.trim()), 300);
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setShowSuggestions(false);
        if (search.trim()) navigate(`${paths.home()}?search=${encodeURIComponent(search.trim())}`);
    };

    const handleSuggestionClick = (productId) => {
        setShowSuggestions(false);
        setSearch("");
        navigate(paths.product(productId));
    };

    const navLinks = (
        <>
            {token ? (
                <>
                    {isAdmin && <Link to={paths.admin()} className={styles.navLink}>Admin</Link>}
                    <Link to={paths.orders()} className={styles.navLink}>Orders</Link>
                    {!emailVerified && <Link to={paths.verifyEmail()} className={styles.navLinkWarn}>Verify Email</Link>}
                    <button onClick={handleLogout} disabled={loading} className={styles.navBtn}>
                        {loading ? "…" : "Logout"}
                    </button>
                </>
            ) : (
                <>
                    <Link to={paths.login()} className={styles.navLink}>Login</Link>
                    <Link to={paths.register()} className={styles.navBtnPrimary}>Register</Link>
                </>
            )}
        </>
    );

    return (
        <>
            <header className={styles.header}>
                <div className={styles.inner}>

                    {/* Logo */}
                    <Link to={paths.home()} className={styles.logo}>
                        <span className={styles.logoBolt}>⚡</span>
                        <span className={styles.logoText}>ElectroStore</span>
                    </Link>

                    {/* Search */}
                    <div className={styles.searchWrapper} ref={searchRef}>
                        <form className={styles.searchForm} onSubmit={handleSearch}>
                            <span className={styles.searchIconWrap}><IconSearch /></span>
                            <input
                                className={styles.searchInput}
                                type="text"
                                placeholder="Search products…"
                                value={search}
                                onChange={handleInputChange}
                                onFocus={() => { if (suggestions.length > 0) setShowSuggestions(true); }}
                                autoComplete="off"
                            />
                            <button type="submit" className={styles.searchBtn}>Search</button>
                        </form>
                        {showSuggestions && (
                            <div className={styles.suggestionsDropdown}>
                                {suggestions.map((p) => (
                                    <button key={p.id} className={styles.suggestionItem} onClick={() => handleSuggestionClick(p.id)}>
                                        {p.imageUrl && <img src={p.imageUrl} alt={p.name} className={styles.suggestionImg} />}
                                        <div className={styles.suggestionInfo}>
                                            <span className={styles.suggestionName}>{p.name}</span>
                                            <span className={styles.suggestionPrice}>{Number(p.price).toFixed(2)} {p.currency}</span>
                                        </div>
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Right actions */}
                    <div className={styles.actions}>
                        {/* Desktop nav links */}
                        <nav className={styles.desktopNav}>{navLinks}</nav>

                        {/* Cart */}
                        <div className={styles.cartWrapper} ref={cartRef}>
                            <button className={styles.iconBtn} onClick={() => setCartOpen(prev => !prev)} aria-label="Cart">
                                <IconCart />
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
                                                        {item.imageUrl && <img src={item.imageUrl} alt={item.productName} className={styles.cartItemImg} />}
                                                        <div className={styles.cartItemInfo}>
                                                            <span className={styles.cartItemName}>{item.productName}</span>
                                                            <span className={styles.cartItemQty}>{item.quantity} × {Number(item.price).toFixed(2)} {item.currency}</span>
                                                        </div>
                                                    </div>
                                                ))}
                                                {cartItems.length > 5 && <p className={styles.moreItems}>+{cartItems.length - 5} more</p>}
                                            </div>
                                            <button className={styles.goToCartBtn} onClick={() => { setCartOpen(false); navigate(paths.cart()); }}>
                                                Go to Cart
                                            </button>
                                        </>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Profile */}
                        <Link to={paths.profile()} className={styles.profileLink}>
                            <img className={styles.profileImage} src={defaultPfp} alt="Profile" />
                        </Link>

                        {/* Hamburger (mobile only) */}
                        <button className={styles.hamburger} onClick={() => setMenuOpen(o => !o)} aria-label="Menu">
                            {menuOpen ? <IconClose /> : <IconMenu />}
                        </button>
                    </div>

                </div>
            </header>

            {/* Mobile drawer */}
            <div className={`${styles.drawer} ${menuOpen ? styles.drawerOpen : ""}`}>
                <nav className={styles.drawerNav}>
                    {navLinks}
                </nav>
            </div>
            {menuOpen && <div className={styles.drawerOverlay} onClick={() => setMenuOpen(false)} />}
        </>
    );
}

export default Header;
