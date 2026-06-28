# Tank Solar Heater — Backend API

Spring Boot 4 REST API for the Tank Solar Heater store. Provides catalog, cart, checkout,
payments (COD + VNPay), orders, JWT authentication, an admin console API, and a Gemini-backed
AI chat assistant.

> Part of the [Tank Solar Heater](../README.md) full-stack project. The Angular storefront lives
> in [`../tank-solar-heater-fe`](../tank-solar-heater-fe).

## Tech Stack

- **Java 17**, **Spring Boot 4.0.6**
- **Spring Data JPA** + **Microsoft SQL Server**
- **Spring Security** — OAuth2 Resource Server with HS256 JWTs (`@EnableMethodSecurity`)
- **Spring AI** — Google Gemini chat model with JDBC-backed chat memory
- **springdoc OpenAPI** — Swagger UI
- **Lombok**, **Bean Validation**
- Build: **Maven** via the `mvnw` wrapper

## Prerequisites

- JDK 17+
- Microsoft SQL Server at `localhost:1433` with a database named `TankSolarHeaterDB`
- A Google Gemini API key
- *(Optional)* VNPay sandbox credentials for online payments

## Configuration

Settings live in [`src/main/resources/application.properties`](src/main/resources/application.properties).
Secrets are read from environment variables:

```bash
export GEMINI_KEY=your-gemini-api-key        # required: AI chat assistant
export VNPAY_TMN_CODE=your-merchant-code     # required only for VNPay payments
export VNPAY_HASH_SECRET=your-hash-secret
```

Key properties:

| Property | Purpose | Default |
|----------|---------|---------|
| `spring.datasource.*` | SQL Server connection | `localhost:1433 / TankSolarHeaterDB`, user `sa` |
| `spring.jpa.hibernate.ddl-auto` | Schema sync — tables auto-created on startup | `update` |
| `app.page-size` | Products per page in listing endpoints | `4` |
| `app.upload.dir` | Where uploaded product images are written | `../tank-solar-heater-fe/src/assets/images` |
| `app.vnpay.*` | VNPay gateway URLs and credentials | sandbox URLs |

## Running

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

API: **http://localhost:8080** · Swagger UI: **http://localhost:8080/swagger-ui.html**

```bash
./mvnw clean package            # build runnable jar into target/
./mvnw test                     # run tests
```

## Security Model

- Stateless JWT (HS256) via the OAuth2 Resource Server. The `role` claim maps to a
  `ROLE_<role>` authority (e.g. `ROLE_ADMIN`).
- Method-level rules use `@PreAuthorize` (e.g. `hasRole('ADMIN')`, `!hasRole('ADMIN')`).
- Publicly permitted: `/api/auth/**`, Swagger, `GET /api/products|categories|brands/**`,
  `/uploads/**`, `/api/cart/**`, and the VNPay callbacks `GET /api/payments/vnpay/**`
  (trusted via HMAC signature, not JWT).
- CORS is open to `http://localhost:4200` (the Angular dev server).

## API Endpoints

Base path: `/api`.

| Area      | Endpoints |
|-----------|-----------|
| Auth      | `POST /auth/login`, `POST /auth/register`, refresh-token endpoints |
| Products  | `GET /products/**` (public); create/update/delete (admin) |
| Categories / Brands | `GET /categories/**`, `GET /brands/**` (public); CRUD (admin) |
| Cart      | `GET/POST/PUT/DELETE /cart/**` (public, cookie-based) |
| Checkout  | `POST /checkout` — places order + creates payment, returns `paymentUrl` for VNPay |
| Payments  | `GET /payments` (admin), `GET /payments/{id}`, `PUT /payments/{id}/status` (admin) |
| VNPay     | `GET /payments/vnpay/return` (browser redirect), `GET /payments/vnpay/ipn` (IPN) |
| Orders    | `GET/POST/PUT /orders/**` |
| Dashboard | `GET /dashboard/**` (admin) |
| Chat      | `POST /chat` — Gemini AI assistant |

### VNPay payment flow

`BANK_TRANSFER` is settled online through VNPay:

1. `POST /checkout` with `paymentMethod: BANK_TRANSFER` creates a `PENDING` payment and
   returns a signed `paymentUrl`.
2. The client redirects the customer to `paymentUrl` (VNPay gateway).
3. VNPay redirects the browser to `/api/payments/vnpay/return`; the backend verifies the
   HMAC-SHA512 signature and amount, marks the payment `PAID`/`FAILED`, then forwards to the
   storefront result page. The handler is idempotent against duplicate callbacks.
4. `/api/payments/vnpay/ipn` is the server-to-server confirmation (register it in the VNPay
   merchant portal); it returns VNPay's `{RspCode, Message}` acknowledgement.

## Package Layout

`com.example.tanksolarheaterbe`:

```
controllers/   REST endpoints
services/      business logic (PaymentService, VnPayService, ChatService, ...)
repositories/  Spring Data JPA repositories
entities/      JPA entities (Product, OrderHeader, Payment, Account, ...)
dto/           request/response models (records + @Data)
security/      JwtService, RefreshTokenService, SecurityConfig
config/        OpenApiConfig, WebMvcConfig, VnPayProperties
ai/            Gemini chat integration
seed/          initial data loading
```

## Notes

- The JWT signing secret in `SecurityConfig` and the DB password in `application.properties`
  are development defaults — change them before any non-local deployment.
- With `ddl-auto=update`, entities drive the schema; no manual migrations are required for local dev.
