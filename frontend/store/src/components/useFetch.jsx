import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {setRoles, setToken} from "./store.jsx";

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


// --- helpers ---
function isPlainObject(x) {
    return (
        x !== null &&
        typeof x === "object" &&
        (x.constructor === Object || Object.getPrototypeOf(x) === Object.prototype)
    );
}

/**
 * Encodes body + adjusts headers appropriately.
 * Returns { body, headers } (headers is a new object).
 */
function buildBodyAndHeaders(inputBody, inputHeaders = {}) {
    const h = { ...inputHeaders };

    if (inputBody === undefined || inputBody === null) {
        return { body: undefined, headers: h };
    }

    // FormData
    if (typeof FormData !== "undefined" && inputBody instanceof FormData) {
        // If caller set it, remove it to avoid boundary bugs
        for (const k of Object.keys(h)) {
            if (k.toLowerCase() === "content-type") delete h[k];
        }
        return { body: inputBody, headers: h };
    }

    // URLSearchParams
    if (typeof URLSearchParams !== "undefined" && inputBody instanceof URLSearchParams) {
        if (!Object.keys(h).some((k) => k.toLowerCase() === "content-type")) {
            h["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";
        }
        return { body: inputBody.toString(), headers: h };
    }

    // Blob/File/ArrayBuffer/TypedArrays
    if (
        (typeof Blob !== "undefined" && inputBody instanceof Blob) ||
        inputBody instanceof ArrayBuffer ||
        ArrayBuffer.isView(inputBody)
    ) {
        return { body: inputBody, headers: h };
    }

    // String
    if (typeof inputBody === "string") {
        return { body: inputBody, headers: h };
    }

    // Plain object: JSON encode
    if (isPlainObject(inputBody) || Array.isArray(inputBody)) {
        if (!Object.keys(h).some((k) => k.toLowerCase() === "content-type")) {
            h["Content-Type"] = "application/json";
        }
        return { body: JSON.stringify(inputBody), headers: h };
    }

    return { body: inputBody, headers: h };
}

async function parseResponse(res, parseAs = "auto") {
    if (res.status === 204) return null;

    if (parseAs === "blob") return res.blob();
    if (parseAs === "arrayBuffer") return res.arrayBuffer();
    if (parseAs === "text") return res.text();
    if (parseAs === "json") return res.json();

    // auto
    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) return res.json();
    return res.text();
}


/**
 * useFetch
 * @param {string} uri - "/api/..."
 * @param {object} options
 * @returns { data, error, loading, refetch, abort }
 */
export default function useFetch(
    uri,
    {
        method = "GET",
        body,
        headers,
        withAuth = false,
        immediate = true,
        deps = [], // to control when auto-refetch happens
        parseAs = "auto", // "auto" | "json" | "text" | "blob" | "arrayBuffer"
        credentials = "include",
    } = {}
) {
    const dispatch = useDispatch();
    const token = useSelector((s) => s.auth.token);

    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const abortRef = useRef(null);
    const mountedRef = useRef(true);

    useEffect(() => {
        mountedRef.current = true;
        return () => {
            mountedRef.current = false;
            abortRef.current?.abort();
        };
    }, []);

    const doFetch = useCallback(
        async (override = {}) => {

            const ctrl = new AbortController();
            abortRef.current = ctrl;


            if (mountedRef.current) {
                setLoading(true);
                setError(null);
            }

            const effectiveMethod = (override.method || method).toUpperCase();
            const canHaveBody = !["GET", "HEAD"].includes(effectiveMethod);


            // token for this request (may update after refresh)
            let authToken = override.token ?? token;

            const makeRequestInit = (jwt) => {
                const mergedHeaders = {
                    ...(headers || {}),
                    ...(override.headers || {}),
                    ...(withAuth && jwt ? { Authorization: `Bearer ${jwt}` } : {}),
                };

                const chosenBody = override.body !== undefined ? override.body : body;
                const { body: encodedBody, headers: finalHeaders } = canHaveBody
                    ? buildBodyAndHeaders(chosenBody, mergedHeaders)
                    : { body: undefined, headers: mergedHeaders };

                return {
                    method: effectiveMethod,
                    headers: finalHeaders,
                    signal: ctrl.signal,
                    ...(canHaveBody && encodedBody !== undefined ? { body: encodedBody } : {}),
                };
            };

            const url = `${BASE_URL}${uri}`;

            try {
                let res = await fetch(url, {
                    ...makeRequestInit(authToken),
                    credentials,
                });

                // Refresh once on 401
                if (res.status === 401 && withAuth) {
                    const newToken = await refreshAccessToken(dispatch);
                    authToken = newToken;

                    res = await fetch(url, {
                        ...makeRequestInit(authToken), // rebuild init for safety
                        credentials,
                    });
                }

                if (!res.ok) {
                    // read text safely for diagnostics (may be empty)
                    let details = "";
                    try {
                        details = (await res.text()).slice(0, 500);
                    } catch {}
                    throw new Error(`HTTP ${res.status}${details ? ` – ${details}` : ""}`);
                }

                const payload = await parseResponse(
                    res,
                    override.parseAs ?? parseAs
                );

                if (mountedRef.current) setData(payload);
                return payload;
            } catch (err) {
                if (mountedRef.current) setError(err);
                throw err;
            } finally {
                if (mountedRef.current) setLoading(false);
            }
        },
        [uri, method, body, headers, withAuth, token, dispatch, parseAs, credentials]
    );

    // Auto-run if requested
    useEffect(() => {
        if (!immediate) return;
        doFetch().catch(() => {});
    }, [doFetch, immediate, ...deps]);

    const abort = useCallback(() => {
        abortRef.current?.abort();
    }, []);

    return { data, error, loading, reFetch: doFetch, abort };
}
