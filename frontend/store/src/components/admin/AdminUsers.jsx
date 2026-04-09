import {useState} from "react";
import {useSelector} from "react-redux";
import useFetch from "../useFetch.js";
import styles from "./AdminUsers.module.css";
import ErrorMessage from "../error/ErrorMessage.jsx";
import {Helmet} from "react-helmet-async";

const BASE_URL = import.meta.env.VITE_API_BASE;

export default function AdminUsers() {
    const {token} = useSelector((s) => s.auth);
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(0);
    const [toggling, setToggling] = useState(null);

    const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
    const {data, loading, error, reFetch} = useFetch(
        `/api/admin/users?page=${page}&size=20${searchParam}`,
        {withAuth: true, deps: [page, search]}
    );

    const users = data?.content || [];
    const totalPages = data?.totalPages || 0;

    const handleToggleAdmin = async (userId, hasAdmin) => {
        setToggling(userId);
        try {
            const endpoint = hasAdmin ? "revoke-admin" : "grant-admin";
            const res = await fetch(`${BASE_URL}/api/admin/users/${userId}/${endpoint}`, {
                method: "POST",
                headers: {Authorization: `Bearer ${token}`},
                credentials: "include",
            });
            if (res.ok) {
                await reFetch();
            } else {
                alert("Failed to update role");
            }
        } catch {
            alert("Failed to update role");
        } finally {
            setToggling(null);
        }
    };

    return (
        <div className={styles.page}>
            <Helmet><title>User Management - Admin</title></Helmet>
            <h1>User Management</h1>

            <div className={styles.searchBar}>
                <input
                    type="text"
                    placeholder="Search by name or email..."
                    value={search}
                    onChange={(e) => {setSearch(e.target.value); setPage(0);}}
                />
            </div>

            {loading && <p>Loading...</p>}
            {error && <ErrorMessage message="Failed to load users." />}
            {!loading && users.length === 0 && <p>No users found.</p>}

            <table className={styles.table}>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Provider</th>
                        <th>Verified</th>
                        <th>Roles</th>
                        <th>Joined</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((user) => {
                        const hasAdmin = user.roles.includes("ADMIN");
                        return (
                            <tr key={user.id}>
                                <td>{user.name}</td>
                                <td className={styles.email}>{user.email}</td>
                                <td>{user.provider}</td>
                                <td>{user.emailVerified ? "Yes" : "No"}</td>
                                <td>
                                    {user.roles.map(r => (
                                        <span key={r} className={`${styles.roleBadge} ${r === "ADMIN" ? styles.adminBadge : ""}`}>
                                            {r}
                                        </span>
                                    ))}
                                </td>
                                <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                                <td>
                                    <button
                                        className={hasAdmin ? styles.revokeBtn : styles.grantBtn}
                                        disabled={toggling === user.id}
                                        onClick={() => handleToggleAdmin(user.id, hasAdmin)}
                                    >
                                        {toggling === user.id ? "..." : hasAdmin ? "Revoke Admin" : "Grant Admin"}
                                    </button>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>

            {totalPages > 1 && (
                <div className={styles.pagination}>
                    <button onClick={() => setPage(p => p - 1)} disabled={page === 0}>Previous</button>
                    <span>Page {page + 1} of {totalPages}</span>
                    <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>Next</button>
                </div>
            )}
        </div>
    );
}
