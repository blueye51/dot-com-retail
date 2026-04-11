import { Link } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import { paths } from "../routes.js"
import styles from "./AdminMenu.module.css";

const cards = [
    {
        to: paths.productList(),
        icon: "📦",
        title: "Product List",
        desc: "View, edit and delete products",
    },
    {
        to: paths.createProduct(),
        icon: "➕",
        title: "Create Product",
        desc: "Add a new product to the store",
    },
    {
        to: paths.bulkUpload(),
        icon: "📤",
        title: "Bulk Upload",
        desc: "Import products via CSV or JSON",
    },
    {
        to: paths.categoryAdmin(),
        icon: "🗂️",
        title: "Categories",
        desc: "Manage the category tree",
    },
    {
        to: paths.brandCreate(),
        icon: "🏷️",
        title: "Brands",
        desc: "Create and manage brands",
    },
    {
        to: paths.adminOrders(),
        icon: "🛒",
        title: "Orders",
        desc: "View and update order statuses",
    },
    {
        to: paths.adminUsers(),
        icon: "👥",
        title: "Users",
        desc: "Search users and manage roles",
    },
    {
        to: paths.adminReviews(),
        icon: "⭐",
        title: "Reviews",
        desc: "Moderate and remove reviews",
    },
];

function AdminMenu() {
    return (
        <div className={styles.page}>
            <Helmet>
                <title>Admin Panel - Electronics Store</title>
            </Helmet>

            <div className={styles.header}>
                <h1 className={styles.title}>Admin Panel</h1>
                <p className={styles.subtitle}>Manage your store from one place</p>
            </div>

            <div className={styles.grid}>
                {cards.map((card) => (
                    <Link key={card.to} to={card.to} className={styles.card}>
                        <span className={styles.icon}>{card.icon}</span>
                        <span className={styles.cardTitle}>{card.title}</span>
                        <span className={styles.cardDesc}>{card.desc}</span>
                    </Link>
                ))}
            </div>
        </div>
    );
}

export default AdminMenu;
