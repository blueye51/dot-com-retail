# i-love-shopping

A full-stack e-commerce application built with Spring Boot and React.

## Table of Contents

- [Setup](#setup)
- [Tech Stack](#tech-stack)
- [ACID Compliance](#acid-compliance)
- [Security](#security)
- [Scalability](#scalability)
- [Product Search](#product-search)
- [Ratings & Reviews](#ratings--reviews)
- [User Settings](#user-settings)
- [Automated Tests](#automated-tests)
- [Entity Relationship Diagram (ERD)](#entity-relationship-diagram-erd)

## Setup

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)
- [Git](https://git-scm.com/)

### Clone

```bash
git clone https://gitea.kood.tech/ericrand/i-love-shopping1.git
cd i-love-shopping1
```

### Environment

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

Most values in `.env.example` have working defaults. The ones you need to set up:

#### Resend (email delivery)

1. Go to [resend.com](https://resend.com) and create an account.
2. In the dashboard, go to **API Keys** and click **Create API Key**.
3. Give it any name, click **Create**, and copy the key.
4. Paste it into `.env` as `RESEND_API_KEY`.

> **Note:** On the free tier, Resend can only send emails to the account owner's email address. Use that same email for `ADMIN_SEED_EMAIL` and when registering in the app.
>
> **Note:** The admin account has 2FA forced on. If you want to test the app as a regular user without 2FA, register with a different email than the one set in `ADMIN_SEED_EMAIL`.

#### Google OAuth2

1. Go to [console.cloud.google.com/auth](https://console.cloud.google.com/auth).
2. Create a new project (or select an existing one).
3. Create an **OAuth client ID** (application type: Web application).
4. Under **Authorised JavaScript origins**, add: `https://localhost:5173`
5. Under **Authorised redirect URIs**, add: `https://localhost:8443/login/oauth2/code/google`
6. Copy the **Client ID** and **Client Secret** into `.env` as `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`.

> If you don't want to set up Google OAuth2 yourself, you can ask me to add your Google email as a test user on my OAuth app.

### Quick Start

```bash
docker compose --profile full up --build
```

This starts all services: PostgreSQL, Redis, MinIO, the backend API, and the frontend. Once everything is up:

- **Frontend:** https://localhost:5173
- **Backend API:** https://localhost:8443
- **MinIO Console:** http://localhost:9001

### Quick Stop

```bash
docker compose --profile full down
```

### Full Cleanup

Remove all containers, volumes, and images:

```bash
docker compose --profile full down -v --rmi all
```

---

## Tech Stack

### Backend -Spring Boot (Java)

The backend is built with **Spring Boot 3** and **Java 21**. Spring Boot was chosen for its mature ecosystem, strong security defaults, and how well it integrates with PostgreSQL through JPA/Hibernate. The framework handles dependency injection, transaction management, and data validation out of the box, which keeps the codebase clean and focused on business logic.

### Frontend -React (Vite + JSX)

The frontend is a **React** application scaffolded with **Vite**. Vite provides fast hot module replacement during development and optimized production builds. The UI is written in JSX with CSS modules for scoped styling.

### PostgreSQL

**PostgreSQL** is the primary relational database. It stores all persistent application data -users, products, categories, brands, orders, and file metadata. PostgreSQL was chosen for its reliability, strong SQL compliance, and excellent support for complex queries and indexing.

### Redis

**Redis** serves as a lightweight in-memory store for temporary, short-lived data:

- **Refresh tokens** -Stored with a 30-day TTL. On each token rotation the old token is deleted and a new one is saved, keeping only valid sessions in memory.
- **OTP codes** -2FA and email verification codes live in Redis with a 5-minute TTL. Once verified, the code is immediately deleted.
- **2FA pending sessions** -When a user with 2FA enabled logs in, a temporary session code is stored for 5 minutes while they complete verification.
- **OAuth2 login codes** -Short-lived (60s) authorization codes for the OAuth2 flow.

Redis is ideal here because all of this data is ephemeral -it doesn't need to survive a restart, it expires naturally, and Redis handles TTL-based expiration natively without any cleanup jobs. It's also extremely fast for the simple key-value lookups these flows require.

### MinIO (S3-Compatible Object Storage)

**MinIO** handles all image storage (product images, etc.). It implements the full **Amazon S3 API**, which means the application uses the standard AWS S3 SDK to interact with it. This makes migration to Amazon S3 in production a single configuration change -just swap the endpoint URL and credentials, no code changes needed.

Images are organized into public and private prefixes within a single bucket. Public images are served directly via URL, while private files use time-limited presigned URLs for secure access.

---

## ACID Compliance

The application fully complies with **ACID** (Atomicity, Consistency, Isolation, Durability) properties:

- **Atomicity** -Spring's `@Transactional` annotation wraps service methods in database transactions. If any step fails, the entire operation rolls back. For example, creating an order with its items either fully commits or fully rolls back.
- **Consistency** -JPA entity constraints (`@Column(nullable = false)`, `@Check`, unique constraints) enforce data integrity at the database level. Foreign keys ensure referential integrity between entities like products, categories, and orders.
- **Isolation** -PostgreSQL's default `READ COMMITTED` isolation level prevents dirty reads. Each transaction only sees committed data from other transactions.
- **Durability** -PostgreSQL writes all committed transactions to disk via its write-ahead log (WAL). Once a transaction is committed, it survives crashes and restarts. Docker volumes ensure database files persist across container restarts.

Spring Boot and PostgreSQL handle most of this by default -the combination provides ACID guarantees without requiring manual transaction management.

---

## Security

### JWT Access Tokens

Authentication uses **JSON Web Tokens (JWTs)** as access tokens. Each token contains the user's ID, roles, email verification status, and 2FA status as claims, signed with an **HMAC-SHA** secret key. This means:

- The server can verify any request by checking the signature -no database lookup needed per request.
- Tokens cannot be forged or tampered with because every claim is part of the signature calculation. Changing any piece of data in the token invalidates the signature.
- Access tokens expire after **15 minutes**, limiting the damage window if a token is ever compromised.

Refresh tokens are opaque values stored in Redis (not JWTs), delivered via **HttpOnly, Secure** cookies so they're inaccessible to JavaScript.

### Two-Factor Authentication (2FA)

Users can enable 2FA in their account settings. When enabled, the login flow changes:

1. User submits email and password -the server validates credentials but does **not** issue tokens.
2. Instead, a **6-digit OTP code** is generated and sent to the user's email via **Resend**.
3. A temporary session code is stored in Redis with a 5-minute TTL.
4. The user submits the OTP along with the session code -if both match, tokens are issued.

The OTP has a 30-second cooldown between sends to prevent abuse, and the code is deleted from Redis immediately after successful verification so it can't be reused.

### Email Verification

Email verification uses a link-based flow:

1. An authenticated user requests a verification link.
2. A unique token is generated, stored in Redis with a 15-minute TTL, and emailed as a clickable link via **Resend**.
3. The user clicks the link -the backend validates the token, marks the account as verified, and deletes the token from Redis.

The verified status is embedded in subsequent JWT tokens as the `emailVerified` claim, so the frontend can gate features without extra API calls.

### Password Reset

Password recovery uses the same link-based approach as email verification:

1. The user enters their email on the "Forgot Password" page.
2. The backend generates a unique token, stores it in Redis with a 15-minute TTL, and emails a reset link. The endpoint always returns 204 regardless of whether the email exists, preventing email enumeration.
3. The user clicks the link and enters a new password.
4. The backend validates the token, updates the password, and deletes the token from Redis.

Password reset is only available for local accounts -Google OAuth2 users manage their passwords through Google.

### Spring Security Configuration

The security layer is configured as a stateless API:

- **Session management** is set to `STATELESS` -no server-side sessions, every request is authenticated via the JWT in the `Authorization` header.
- **CSRF protection** is disabled, which is appropriate for stateless APIs that don't use cookie-based authentication for state-changing requests.
- **CORS** is configured to only allow the frontend origin, with credentials enabled for the refresh token cookie.
- **Anonymous access** is disabled -unauthenticated users only reach explicitly public endpoints.
- **OAuth2 (Google)** is supported with custom success/failure handlers that issue the same JWT token pair as standard login.
- **Cloudflare Turnstile** CAPTCHA is validated on registration and login to prevent automated attacks.
- Endpoints are secured by role: admin routes require `ROLE_ADMIN`, public routes (products, categories, brands) are open, and everything else requires authentication.

### Protection Against Common Attacks

- **SQL Injection** -All database queries go through JPA/Hibernate with parameterized queries. No raw SQL string concatenation.
- **XSS** -The API is a pure JSON REST API. No server-side HTML rendering. The React frontend handles output encoding.
- **Token Theft** -Access tokens are short-lived (15 min). Refresh tokens are in HttpOnly cookies inaccessible to JavaScript. Token rotation on every refresh invalidates old tokens.
- **Brute Force** -OTP cooldowns, token expiration, and Turnstile CAPTCHA limit automated attack surface.
- **CORS** -Strict origin allowlist prevents cross-origin requests from unauthorized domains.

---

## Scalability

The application is designed to be straightforward to extend:

- **Modular package structure** -Each domain (products, orders, users, auth, files, categories, brands) lives in its own package with its own controller, service, repository, DTOs, and entities. Adding a new domain means adding a new package without touching existing code.
- **S3-compatible storage** -MinIO can be swapped to Amazon S3 by changing configuration. No code changes needed.
- **Redis for ephemeral state** -Keeping temporary data out of PostgreSQL means the relational database stays focused on persistent data and doesn't accumulate cleanup debt.
- **Stateless authentication** -JWT-based auth means the backend can be horizontally scaled behind a load balancer without sticky sessions.
- **Docker Compose** -The entire stack is containerized with health checks and dependency ordering, making it reproducible across environments.

---

## Product Search

The search system is built on **JPA Specifications**, which compose filters dynamically at query time:

- **Text search** -Case-insensitive partial matching on product name.
- **Category filter** -Filter by exact category ID.
- **Brand filter** -Filter by exact brand ID.
- **Price range** -Min and max price boundaries.
- **Sorting** -By name, price, creation date, or relevance, ascending or descending.
- **Relevance sorting** -A simple popularity-based relevance score. Products are ranked by a combination of rating count and average rating, surfacing popular and well-reviewed items first.
- **Pagination** -Page-based with configurable page size (max 100).

Each filter is an independent `Specification` that gets combined with `.and()`. This makes it trivial to add new filters -write a new specification method and add it to the composition chain. The query also uses `LEFT JOIN FETCH` on brand and category to avoid N+1 query problems.

---

## Ratings & Reviews

Users can rate and review products directly from the product page. Each rating includes a score (1–5) and an optional text comment. The system tracks:

- **Average rating** -Computed per product and returned in both product listings and detail views.
- **Total rating count** -Displayed alongside the average to give context on how many reviews a product has received.
- **Star display** -A visual star component renders the average rating on product cards and the product detail page.

Ratings are tied to authenticated users and stored in the database with a foreign key to the product. The average and count are maintained on the product entity for efficient querying without needing to aggregate on every request.

---

## User Settings

User preferences are stored server-side and fetched after every successful authentication (login, OAuth2, 2FA). Settings are kept in a Redux slice on the frontend, making them globally accessible without extra API calls.

### Imperial Units

Users can toggle between **metric** (cm, kg) and **imperial** (inches, lb) units in their profile settings. The backend always stores values in metric — conversion is purely a frontend concern:

- **Product display** -The product detail page converts dimensions and weight to the user's preferred unit system.
- **Product creation** -Input labels dynamically show the active unit. Values entered in imperial are converted to metric before being sent to the backend.
- **Conversion utilities** -A shared `units.js` module provides bidirectional converters (`cmToIn`, `inToCm`, `kgToLb`, `lbToKg`) and display formatters (`formatDimension`, `formatWeight`).

---

## Automated Tests

Tests live in `backend/store/src/test/` and use **Testcontainers** to spin up real PostgreSQL and Redis instances per test run — no mocks for infrastructure, so tests hit the same database and cache the application uses in production.

- **`ProductServiceTest`** -Unit tests for the product search service using Mockito. Tests that filters, pagination, sorting, and thumbnail mapping work correctly at the service layer without needing a database.
- **`ProductControllerIT`** -Integration tests that boot the full Spring context and hit the `/api/products/page` endpoint via MockMvc. Tests search by name, price range filtering, category/brand filtering, sort ordering, and pagination against a real PostgreSQL database.
- **`SecurityIT`** -Integration tests for the security layer. Verifies that public endpoints (products, brands) are accessible without a token, protected endpoints return 401 without a token and 200 with a valid one, invalid tokens are rejected, and admin endpoints enforce role-based access (403 for regular users, 204 for admins).

The test profile (`application-test.yml`) uses `ddl-auto: create-drop` so each run starts with a clean schema, disables SSL, and stubs out external services (OAuth2, Resend, Turnstile) with dummy values so tests run without any API keys.

---

## Entity Relationship Diagram (ERD)

![ERD](./Store.drawio.svg)