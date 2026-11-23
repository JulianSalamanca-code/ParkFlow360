# Plan de implementación — ParkFlow360

**Fecha:** 2026-11-22  
**Estado:** hitos 1–4 ejecutados e integrados en `dev`; pendiente el cierre (CI y despliegue).

Este documento convierte la tabla F0–F11 de la [especificación](ESPECIFICACIONES_PARKFLOW360.md) en un plan para dos personas. Las historias están en el [backlog nuevo](BACKLOG_NUEVO.md) y las convenciones en [`AGENTS.md`](../../AGENTS.md).

---

## 1. Punto de partida

- Trabajar sobre `dev`: ya contiene toda la documentación reorganizada.
- No usar las ramas `feat/f*` actuales: están atrasadas en `a58e344` y no tienen código. Crearlas de nuevo desde `dev` cuando toque cada hito.
- Integración siempre en `dev`, nunca directo a `main`.

## 2. División recomendada: por capas

La forma más limpia de no chocarse es que cada persona sea dueña de una capa.

| Persona | Dueño de | No toca |
|---|---|---|
| **A — Backend** | `backend/**`, migraciones, `SecurityConfig`, `IngresoService`, `PagoService` | `frontend/**`, documentación |
| **B — Frontend** | `frontend/**`, rutas, menú, componentes, Cypress | `backend/**`, documentación |
| **Documentación** | Una sola persona, al final de cada hito | Código |

Si se prefiere dividir por funcionalidad en vez de por capas, la regla que no debe cambiar es que **ningún archivo tenga dos dueños al mismo tiempo**.

## 3. Orden de hitos

### Hito 1 — Roles y base multiempresa (F0, F5, F6, F7, F8)

**A (backend)**

- `V17`: `ADMIN → PLATAFORMA` (aceptando `ADMIN` transitoriamente).
- `empresas`, `parqueaderos`, tipos de vehículo con cupo fijo/ilimitado y `parqueadero_usuarios`.
- `parqueadero_id` en espacios, planos, ingresos y pagos; `numero` único por parqueadero.
- Alta del cajero junto con el parqueadero (email y contraseña temporal con BCrypt).
- Vinculación por identificador y filtrado por parqueadero en los servicios.

**B (frontend)**

- `RoleGuard`, `AuthService` y menú para los 5 roles.
- Pantallas de empresas/parqueaderos y vinculación.
- Selector de parqueadero activo.

> Aunque la spec pone F5 en fase 4, conviene hacer **toda la V17 al principio**. Casi todas las reglas de filtrado dependen de ella.

**Merge:** backend → `dev` → checks → frontend → `dev`.

### Hito 2 — Saldo, factura y crédito (F1, F2, F3)

**A**

- `V18`: `saldo`, `movimientos_saldo`, `facturas` y `creditos`.
- Recarga, movimientos, factura única por ingreso y cobro automático con saldo.
- Crédito del faltante: máximo 1, 3 días, sin intereses.
- Deuda vencida bloquea **solo** `POST /api/ingresos/reservar` con 403.

**B**

- `/mi-saldo`, recarga y movimientos.
- Pantalla de deuda con fecha límite.
- Aviso de cobro en `mis-parqueos`.

**Punto crítico (resuelto):** el recibo se abre al registrar la llegada, no al salir. El cajero confirma el cobro en pagaduría con `POST /api/facturas/{id}/pagar` (recalcula y debita el faltante), y `registrarSalida()` valida pago confirmado y tolerancia vigente. Ver F2 y F11.

**Merge:** backend → `dev` → checks → frontend → `dev`.

### Hito 3 — Hora de espera y operación (F4, F6, F7)

**A**

- `V19`: `fecha_reserva`, `fecha_limite_llegada`, `fecha_llegada` e índice.
- `PUT /api/ingresos/{id}/llegada`: dentro del límite sin cargo; tardía cobra 1 hora y activa la estancia.
- Job `@Scheduled` cada minuto: expira, libera espacio y factura 1 hora **solo si `tipo = HORA`**, sin doble cobro.
- Endpoints de verificación/estado de pago y filtro de pagos para el cajero.

**B**

- Botón "Registrar llegada" y contador.
- Vistas de guardia y cajero.
- Quitar el `'POR HORAS'` fijo: derivar `modalidad` de `tarifa.tipo`.

**Merge:** backend → `dev` → checks → frontend → `dev`.

### Hito 4 — Cierre (F9, F10, F11)

- B: menú inferior responsive con hamburguesa por rol y specs Cypress nuevas.
- A: primeros tests `*Test.java` (`mvn test` / `mvn verify`) y migración `V20` de tolerancia por empresa.
- B: flujo de pagaduría, autorización de salida y vista de configuración de tolerancia.
- Ambos: actualizar la columna `Estado` de la spec, `README.md` y `AGENTS.md` si cambió algo.
- Integración final: `docker compose up -d`, backend, frontend y E2E completo.

## 4. Reglas para no chocarse

1. **Un solo dueño de las migraciones**: la persona A crea V17–V20. Nunca se editan V1–V16.
2. **Archivos sensibles con dueño único**: `SecurityConfig`, `IngresoService` y `PagoService` los toca solo A; `app.routes.ts`, `app.component.html`, `AuthService` y `RoleGuard` los toca solo B.
3. **Contrato antes de codificar**: al inicio de cada hito acuerdan los endpoints y DTOs de la sección 6 de la spec y no los cambian a mitad de hito.
4. **Ramas cortas desde `dev`**, PR a `dev`, integración frecuente.
5. **Documentación fuera de las ramas de funcionalidad**: se actualiza al cerrar cada hito, no mientras se programa.
6. **Checks antes de integrar**: A corre `mvn test` / `mvn verify`; B corre `pnpm build` y `pnpm e2e`.

## 5. Riesgos resueltos y supervivientes

- [x] `PagoService.crear()` prohibía un segundo pago por ingreso: se resolvió reutilizando el pago confirmado y debitando solo el faltante.
- [x] Idempotencia de cobros y expiración mediante bloqueo y `facturas.ingreso_id` único.
- [x] `ADMIN` sigue funcionando como alias de `PLATAFORMA` para no invalidar tokens vigentes.
- [x] Deuda vencida bloquea únicamente `POST /api/ingresos/reservar`.
- [ ] `mvn` no está disponible en todos los entornos; la persona A necesita tenerlo instalado.
- [ ] Sin CI: los checks se ejecutan manualmente y no se exigen en cada PR.

## 6. Checklist de cierre

- [x] Endpoints y DTOs usados coinciden con la sección 6 de la spec.
- [x] Backend integrado en `dev` con `mvn test` / `mvn verify` en verde.
- [x] Frontend integrado en `dev` con `pnpm build` y el E2E del flujo nuevo en verde.
- [x] Columna `Estado` de la spec actualizada (F0–F11).
- [x] Worktree/rama de cada hito cerrada mediante PR a `dev`.
- [ ] E2E completo con backend y PostgreSQL levantados.
- [ ] Pipeline de CI que ejecute los checks en cada PR.
- [ ] Validación del despliegue (Netlify / Render / Supabase).

---

Volver al [índice de documentación](../INDICE_DOCUMENTACION.md).
