# ParkFlow360 — Manual de Contexto del Proyecto (MCP)

> Documento vivo de contexto. Fuente de verdad: el código y [`AGENTS.md`](../../AGENTS.md).  
> Cobertura funcional: [`ESPECIFICACIONES_PARKFLOW360.md`](../01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md).

## Estado del proyecto

- **Nombre:** ParkFlow360
- **Tipo:** SaaS multiempresa de gestión de parqueaderos
- **Repositorio:** JulianSalamanca-code/ParkFlow360
- **Rama de trabajo actual:** `dev`
- **Monorepo:** sí (`backend/` + `frontend/`)
- **Estado:** en desarrollo; F0–F11 están implementados. Pendientes: CI, despliegue y cierre de deuda técnica (ver `AGENTS.md`).

## Tecnologías

### Backend

- Java 17, Spring Boot 3.2.5, Maven (sin wrapper)
- Spring Security stateless + JWT (JJWT 0.12.5), Google OAuth2 con flujo manual
- Spring Data JPA, Flyway (migraciones existentes V1–V20)
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
4. **Ingresos**: reservas, llegada del guardia, salidas autorizadas y liberación de espacios.
5. **Pagos y facturas**: recibo al entrar, cobro del faltante en pagaduría, pagos y créditos.
6. **Saldos**: recarga y movimientos con saldo anterior y posterior.
7. **Empresas y parqueaderos**: identificador único, cupos por tipo de vehículo y tolerancia de salida por empresa.
8. **Reportes**: general, espacios por estado e ingresos por período, filtrados por parqueadero.
9. **Seguridad y usuarios**: JWT, Google OAuth y roles vigentes `PLATAFORMA` (alias `ADMIN`), `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA` y `USUARIO`.

## Cobertura funcional

F0–F11 están implementados (tabla maestra en la [especificación](../01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md)). Quedan pendientes CI, despliegue y cierre de deuda técnica; ver `AGENTS.md`.

## Flujo actual

1. `POST /api/auth/login` o inicio con Google (`GET /api/auth/google/authorize` → callback frontend `/auth/google/callback`).
2. Navegación según rol (`RoleGuard`): plataforma → `/planos`, empresa → `/empresa`, cajero → `/cajero/pagos`, guardia → `/guardia/ingresos`, usuario → `/mis-parqueos`.
3. Reserva (`POST /api/ingresos/reservar`), llegada registrada por el guardia (`PUT /api/ingresos/{id}/llegada`, abre el recibo), cobro en pagaduría (`POST /api/facturas/{id}/pagar`) y salida autorizada por el guardia (`PUT /api/ingresos/{id}/salida`) dentro de la tolerancia.

## Endpoints principales

- `/api/auth`: `login`, `registro`, `google/authorize`, `google/callback`
- `/api/vehiculos`, `/api/espacios`, `/api/tarifas`, `/api/ingresos`, `/api/pagos`, `/api/facturas`, `/api/saldos`, `/api/creditos`, `/api/empresas`, `/api/parqueaderos`, `/api/reportes`, `/api/usuarios`
- Rutas por rol: `/api/vehiculos/mios`, `/api/ingresos/mios`, `/api/ingresos/reservar`, `/api/ingresos/activos`, `/api/ingresos/{id}/estado-pago`, `GET /api/espacios/**`, `GET /api/tarifas/**`

## Requisitos locales

Java 17+, Maven 3.9+, Node 22, pnpm, Docker opcional.

## Notas

Proyecto privado y confidencial. Los documentos históricos están clasificados en [`INDICE_DOCUMENTACION.md`](../INDICE_DOCUMENTACION.md).
