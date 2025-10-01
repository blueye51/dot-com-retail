import { configureStore, createSlice } from "@reduxjs/toolkit";

const authSlice = createSlice({
    name: "auth",
    initialState: { token: null },
    reducers: {
        setToken: (state, action) => {
            state.token = action.payload;
        },
        clearToken: (state) => {
            state.token = null;
        }
    }
})

export const { setToken, clearToken } = authSlice.actions;

export const store = configureStore({
    reducer: {
        auth: authSlice.reducer
    }
})