import { configureStore, createSlice } from "@reduxjs/toolkit";

const authSlice = createSlice({
    name: "auth",
    initialState: { token: null, roles: [] },
    reducers: {
        setToken: (state, action) => {
            state.token = action.payload;
        },
        clearToken: (state) => {
            state.token = null;
        },
        setRoles: (state, action) => {
            state.roles = action.payload;
        },
    }
})

export const { setToken, clearToken, setRoles } = authSlice.actions;

export const store = configureStore({
    reducer: {
        auth: authSlice.reducer
    }
})