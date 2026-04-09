import http from "k6/http";
import { check, sleep, group } from "k6";
import { SharedArray } from "k6/data";
import { Rate, Trend } from "k6/metrics";

// ─── Custom metrics ───
const errorRate = new Rate("errors");
const browseLatency = new Trend("browse_latency", true);
const authLatency = new Trend("auth_latency", true);
const cartLatency = new Trend("cart_latency", true);
const adminLatency = new Trend("admin_latency", true);

// ─── Config ───
// Override with: k6 run -e BASE_URL=https://localhost:8443 load-test.js
const BASE = __ENV.BASE_URL || "https://localhost:8443";

// Seeded test users (from DataSeeder.java)
const TEST_USER = { email: "user@store.dev", password: "User@123" };
const ADMIN_USER = { email: __ENV.ADMIN_EMAIL || "eric.rand66@gmail.com", password: __ENV.ADMIN_PASSWORD || "Eric@123" };

// Cloudflare Turnstile always-pass test token
const TURNSTILE_TOKEN = "test-token";

// ─── Scenarios ───
export const options = {
    insecureSkipTLSVerify: true,
    scenarios: {
        // Scenario 1: Public browsing — heaviest traffic
        browse: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "15s", target: 20 },  // ramp up
                { duration: "30s", target: 20 },  // sustain
                { duration: "10s", target: 0 },   // ramp down
            ],
            exec: "browseProducts",
        },
        // Scenario 2: Authenticated user flow — login + cart
        userFlow: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "10s", target: 5 },
                { duration: "30s", target: 5 },
                { duration: "10s", target: 0 },
            ],
            exec: "authenticatedUserFlow",
            startTime: "5s", // slight delay to let browse warm up
        },
        // Scenario 3: Auth endpoint stress — tests rate limiting
        authStress: {
            executor: "constant-arrival-rate",
            rate: 15,             // 15 requests/sec — should trigger 429s
            timeUnit: "1s",
            duration: "20s",
            preAllocatedVUs: 10,
            exec: "authEndpointStress",
            startTime: "10s",
        },
        // Scenario 4: Admin operations — low traffic, heavier queries
        admin: {
            executor: "constant-vus",
            vus: 2,
            duration: "30s",
            exec: "adminFlow",
            startTime: "10s",
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<2000"],       // 95th percentile under 2s
        errors: ["rate<0.3"],                     // less than 30% errors (auth stress will push 429s)
        browse_latency: ["p(95)<1500"],           // browse should be fast
        cart_latency: ["p(95)<2000"],
    },
};

// ─── Helper: login and return token ───
function login(email, password) {
    const res = http.post(
        `${BASE}/api/auth/login`,
        JSON.stringify({ email, password, turnstileToken: TURNSTILE_TOKEN }),
        { headers: { "Content-Type": "application/json" } }
    );
    if (res.status === 200) {
        try {
            const body = JSON.parse(res.body);
            return body.accessToken || null;
        } catch {
            return null;
        }
    }
    return null;
}

