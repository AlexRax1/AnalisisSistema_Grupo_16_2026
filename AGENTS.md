# AGENTS.md

MuniQuejas is a municipal complaint system: `backend/` is Spring Boot 4.1.1 on Java 21 (Maven wrapper), `frontend/` is Angular 21 (standalone, zoneless) with Vitest.

## Read first

- `documentacion/flujo_del_programa.md` — canonical roles, complaint lifecycle, and business rules. The `.docx` files in `documentacion/` are the original use cases.
- `PLAN_REPORTERIA.md` — living status/checklist for the reporting module. Update it when working on reporting; its "frozen template" rule means new reports adapt the existing dataset/screen, not duplicate them.
- Root `README.md` is Angular CLI boilerplate. Ignore it for project setup.

## Run locally

- Backend configuration comes from environment variables (`SERVER_PORT`, `DB_*`, `JWT_*`, `MAIL_*`). There is no dotenv loader, so a `.env` file is not read automatically:
  ```bash
  cd backend
  set -a; source .env; set +a
  ./mvnw spring-boot:run
  ```
  Starts on `http://localhost:8080`.
- Frontend:
  ```bash
  cd frontend
  npm install
  npm start
  ```
  Dev server runs on `http://localhost:4200`.
- PostgreSQL must be running before the backend starts. For a fresh database, create it and run `psql -U postgres -d <db> -f sql/db_MuniQuejas.sql`. The script seeds roles, categories, subcategories, and dependencies but **no users**; create the first administrator manually.
- `sql/bitacora_inmutabilidad.sql` is a one-time migration for existing databases, not part of a fresh install.
- `spring.jpa.hibernate.ddl-auto` is `update` on `main`. When changing entities, keep `sql/db_MuniQuejas.sql` in sync because it is the fresh-install schema.
- `sql/db_MuniQuejas.sql` declares `recuperacion_password` twice near the end. A fresh import reports one error for the duplicate, but the earlier tables and seed data are still created.

## Verify changes

- Focused backend test:
  ```bash
  cd backend && ./mvnw -Dtest=QuejaServiceRegistroTest test
  ```
- Full backend suite: `./mvnw test`. Most tests are Mockito unit tests, but `BackendApplicationTests` is `@SpringBootTest` and needs the same environment plus a reachable PostgreSQL, so the full suite fails without DB setup.
- The frontend production build currently fails the per-component style budgets in `angular.json`: `detalle-queja-inspector.css`, `detalle-queja-gestion.css`, and `detalle-queja-especialista.css` exceed the 12 kB error limit. Use the development build as a typecheck instead:
  ```bash
  cd frontend && npm run build -- --configuration development
  ```
  Do not assume that production-build failure was introduced by your change.
- `npm test` currently fails with "No tests found": Vitest is configured but the repo has no `*.spec.ts` files. Add a spec before using it as a smoke check.
- There is no lint task or ESLint config. Prettier is configured (100 columns, single quotes); run `npx prettier --write <files>` from `frontend/`.

## Architecture

- Backend is package-by-feature under `com.muni.backend`: `security` (JWT/BCrypt/password reset), `usuarios`, `quejas` (core workflow), `bitacora` (audit), `reportes`, and `shared` (file storage, email). `QuejaEstadoService` centralizes complaint state transitions and role-specific available actions.
- Frontend: `core/` holds models, HTTP services, and the auth interceptor; role modules live under `modules/` (`auth`, `portal-ciudadano`, `gestion-municipal`, `operaciones-campo`, `admin`, `portal`), with shared layouts under `shared/layouts/`.
- API prefixes: `/auth`, `/quejas`, `/api/funcionario`, `/api/inspector`, `/api/especialista`, `/admin/quejas`, `/admin/usuarios-internos`, `/admin/reportes`, `/admin/bitacora`. `/usuarios` is also exposed as `/api/usuarios`.
- Database/JWT roles are `CIUDADANO`, `FUNCIONARIO_MUNICIPAL`, `INSPECTOR_CAMPO`, `ESPECIALISTA_TECNICO`, `ADMINISTRADOR`.
- Complaint states are persisted as display strings with spaces and accents: `REGISTRADA`, `EN INSPECCIÓN`, `EN REPARACIÓN TÉCNICA`, `EN VALIDACIÓN DE REPARACIÓN`, `PENDIENTE DE CIERRE`, `SOLUCIONADA / CERRADA`, `RECHAZADA`. The underscore forms in the docs are not the DB values; when adding or renaming a state, update backend and frontend string comparisons together.
- `bitacora` is append-only at the database level (triggers block UPDATE/DELETE/TRUNCATE). Record audit events through `BitacoraService`; never add update/delete logic for this table.
- Uploads: `FileStorageService` writes to `subidas/evidencias` relative to the process working directory and serves it at `/subidas/evidencias/**`. Run the backend from `backend/` to use `backend/subidas`; only JPG/PNG/WEBP up to 5 MB with magic-byte validation are accepted. The directory is gitignored.

## Known gotchas

- The frontend hardcodes `http://localhost:8080` in every `core/services/*.ts`, `auth.service.ts`, and several evidence-URL helpers. There is no `environment.ts`; search for `localhost:8080` before changing the API host.
- `proxy.conf.json` only proxies `/quejas` and `/api`, and absolute URLs bypass it. If you convert services to relative URLs, add `/auth`, `/admin`, and `/usuarios` routes too.
- Backend login returns full role names (`FUNCIONARIO_MUNICIPAL`, `INSPECTOR_CAMPO`, `ESPECIALISTA_TECNICO`), but `login.ts` redirects on short names (`FUNCIONARIO`, `INSPECTOR`, `ESPECIALISTA`). Keep that mapping in sync when touching auth.
- `JwtService` hard-codes 30-minute token expiry; `jwt.expiration` / `JWT_EXPIRATION_MS` is not used there.
- `@PreAuthorize` appears on several controllers, but `@EnableMethodSecurity` is not configured, so those annotations are inert. Actual authorization is the path matcher list in `SecurityConfig`; note `/admin/quejas/**` is currently `permitAll`, and `/admin/**` includes the unseeded `EMPLEADO` role.
- The app is zoneless (`provideZonelessChangeDetection()`). Some pages use signals; older pages update state and call `ChangeDetectorRef.detectChanges()` manually. Follow the surrounding page's pattern.
- Several field/management screens silently fall back to hardcoded mock data on HTTP errors (`bandeja-gestion`, `bandeja-inspector`, `bandeja-especialista`, and their detail pages). A populated screen is not proof the API works; check console warnings and network responses.
- `modules/admin/pages/admin-usuarios/` is imported by `app.routes.ts` but not routed; `/admin/usuarios` redirects to `gestion-personal`, which is the active personnel screen.
- Deployment assets live on the `produccion` branch, not `main`: `backend/Dockerfile`, `application-prod.yaml` (with `ddl-auto: validate`), frontend/nginx Dockerfiles, and `docker-compose.yml`. `.github/workflows/deploy-backend.yml` triggers only on pushes to `produccion` touching `backend/**`. Check that branch before changing deployment.

## Conventions

- Keep code identifiers, API messages, and domain values in Spanish, matching existing files.
- Angular 21 uses strict TS/templates, standalone components, and no NgModules.
- Backend uses Lombok. Most feature code uses constructor injection via `@RequiredArgsConstructor`; some older controllers still use field injection.
