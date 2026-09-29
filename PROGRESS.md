# Progress

Spec: [docs/PRD.md](docs/PRD.md)

## Resume notes

- Monorepo: `backend/` (Spring Boot), `frontend/` (React and Vite), `docs/`, `scripts/`, root `docker-compose.yml` and `.env`
- Copy `.env.example` to `.env` (gitignored) and set `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `IDENTITY_MOCK_WEBHOOK_SECRET`
- Dev: `docker compose up -d postgres`, then in `backend/` run `mvn package -DskipTests` and `java -jar target/trustlayer.jar` with `.env` loaded, then in `frontend/` run `npm install` and `npm run dev` (port 3000, proxies `/api` to 8080)
- Or `docker compose up --build` for postgres, backend and frontend
- Deviation from the PRD: the two `webhook_events` tables are named `verification_webhook_events` and `payment_webhook_events` because both modules share one database
- Added event `PASSWORD_RESET_REQUESTED` so the notification module can send the reset link
- Notifications also cover `SUBSCRIPTION_CANCELLED` and `ACCESS_BLOCKED` because the PRD event table lists notification as a consumer

## Phases

| Phase | Status |
|---|---|
| 1 Skeleton, config, errors, Flyway, Compose | done and verified |
| 2 User module and authentication | done and verified |
| 3 Verification module (mock, Stripe Identity code) | mock verified, Stripe Identity not verified |
| 4 Payment module and Stripe webhooks | verified against a local Stripe stub and locally signed webhooks, real Stripe not verified |
| 5 Events and access module | done and verified |
| 6 Notification module (mock, SMTP and Meta code) | mock verified, real SMTP and Meta not verified |
| 7 Admin endpoints | done and verified |
| 8 README, Mermaid diagrams, demo script | written, README steps not followed by a fresh person |
| 9 Frontend and integration | done and verified in Chromium against the running backend |
| 10 UI redesign | done and verified, same browser checks plus dark mode and phone width |

## Frontend (phase 9)

React 18, TypeScript, React Router, Vite, plain CSS with automatic dark mode. No state library and no UI kit. Same origin through a proxy (Vite in dev, nginx in Docker), so the backend has no CORS config. Details are in `docs/INTEGRATION.md`.

Screens: register, sign in, verify email, forgot and reset password, dashboard with four steps and live access state, payment return pages, profile, admin lists with filters and paging, admin user overview with the mock identity decision.

Verified with a Playwright script in Chromium (run locally, not committed yet): 28 of 28 checks passed. It covered registration, email link, guards, mock identity label and admin decision in a second session, checkout redirect, waiting page flipping only after a signed webhook, GRANTED then BLOCKED after cancel, admin tables, profile, password reset, sign out and non admin blocked. Screenshots are in `docs/screenshots`.

Also verified: `docker compose up --build` serves the built frontend through nginx, deep links fall back to `index.html`, and `/api` proxies to the backend.

Fixed during testing: sign out now clears local tokens before the revoke call so navigating away cannot leave a session behind.

## How verification was done

A Python script sent HTTP requests to the running app (PostgreSQL 16 in Docker). Stripe Checkout creation was pointed at a local stub server through `STRIPE_API_BASE`. Stripe webhooks were signed with the webhook secret in Stripe's `t=...,v1=...` format. The scripts were run locally and are not committed yet.

Result: 99 of 99 checks passed on the main run, 5 of 5 on the failing provider run, 2 of 2 on the rate limit run.

## Acceptance criteria (PRD section 14)

| # | Criterion | Result |
|---|---|---|
| 1 | Registration to granted access using only the API | Passed. Identity through the mock decision endpoint, payment through a signed webhook |
| 2 | Same Stripe webhook three times changes state once | Passed |
| 3 | Same checkout twice with one Idempotency-Key creates one Stripe session | Passed against the stub (stub counted one created session) |
| 4 | No verified identity means no checkout | Passed (403 IDENTITY_NOT_VERIFIED) |
| 5 | Cancelling a subscription blocks access | Passed through the subscription.deleted webhook |
| 6 | Failing email or WhatsApp never breaks payment flow | Passed. Deliveries retried 3 times then FAILED, payment and access unaffected |
| 7 | App starts with mocks and no private credentials | Passed. Started with only DB password and JWT secret set |
| 8 | No secret in the repository | Passed by pattern scan, `.env` is gitignored. The folder is not a git repository yet |

## UI redesign (phase 10)

Design tokens with a full dark palette, Inter and JetBrains Mono, Lucide icons. Split auth layout, four step timeline dashboard with progress bar and access ring, plan cards, fact tiles, admin console with summary counts, segmented tabs, debounced search, avatars and paged tables, polished result pages.

Verified: Playwright 29 of 29 in Chromium through the Vite dev server (the earlier 28 checks plus no horizontal overflow at 390px). A smoke run against the nginx build in Docker passed (sign in, dashboard, admin tabs, deep route reload, Inter loaded, no page errors). Screenshots in `docs/screenshots` were regenerated in light, dark and mobile.

Fixed during this phase: the admin search fired a request per keystroke and a slow earlier response could overwrite the latest results. Search is now debounced and all admin tabs ignore stale responses.

## Also verified

- Email verification, password reset, refresh rotation, reuse detection revoking the family, logout revocation, single use tokens stored hashed
- Lockout after 5 failed logins, rate limit 429 with Retry-After
- Session expiry blocks completion, attempts capped at 3, duplicate identity webhook skipped, bad signatures rejected
- Admin endpoints forbidden for USER and anonymous, pagination and filters work, overview shows full state
- Standard error body everywhere including security and rate limit responses
- No tokens, JWTs or passwords found in logs with default settings
- `docker compose up --build` starts PostgreSQL and the app and the admin login works

## Not verified

- Real Stripe Checkout, real Stripe webhooks and the Stripe CLI forwarding flow (no Stripe test key was available)
- Stripe Identity (`IDENTITY_PROVIDER=stripe`) including its webhook and result mapping
- Real SMTP delivery and the Meta Cloud API call
- Shape of real `invoice.payment_failed` events on newer Stripe API versions (both known field locations are read, neither tested against a real event)
- Concurrent duplicate webhook delivery and concurrent refresh with the same token (code relies on the unique index and optimistic locking, not load tested)
- Swagger UI rendering beyond the HTTP 200
- The Playwright script was not run against the nginx build in Docker (only the Vite dev server), and Docker's backend has no Stripe stub
- Real Stripe hosted checkout page in the browser (the test intercepted the redirect)
- Mobile layout beyond a CSS media query, and other browsers than Chromium
- Refresh token rotation triggered from the UI after the access token expires (verified at API level only)
- Following README and demo steps from a clean machine

## Next

Add real credentials and run the Stripe and Stripe Identity flows through the UI, then commit automated tests (Testcontainers for the backend, Playwright for the frontend).
