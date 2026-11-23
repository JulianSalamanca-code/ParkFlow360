# Especificaciones funcionales y técnicas — ParkFlow360

**Fecha:** 2026-11-22  
**Estado:** pendiente de implementación.  
**Nota:** este documento describe el **modelo objetivo**. El código es la fuente de verdad del estado actual (ver `AGENTS.md`); ninguna de las funcionalidades de aquí existe hoy en el repositorio.

---

## 1. Contexto y objetivos

ParkFlow360 se plantea como un **SaaS multiempresa**: la plataforma es gestionada exclusivamente por nosotros (`PLATAFORMA`) y las empresas gestionan sus propios parqueaderos.

Objetivos:

1. Eliminar la necesidad de hacer filas para pagar, mediante saldo recargable y cobro automático.
2. Soportar varios parqueaderos, cada uno con identificador propio, capacidad y configuración por tipo de vehículo.
3. Separar claramente las responsabilidades: pagos (cajero), campo (guardia), cliente (usuario) y plataforma.
4. Mantener el flujo actual (`reservar → llegada → salida → pago`) sin romperlo; los cambios se añaden encima.

## 2. Glosario

| Término | Definición |
|---|---|
| Parqueadero | Unidad operativa de una empresa, con nombre, identificador único y configuración de capacidad. |
| Identificador | Código público del parqueadero que permite al usuario vincularse a él. |
| Cupo fijo | Cantidad máxima definida para un tipo de vehículo dentro del parqueadero. Si no hay cupo fijo, la cantidad es ilimitada. |
| Factura | Cargo generado por una estancia o por una reserva expirada; única por ingreso. |
| Saldo | Dinero recargado por el usuario, disponible para cobros automáticos. |
| Crédito | Adelanto del faltante de una factura, con vencimiento a 3 días. |
| No-show | Reserva vencida sin registro de llegada del guardia. |

## 3. Roles y matriz de permisos

| Rol | Quién es | Alcance de datos | Permisos clave |
|---|---|---|---|
| `PLATAFORMA` | Nosotros (antes `ADMIN`) | Todos los parqueaderos | Control absoluto: empresas, parqueaderos, roles, facturación, reportes y pagos |
| `EMPRESA` | Dueña de parqueaderos | Sus parqueaderos | Alta y configuración de parqueaderos y cupos; asignar cajero y guardias; consultar usuarios vinculados |
| `CAJERO_PAGOS` | Usuario de pagos, creado automáticamente con cada parqueadero | **Exactamente 1** parqueadero | Revisar, crear y marcar pagos; ver facturas y créditos. No crea reservas ni modifica datos de campo |
| `GUARDIA` | Uno o varios por parqueadero | **Exactamente 1** parqueadero por guardia | Verificar veracidad de datos, marcar llegada, completar campos faltantes y consultar si un vehículo está al día. No gestiona pagos |
| `USUARIO` | Cliente | Varios parqueaderos vinculados, solo como usuario | Reservar, ver sus parqueos y vehículos, recargar saldo, sacar crédito. Bloqueo de reservas si tiene deuda vencida |

Reglas transversales de roles:

- `ADMIN` se renombra a `PLATAFORMA`. Se acepta `ADMIN` de forma transitoria para no invalidar tokens vigentes.
- El cajero se crea en la misma transacción que el parqueadero, con el **email y la contraseña temporal** ingresados en el formulario (BCrypt; la contraseña nunca se devuelve ni se registra).
- Un guardia pertenece a un único parqueadero, pero un parqueadero puede tener varios guardias.
- Un usuario común puede vincularse a varios parqueaderos mediante el identificador, pero siempre con rol `USUARIO` en cada uno.
- El filtrado por parqueadero se resuelve **en backend** desde la tabla de vinculación; no se asume que el JWT contenga los parqueaderos permitidos.

