# ParkFlow360 — Manual de Contexto del Proyecto (MCP)

> Documento vivo de contexto. Fuente de verdad: el código y [`AGENTS.md`](../AGENTS.md).  
> Funcionalidades previstas: [`ESPECIFICACIONES_PARKFLOW360.md`](ESPECIFICACIONES_PARKFLOW360.md).

## Estado del proyecto

- **Nombre:** ParkFlow360
- **Tipo:** SaaS multiempresa de gestión de parqueaderos
- **Repositorio:** JulianSalamanca-code/ParkFlow360
- **Rama de trabajo actual:** `docs/especificaciones-y-agentes`
- **Monorepo:** sí (`backend/` + `frontend/`)
- **Estado:** en desarrollo; las funcionalidades F0–F10 de la spec están pendientes

## Tecnologías

### Backend

- Java 17, Spring Boot 3.2.5, Maven (sin wrapper)
- Spring Security stateless + JWT (JJWT 0.12.5), Google OAuth2 con flujo manual
- Spring Data JPA, Flyway (migraciones existentes V1–V16)
- PostgreSQL 15

### Frontend

- Angular 22.2.x (standalone), TypeScript ~6.0.3, RxJS 7.8, Cypress 16.2
- Node 22, pnpm (Netlify usa `pnpm-lock.yaml`)

### Infraestructura

- Backend: Render · Frontend: Netlify · Base de datos: Supabase PostgreSQL
- Docker Compose solo levanta PostgreSQL local en `:5432`

## Arquitectura

`frontend` (`:4200`, proxy `/api` → `:8080`) → API REST Spring Boot (`:8080`) → PostgreSQL.

Backend: `com.parkflow360` con `config/`, `security/` y `modulos/<dominio>/{controller,dto,entity,repository,service}`; `shared/exception` para el manejo global de errores.

Frontend: `src/app/{auth,core,features,shared}`, rutas lazy con `RoleGuard`, servicios HTTP con URLs relativas `/api/...`.

## Módulos existentes

1. **Vehículos**: CRUD con validación de placa `^[A-Z]{3}-[A-Z0-9]{3}$`.
2. **Espacios**: CRUD, estados `LIBRE`/`RESERVADO`/`OCUPADO`, planos e importación CSV.
3. **Tarifas**: CRUD con tipos `HORA`, `DIA`, `SEMANA`, `MES`, `MOMENTO`.
4. **Ingresos**: reservas, ingresos, salidas y liberación de espacios.
5. **Pagos**: registro manual (métodos EFECTIVO/TARJETA/TRANSFERENCIA/QR; estados `CONFIRMADO`/`FALLIDO`).
6. **Reportes**: general, espacios por estado e ingresos por período.
7. **Seguridad y usuarios**: JWT, Google OAuth y roles actuales `ADMIN`/`USUARIO`.

## Módulos previstos (no implementados)

`saldos`, `creditos`, `facturas`, `empresas`, `parqueaderos`, además de los roles `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS` y `GUARDIA`. Ver la spec.

## Flujo actual del usuario

1. `POST /api/auth/login` o inicio con Google (`GET /api/auth/google/authorize` → callback frontend `/auth/google/callback`).
2. Navegación según rol (`RoleGuard`): administrador → `/vehiculos`; usuario → `/mis-parqueos`.
3. Reserva (`POST /api/ingresos/reservar`), registro de pagos y salida (`PUT /api/ingresos/{id}/salida`).

## Endpoints principales

- `/api/auth`: `login`, `registro`, `google/authorize`, `google/callback`
- `/api/vehiculos`, `/api/espacios`, `/api/tarifas`, `/api/ingresos`, `/api/pagos`, `/api/reportes`, `/api/usuarios`
- Rutas compartidas ADMIN/USUARIO: `/api/vehiculos/mios`, `/api/ingresos/mios`, `/api/ingresos/reservar`, `GET /api/espacios/**`, `GET /api/tarifas/**`

## Requisitos locales

Java 17+, Maven 3.9+, Node 22, pnpm, Docker opcional.

## Notas

Proyecto privado y confidencial. Los documentos históricos están clasificados en [`INDICE_DOCUMENTACION.md`](INDICE_DOCUMENTACION.md).
