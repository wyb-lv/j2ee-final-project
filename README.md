# Tank Solar Heater — Full-Stack E-Commerce

A web shop for solar water-heating tanks, built as a J2EE coursework project. It pairs a
Spring Boot REST API with an Angular storefront and admin console, complete with JWT auth,
an AI chat assistant, and online payments through VNPay.

```
final/
├── tank-solar-heater-be/   Spring Boot 4 REST API (Java 17, SQL Server)
└── tank-solar-heater-fe/   Angular 20 storefront + admin (SSR via Express)
```

## Features

- **Catalog** — products, categories, and brands with image uploads and paginated listings.
- **Cart** — cookie-backed cart that works for guests (no login required to browse/add).
- **Checkout & payments** — order placement with two methods:
  - `COD` — cash on delivery.
  - `BANK_TRANSFER` — online payment via the **VNPay** gateway (redirect + signed callback).
- **Orders** — customer order history and admin order management with status transitions.
- **Accounts & auth** — registration, login, and JWT access tokens with refresh tokens.
- **Admin console** — dashboard stats plus CRUD for products, categories, brands, and orders.
- **AI assistant** — a chat endpoint backed by Google Gemini (Spring AI) with persisted memory.

## Tech Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Spring Boot 4.0.6, Java 17, Spring Data JPA, Spring Security (OAuth2 Resource Server / JWT), Spring AI (Google Gemini), springdoc OpenAPI |
| Database  | Microsoft SQL Server |
| Frontend  | Angular 20, TypeScript, RxJS, Angular SSR (Express) |
| Build     | Maven (`mvnw`), Angular CLI |

## Prerequisites

- **Java 17+** and Maven (the bundled `mvnw` wrapper works without a local Maven install).
- **Node.js 20+** and npm.
- **Microsoft SQL Server** reachable at `localhost:1433` with a database named `TankSolarHeaterDB`.
- A **Google Gemini API key** for the chat assistant.
- *(Optional)* **VNPay sandbox credentials** from <https://sandbox.vnpayment.vn> for online payments.

## Getting Started

### 1. Backend (`tank-solar-heater-be`)

Configuration lives in [`src/main/resources/application.properties`](tank-solar-heater-be/src/main/resources/application.properties).
Update the datasource credentials to match your SQL Server, then provide secrets via environment variables:

```bash
# Required for the AI chat assistant
export GEMINI_KEY=your-gemini-api-key

# Required only for VNPay (BANK_TRANSFER) payments
export VNPAY_TMN_CODE=your-merchant-code
export VNPAY_HASH_SECRET=your-hash-secret
```

Hibernate is set to `ddl-auto=update`, so tables are created/updated automatically on first run.

```bash
cd tank-solar-heater-be
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**.
Interactive API docs (Swagger UI): **http://localhost:8080/swagger-ui.html**

### 2. Frontend (`tank-solar-heater-fe`)

```bash
cd tank-solar-heater-fe
npm install
npm start                     # ng serve
```

The storefront runs on **http://localhost:4200** (CORS is pre-configured for this origin).

## API Overview

All endpoints are served under `/api`. Authentication uses a Bearer JWT; roles are `ADMIN` and customer.

| Area      | Endpoints (base `/api`) |
|-----------|--------------------------|
| Auth      | `POST /auth/login`, `POST /auth/register`, refresh-token endpoints |
| Products  | `GET /products/**` (public), admin CRUD |
| Categories / Brands | `GET /categories/**`, `GET /brands/**` (public), admin CRUD |
| Cart      | `GET/POST/PUT/DELETE /cart/**` (public, cookie-based) |
| Checkout  | `POST /checkout` — places the order and creates a payment |
| Payments  | `GET /payments` (admin), `GET /payments/{id}`, `PUT /payments/{id}/status` (admin) |
| VNPay     | `GET /payments/vnpay/return` (browser redirect), `GET /payments/vnpay/ipn` (server callback) |
| Orders    | `GET/POST/PUT /orders/**` |
| Dashboard | `GET /dashboard/**` (admin) |
| Chat      | `POST /chat` — AI assistant |

### Payment flow (VNPay)

1. Customer checks out with `paymentMethod: BANK_TRANSFER` → the API creates a `PENDING`
   payment and returns a `paymentUrl`.
2. The storefront redirects the customer to `paymentUrl` (the VNPay gateway).
3. After payment, VNPay redirects the browser to `/api/payments/vnpay/return`, which verifies
   the HMAC-SHA512 signature, records the result, and forwards to the storefront result page.
4. The `/api/payments/vnpay/ipn` server-to-server callback is the authoritative confirmation
   (register its URL in the VNPay merchant portal).

## Project Layout

**Backend** (`com.example.tanksolarheaterbe`):

```
controllers/   REST endpoints          entities/      JPA entities
services/      business logic          repositories/  Spring Data JPA
dto/           request/response models security/      JWT, auth config
config/        app + VNPay config       ai/            Gemini chat integration
seed/          initial data
```

**Frontend** (`src/app`): `pages/` (home, shop, product-detail, cart/checkout, account, auth),
`admin/` (dashboard + CRUD), `services/` (HTTP clients), `models/`, and `shared/`.

## Building for Production

```bash
# Backend → runnable jar in target/
cd tank-solar-heater-be && ./mvnw clean package

# Frontend → static + SSR bundle in dist/
cd tank-solar-heater-fe && npm run build
```

## Notes

- The JWT signing secret in `SecurityConfig` and the DB password in `application.properties`
  are development defaults — replace them before any non-local deployment.
- Uploaded product images are written to `tank-solar-heater-fe/src/assets/images` and served at
  `/uploads/**` (configurable via `app.upload.dir`).