## 4. Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | Todo usuario tiene un saldo `>= 0`. Toda recarga es `> 0` y genera un movimiento inmutable con saldo anterior y posterior. |
| RN-02 | La factura se genera **al registrar la salida** de un ingreso activo o **al expirar** una reserva por no-show. Existe como máximo una factura por ingreso. |
| RN-03 | El valor de la factura se calcula en backend. Si la tarifa es `HORA`: `ceil(horas entre llegada y salida) * tarifaValor`. Para otros tipos de tarifa: una vez `tarifaValor`. |
| RN-04 | Si `saldo >= valor` de la factura, se debita el saldo, se crea un `Pago` con `metodoPago = SALDO` y `estadoPago = CONFIRMADO`, y la factura pasa a `PAGADA`. |
| RN-05 | Si `saldo < valor`, **no se crea el pago**; la factura queda `PENDIENTE` y el usuario puede recargar y reintentar o solicitar crédito. |
| RN-06 | El crédito cubre **solo el faltante** de una factura pendiente. Máximo **1 crédito pendiente por usuario**, **sin intereses**. |
| RN-07 | El crédito vence **3 días después de otorgado**. Un crédito `PENDIENTE` con fecha límite vencida constituye deuda vencida. |
| RN-08 | La deuda vencida bloquea **únicamente** `POST /api/ingresos/reservar`, con respuesta 403 y mensaje que indique la deuda y la fecha límite. Los demás flujos siguen disponibles. |
| RN-09 | Cada reserva tiene `fecha_limite_llegada = fecha_reserva + 1 hora`. El guardia registra la llegada: dentro del límite no hay cargo; fuera del límite se factura **1 hora** y la estancia continúa normalmente. |
| RN-10 | Un job programado expira cada minuto las reservas `RESERVADO` vencidas: pasan a `CANCELADO`, liberan el espacio y generan la factura de 1 hora **solo si la tarifa es por horas** (`tipo = HORA`). El cobro es idempotente por ingreso. |
| RN-11 | Si el guardia registra la llegada antes de que el job expire la reserva, se cobra la hora y se activa la estancia. Si el job ya la canceló, la hora ya fue facturada y se requiere una nueva reserva (sin doble cobro). |
| RN-12 | La modalidad de la reserva se deriva del `tipo` de tarifa seleccionada; se elimina el valor `'POR HORAS'` fijo del frontend. |
| RN-13 | Los espacios tienen `numero` único **por parqueadero**; la placa del vehículo sigue siendo única a nivel global. |
| RN-14 | Si un tipo de vehículo tiene cupo fijo, no se pueden crear/vincular espacios de ese tipo por encima de la cantidad configurada. |
| RN-15 | El usuario vincula parqueaderos ingresando su identificador único. Solo ve los parqueaderos a los que está vinculado. |

## 5. Modelo de datos y migraciones previstas

La última migración existente es `V16`. Se prevén tres migraciones nuevas, ordenadas por fase de implementación:

### V17 — Roles, empresas y parqueaderos (F0, F5–F8)

- `empresas`: `id`, `nombre`, `identificador`, `activo`, `fecha_creacion`.
- `parqueaderos`: `id`, `empresa_id`, `nombre`, `identificador` (único), `capacidad_vehiculos`, `activo`, `fecha_creacion`.
- `parqueaderos_tipos_vehiculo`: `id`, `parqueadero_id`, `tipo`, `cantidad_fija` (booleano), `cantidad` (nullable), único por `(parqueadero_id, tipo)`.
- `parqueadero_usuarios`: `id`, `parqueadero_id`, `usuario_id`, `rol_asignado` (`CAJERO_PAGOS`, `GUARDIA`, `USUARIO`). Índice parcial que garantiza que un cajero o guardia tenga un solo parqueadero.
- `usuarios.rol`: actualización de `ADMIN` a `PLATAFORMA`.
- Columna `parqueadero_id` en `espacios`, `planos`, `ingresos` y `pagos`.
- `espacios.numero` pasa a ser único por `(parqueadero_id, numero)`.

### V18 — Saldo, facturas y créditos (F1–F3)

- `usuarios.saldo DECIMAL(12,2) DEFAULT 0`, con restricción `>= 0`.
- `movimientos_saldo`: `id`, `usuario_id`, `tipo` (`RECARGA`, `DEBITO`, `CREDITO`, `PAGO_CREDITO`), `valor`, `saldo_anterior`, `saldo_posterior`, `referencia`, `fecha`.
- `facturas`: `id`, `ingreso_id` (único), `valor`, `estado` (`PENDIENTE`, `PAGADA`), `fecha_emision`, `fecha_pago`.
- `creditos`: `id`, `usuario_id`, `factura_id`, `monto`, `monto_pendiente`, `fecha_otorgamiento`, `fecha_limite`, `estado` (`PENDIENTE`, `PAGADO`, `VENCIDO`).

