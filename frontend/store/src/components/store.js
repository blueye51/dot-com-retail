import { configureStore, createSlice } from "@reduxjs/toolkit";

const authSlice = createSlice({
    name: "auth",
    initialState: { token: null, roles: [], emailVerified: false },
    reducers: {
        setAuth: (state, action) => {
            state.token = action.payload.token;
            state.roles = action.payload.roles;
            state.emailVerified = action.payload.emailVerified;
        },
        logout: (state) => {
            state.token = null;
            state.roles = [];
            state.emailVerified = false;
        },
    }
})

export const { setAuth, logout } = authSlice.actions;

export const store = configureStore({
    reducer: {
        auth: authSlice.reducer
    }
})