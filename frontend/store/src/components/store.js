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

export const { setAuth, logout } = authSlice.actions;

export const store = configureStore({
    reducer: {
        auth: authSlice.reducer
    }
})