### V19 — Hora de espera (F4)

- `ingresos.fecha_reserva`, `ingresos.fecha_limite_llegada`, `ingresos.fecha_llegada`.
- Índice sobre `(estado, fecha_limite_llegada)` para la consulta del job.

## 6. Endpoints previstos

| Método y ruta | Roles | Descripción | Fase |
|---|---|---|---|
| `POST /api/empresas/{id}/parqueaderos` | `EMPRESA`, `PLATAFORMA` | Crea parqueadero y su usuario de pagos | 4 |
| `POST /api/parqueaderos/vincular` | `USUARIO` | Vincula al usuario con un identificador | 4 |
| `GET /api/parqueaderos/mios` | `USUARIO`, `EMPRESA`, `PLATAFORMA` | Parqueaderos del usuario | 4 |
| `GET /api/saldos/mi-cuenta` | `USUARIO`, `PLATAFORMA` | Saldo y movimientos | 1 |
| `POST /api/saldos/recargar` | `USUARIO`, `PLATAFORMA` | Recarga de saldo | 1 |
| `GET /api/creditos/mios` | `USUARIO`, `CAJERO_PAGOS` | Créditos y deuda del usuario | 2 |
| `POST /api/creditos` | `USUARIO` | Solicita crédito para factura pendiente | 2 |
| `POST /api/facturas/{id}/pagar` | `USUARIO`, `PLATAFORMA` | Reintento de cobro tras recarga | 1 |
| `PUT /api/ingresos/{id}/llegada` | `GUARDIA`, `PLATAFORMA` | Registra llegada dentro o fuera del límite | 3 |
| `GET /api/ingresos/{id}/estado-pago` | `GUARDIA`, `CAJERO_PAGOS` | Consulta si el vehículo está al día | 4 |
| `PUT /api/ingresos/{id}/verificar` | `GUARDIA` | Verifica datos y completa campos faltantes | 4 |
| `POST /api/pagos` | `CAJERO_PAGOS`, `PLATAFORMA` | Registro de pagos (antes solo `ADMIN`) | 4 |

Los endpoints actuales de vehículos, espacios, tarifas, ingresos, pagos, reportes y usuarios se conservan, ajustando los roles de `ADMIN` a `PLATAFORMA` y aplicando el filtro por parqueadero.

## 7. Cambios por capa

**Backend**

- Nuevos módulos `saldos`, `creditos`, `empresas`, `parqueaderos`; extensión de `pagos` con `FacturaService` y cobro automático.
- `IngresoService`: validación de deuda en `reservar()`, registro de llegada, expiración y generación de facturas.
- `SecurityConfig`: roles nuevos y reglas por ruta; servicios con filtro por parqueadero.
- `@Scheduled` para expiración de reservas; transacciones con bloqueo pesimista para evitar cobros duplicados.

**Frontend**

- Rutas y menú por rol: panel `EMPRESA`, vista `CAJERO_PAGOS`, vista `GUARDIA`, `MI SALDO` del cliente.
- `RoleGuard` y menú inferior adaptados a los cinco roles.
- Selector de parqueadero activo para usuarios vinculados a varios.
- `modalidad` derivada de `tarifa.tipo`.

## 8. Criterios de aceptación (resumen)

- **Dado** un usuario con saldo suficiente, **cuando** se registra su salida, **entonces** se crea un pago confirmado y no requiere intervención del cajero.
- **Dado** un usuario sin saldo suficiente, **cuando** se genera su factura, **entonces** no se crea pago y puede solicitar un crédito por el faltante.
- **Dado** un crédito con más de 3 días de otorgado sin pagar, **cuando** el usuario intente reservar, **entonces** recibe 403 y no se crea la reserva.
- **Dado** una reserva por tarifa `HORA` sin llegada registrada después de 1 hora, **entonces** se cancela, se libera el espacio y se factura 1 hora.
- **Dado** un guardia, **cuando** registra una llegada tardía, **entonces** se cobra 1 hora y la estancia queda `ACTIVO`.
- **Dado** un cajero, **cuando** inicie sesión, **entonces** solo ve los pagos de su parqueadero.
- **Dado** un usuario vinculado a dos parqueaderos, **cuando** consulta sus parqueos, **entonces** ve únicamente los de ambos parqueaderos.

## 9. Fuera de alcance