function authHeaders(token) {
    return { headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" } };
}

// ─── Scenario 1: Browse products (public) ───
export function browseProducts() {
    group("Browse Products", () => {
        // Home page — product grid
        let res = http.get(`${BASE}/api/products/page?page=0&size=24`);
        browseLatency.add(res.timings.duration);
        check(res, { "product list 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);

        // Browse with filters
        res = http.get(`${BASE}/api/products/page?page=0&size=24&sort=price&descending=false`);
        browseLatency.add(res.timings.duration);
        check(res, { "filtered list 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.3);

        // Load categories
        res = http.get(`${BASE}/api/categories`);
        browseLatency.add(res.timings.duration);
        check(res, { "categories 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.3);

        // Load brands
        res = http.get(`${BASE}/api/brands`);
        browseLatency.add(res.timings.duration);
        check(res, { "brands 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.3);

        // View a specific product (grab first product ID from the list)
        const listRes = http.get(`${BASE}/api/products/page?page=0&size=5`);
        if (listRes.status === 200) {
            try {
                const products = JSON.parse(listRes.body).content;
                if (products && products.length > 0) {
                    const productId = products[Math.floor(Math.random() * products.length)].id;

                    res = http.get(`${BASE}/api/products/${productId}`);
                    browseLatency.add(res.timings.duration);
                    check(res, { "product detail 200": (r) => r.status === 200 }) || errorRate.add(1);

                    sleep(0.3);

                    // Load reviews for that product
                    res = http.get(`${BASE}/api/ratings/product/${productId}/reviews`);
                    browseLatency.add(res.timings.duration);
                    check(res, { "reviews 200": (r) => r.status === 200 }) || errorRate.add(1);
                }
            } catch {}
        }
    });

    sleep(1);
}

// ─── Scenario 2: Authenticated user flow ───
export function authenticatedUserFlow() {
    group("Authenticated User Flow", () => {
        // Login
        const token = login(TEST_USER.email, TEST_USER.password);
        if (!token) {
            errorRate.add(1);
            return;
        }

        sleep(0.5);

        // View profile
        let res = http.get(`${BASE}/api/users/me`, authHeaders(token));
        cartLatency.add(res.timings.duration);
        check(res, { "profile 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.3);

        // Get cart
        res = http.get(`${BASE}/api/cart`, authHeaders(token));
        cartLatency.add(res.timings.duration);
        check(res, { "get cart 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.3);

        // Add item to cart — grab a random product
        const listRes = http.get(`${BASE}/api/products/page?page=0&size=10`);
        if (listRes.status === 200) {
            try {
                const products = JSON.parse(listRes.body).content;
                if (products && products.length > 0) {
                    const product = products[Math.floor(Math.random() * products.length)];
                    if (product.stock > 0) {
                        res = http.post(
                            `${BASE}/api/cart`,
                            JSON.stringify({ productId: product.id, quantity: 1 }),
                            authHeaders(token)
                        );
                        cartLatency.add(res.timings.duration);
                        check(res, { "add to cart 2xx": (r) => r.status >= 200 && r.status < 300 }) || errorRate.add(1);
                    }
                }
            } catch {}
        }

        sleep(0.5);

        // View orders
        res = http.get(`${BASE}/api/orders?page=0&size=10`, authHeaders(token));
        cartLatency.add(res.timings.duration);
        check(res, { "orders 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);

        // Clear cart to reset state
        http.del(`${BASE}/api/cart`, null, authHeaders(token));
    });

    sleep(1);
}

// ─── Scenario 3: Auth endpoint stress (rate limit test) ───
export function authEndpointStress() {
    const res = http.post(
        `${BASE}/api/auth/login`,
        JSON.stringify({
            email: "load-test@example.com",
            password: "wrong",
            turnstileToken: TURNSTILE_TOKEN,
        }),
        { headers: { "Content-Type": "application/json" } }
    );
    authLatency.add(res.timings.duration);

    // We expect a mix of 401 (bad credentials) and 429 (rate limited)
    check(res, {
        "auth response 401 or 429": (r) => r.status === 401 || r.status === 429,
    }) || errorRate.add(1);

    if (res.status === 429) {
        check(res, {
            "429 has Retry-After": (r) => r.headers["Retry-After"] !== undefined,
        });
    }
}

// ─── Scenario 4: Admin operations ───
export function adminFlow() {
    group("Admin Flow", () => {
        const token = login(ADMIN_USER.email, ADMIN_USER.password);
        if (!token) {
            // Admin might have 2FA — skip gracefully
            console.log("Admin login failed (2FA may be enabled). Skipping admin scenario.");
            sleep(5);
            return;
        }

        sleep(0.3);

        // List all orders
        let res = http.get(`${BASE}/api/admin/orders?page=0&size=20`, authHeaders(token));
        adminLatency.add(res.timings.duration);
        check(res, { "admin orders 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);

        // List users
        res = http.get(`${BASE}/api/admin/users?page=0&size=20`, authHeaders(token));
        adminLatency.add(res.timings.duration);
        check(res, { "admin users 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);

        // Search users
        res = http.get(`${BASE}/api/admin/users?page=0&size=20&search=test`, authHeaders(token));
        adminLatency.add(res.timings.duration);
        check(res, { "admin user search 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);

        // List reviews
        res = http.get(`${BASE}/api/admin/reviews?page=0&size=20`, authHeaders(token));
        adminLatency.add(res.timings.duration);
        check(res, { "admin reviews 200": (r) => r.status === 200 }) || errorRate.add(1);

        sleep(0.5);
    });

    sleep(1);
}
