import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {setRoles, setToken} from "./store.jsx";

// ---- config you might hoist to env ----
const BASE_URL = "https://localhost:8443";

// Deduplicate concurrent refresh calls across the app
let refreshInFlight = null;

export async function refreshAccessToken(dispatch) {
    if (!refreshInFlight) {
        refreshInFlight = (async () => {
            const res = await fetch(`${BASE_URL}/api/auth/refresh`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
            });
            if (!res.ok) throw new Error("Failed to refresh token");
            const data = await res.json();
            const newToken = data.accessToken;
            const newRoles = data.roles || [];
            dispatch(setToken(newToken));
            dispatch(setRoles(newRoles));
            return newToken;
        })().finally(() => {
            refreshInFlight = null;
        });
    }
    return refreshInFlight;
}

/**
 * useFetch
 * @param {string} uri - "/api/..."
 * @param {object} options - { method, body, headers, withAuth, immediate }
 * @returns { data, error, loading, refetch, abort }
 */
export default function useFetch(
    uri,
    {
        method = "GET",
        body = undefined,
        headers = {},
        withAuth = false, // include auth token
        immediate = true, // auto-run on mount/dep change
    } = {}
) {
    const dispatch = useDispatch();
    const token = useSelector((s) => s.auth.token);
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const abortRef = useRef(null);
    const mountedRef = useRef(true);

    // Memoize body and headers to avoid unnecessary re-fetches, so i can keep them in deps
    const bodyMemo = useMemo(() => body, [JSON.stringify(body||null)]);
    const headersMemo = useMemo(() => headers, [JSON.stringify(headers||null)]);

    useEffect(() => {
        mountedRef.current = true;
        return () => {
            mountedRef.current = false;
            if (abortRef.current) abortRef.current.abort();
        };
    }, []);

    const doFetch = useCallback(async (override = {}) => {
            const ctrl = new AbortController();
            abortRef.current = ctrl;

            setLoading(true);
            setError(null);

            const effectiveMethod = override.method || method;
            const canHaveBody = !["GET", "HEAD"].includes(
                effectiveMethod.toUpperCase()
            );

            // compute token once (may be overridden after refresh)
            let authToken = override.token || token;

            const makeReq = async (jwt) => {
                const reqHeaders = {
                    "Content-Type": "application/json",
                    ...headersMemo,
                    ...(override.headers || {}),
                    ...(withAuth && jwt ? { Authorization: `Bearer ${jwt}` } : {}),
                };

                const reqInit = {
                    method: effectiveMethod,
                    headers: reqHeaders,
                    signal: ctrl.signal,
                    // Only attach body if allowed and provided
                    ...(canHaveBody &&
                        (override.body !== undefined ? { body: JSON.stringify(override.body) } :
                            bodyMemo !== undefined ? { body: JSON.stringify(bodyMemo) } : {})),
                };

                const res = await fetch(`${BASE_URL}${uri}`, {
                    ...reqInit,
                    credentials: "include",
                });

                if (res.status === 401 && withAuth) {
                    // try refresh once
                    const newToken = await refreshAccessToken(dispatch);
                    authToken = newToken; // update for retry
                    const retryHeaders = {
                        ...reqHeaders,
                        Authorization: `Bearer ${newToken}`,
                    };
                    const retryRes = await fetch(`${BASE_URL}${uri}`, {
                        ...reqInit,
                        headers: retryHeaders,
                        credentials: "include",
                    });
                    return retryRes;
                }
                return res;
            };

            try {
                const res = await makeReq(authToken);

                if (!res.ok) {
                    // Try to extract error text/json for better messages
                    let details = "";
                    try {
                        const txt = await res.text();
                        details = txt?.slice(0, 500);
                    } catch (_) {}
                    throw new Error(`HTTP ${res.status}${details ? ` – ${details}` : ""}`);
                }

                let payload = null;
                if (res.status !== 204) {
                    // May not always be JSON
                    const ct = res.headers.get("content-type") || "";
                    payload = ct.includes("application/json") ? await res.json() : await res.text();
                }

                if (mountedRef.current) setData(payload ?? null);
                return payload;
            } catch (err) {
                if (mountedRef.current) setError(err);
                throw err;
            } finally {
                if (mountedRef.current) setLoading(false);
            }
        },
        [uri, method, bodyMemo, headersMemo, withAuth, token, dispatch]
    );

    // Auto-run if requested
    useEffect(() => {
        if (!immediate) return;
        doFetch().catch(() => {});
    }, [doFetch, immediate]);

    const abort = useCallback(() => {
        if (abortRef.current) abortRef.current.abort();
    }, []);

    return { data, error, loading, reFetch: doFetch, abort };
}