- Integración real con Wompi (hoy solo hay columnas sin uso en V12).
- Empaquetado Electron completo; solo se mejora la navegación responsive.
- Envío de correos/SMTP, intereses de crédito y más de un parqueadero por cajero o guardia.

## 10. Decisiones confirmadas

1. `ADMIN` se renombra a `PLATAFORMA`.
2. Cupo fijo vs ilimitado por tipo de vehículo.
3. Credencial del cajero: email y contraseña temporal ingresados en el formulario de alta del parqueadero.
4. El usuario se vincula por el identificador del parqueadero.
5. Cajero y guardia: exactamente 1 parqueadero cada uno (varios guardias por parqueadero).
6. Deuda vencida bloquea solo nuevas reservas.
7. Factura al salir o expirar; crédito del faltante, 1 activo, 3 días, sin intereses.
8. No-show se cobra igual que cualquier factura, solo con tarifa por horas.
9. Llegada tardía: se cobra la hora y se permite estacionar.

## 11. Tabla maestra de implementación

| ID | Funcionalidad | Descripción | Roles | Backend | Frontend | Fase | Estado |
|---|---|---|---|---|---|---|---|
| F0 | Migración de roles | Renombrar `ADMIN` a `PLATAFORMA` y ajustar accesos | `PLATAFORMA` | V17: UPDATE de roles; `SecurityConfig`, `JwtService`, seed | `RoleGuard`, menú y textos | 0 | Pendiente |
| F1 | Saldo y recarga | Recarga con ledger de movimientos | `USUARIO`, `PLATAFORMA` | V18: `saldo`, `movimientos_saldo`; `/api/saldos/*` | Ruta `/mi-saldo` | 1 | Pendiente |
| F2 | Factura y cobro automático | Factura al salir/expirar; cobro con saldo | `USUARIO`, `CAJERO_PAGOS`, `PLATAFORMA` | V18: `facturas`; `cobrarFactura()` desde salida y expiración | Aviso de cobro en `mis-parqueos` | 1 | Pendiente |
| F3 | Crédito y bloqueo | Faltante, 1 activo, 3 días, sin intereses; deuda bloquea reservar | `USUARIO`, `CAJERO_PAGOS` | V18: `creditos`; `POST /api/creditos`; 403 en `reservar()` | Pantalla de deuda y fecha límite | 2 | Pendiente |
| F4 | Hora de espera y no-show | Llegada marcada por guardia; cobro de 1 h solo con tarifa `HORA` | `GUARDIA`, `USUARIO`, `CAJERO_PAGOS` | V19: fechas en `ingresos`; `PUT /api/ingresos/{id}/llegada`; job `@Scheduled` | Botón "Registrar llegada" y contador | 3 | Pendiente |
| F5 | Empresas y parqueaderos | Nombre, identificador único, capacidad y tipos con cupo fijo/ilimitado | `PLATAFORMA`, `EMPRESA` | V17: `empresas`, `parqueaderos`, `parqueaderos_tipos_vehiculo`; `parqueadero_id` en espacios/planos/ingresos/pagos | Panel de alta y configuración | 4 | Pendiente |
| F6 | Usuario de pagos | Creado con el parqueadero (email y contraseña temporal del formulario); gestiona pagos de su único parqueadero | `CAJERO_PAGOS`, `EMPRESA`, `PLATAFORMA` | V17: usuario + asignación 1:1; filtro de pagos por parqueadero | Vista de pagos del cajero | 4 | Pendiente |
| F7 | Guardias | Verificación de datos, llegada, campos faltantes y estado de pago | `GUARDIA`, `EMPRESA` | V17: rol `GUARDIA` + asignación 1:1; endpoints de verificación y consulta | Vista operativa del guardia | 4 | Pendiente |
| F8 | Vinculación múltiple | El usuario se vincula por identificador y ve solo sus parqueaderos | `USUARIO`, `EMPRESA` | V17: `parqueadero_usuarios`; filtros por parqueadero en servicios | Selector de parqueadero | 4 | Pendiente |
| F9 | Menú inferior responsive | Ítems clave + hamburguesa abajo | Todos | — | `app.component.html`, `styles.css` | 5 | Pendiente |
| F10 | Pruebas y documentación | Primeros tests unitarios, Cypress y 2ª entrega de `AGENTS.md` | Todos | `*Test.java`, specs nuevas | Specs E2E nuevas | 5 | Pendiente |
