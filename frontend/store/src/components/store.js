import { configureStore, createSlice } from "@reduxjs/toolkit";

export function buildAuthFromToken(token) {
    let claims = {};
    try { claims = JSON.parse(atob(token.split(".")[1])); } catch {}
    return {
        token,
        roles: claims.roles || [],
        emailVerified: claims.emailVerified ?? false,
        twoFactorEnabled: claims["2fa"] ?? false,
    };
}

const authSlice = createSlice({
    name: "auth",
    initialState: { token: null, roles: [], emailVerified: false, twoFactorEnabled: false },
    reducers: {
        setAuth: (state, action) => {
            state.token = action.payload.token;
            state.roles = action.payload.roles;
            state.emailVerified = action.payload.emailVerified;
            state.twoFactorEnabled = action.payload.twoFactorEnabled;
        },
        logout: (state) => {
            state.token = null;
            state.roles = [];
            state.emailVerified = false;
            state.twoFactorEnabled = false;
        },
    }
})

const settingsSlice = createSlice({
    name: "settings",
    initialState: { imperialUnits: false },
    reducers: {
        setSettings: (state, action) => {
            state.imperialUnits = action.payload.imperialUnits ?? false;
        },
        clearSettings: () => ({ imperialUnits: false }),
    }
})

export const { setAuth, logout } = authSlice.actions;
export const { setSettings, clearSettings } = settingsSlice.actions;

const CART_STORAGE_KEY = "guest_cart";

function loadGuestCart() {
    try {
        const raw = localStorage.getItem(CART_STORAGE_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch {
        return [];
    }
}

function saveGuestCart(items) {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
}

export function clearGuestCart() {
    localStorage.removeItem(CART_STORAGE_KEY);
}

const cartSlice = createSlice({
    name: "cart",
    initialState: { items: loadGuestCart(), total: 0 },
    reducers: {
        setCart: (state, action) => {
            state.items = action.payload.items;
            state.total = action.payload.total;
        },
        addToGuestCart: (state, action) => {
            const { productId, productName, price, currency, quantity, stock, imageUrl } = action.payload;
            const existing = state.items.find(i => i.productId === productId);
            if (existing) {
                existing.quantity += quantity;
            } else {
                state.items.push({ productId, productName, price, currency, quantity, stock, imageUrl });
            }
            state.total = state.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
            saveGuestCart(state.items);
        },
        updateGuestCartQuantity: (state, action) => {
            const { productId, quantity } = action.payload;
            const item = state.items.find(i => i.productId === productId);
            if (item) {
                item.quantity = quantity;
            }
            state.total = state.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
            saveGuestCart(state.items);
        },
        removeFromGuestCart: (state, action) => {
            state.items = state.items.filter(i => i.productId !== action.payload);
            state.total = state.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
            saveGuestCart(state.items);
        },
        clearCart: (state) => {
            state.items = [];
            state.total = 0;
            saveGuestCart([]);
        },
    }
});

export const { setCart, addToGuestCart, updateGuestCartQuantity, removeFromGuestCart, clearCart } = cartSlice.actions;

export const store = configureStore({
    reducer: {
        auth: authSlice.reducer,
        settings: settingsSlice.reducer,
        cart: cartSlice.reducer,
    }
})