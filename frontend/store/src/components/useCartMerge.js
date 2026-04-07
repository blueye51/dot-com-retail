import {useDispatch} from "react-redux";
import {setCart, clearGuestCart} from "./store.js";

const BASE_URL = import.meta.env.VITE_API_BASE;

export async function mergeGuestCart(dispatch, accessToken) {
    const raw = localStorage.getItem("guest_cart");
    if (!raw) return;

    let guestItems;
    try {
        guestItems = JSON.parse(raw);
    } catch {
        return;
    }

    if (!Array.isArray(guestItems) || guestItems.length === 0) return;

    const items = guestItems.map(i => ({productId: i.productId, quantity: i.quantity}));

    try {
        const res = await fetch(`${BASE_URL}/api/cart/merge`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${accessToken}`,
            },
            credentials: "include",
            body: JSON.stringify({items}),
        });
        if (res.ok) {
            const cart = await res.json();
            dispatch(setCart(cart));
        }
    } catch {}

    clearGuestCart();
}
