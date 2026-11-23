# Especificaciones funcionales y técnicas — ParkFlow360

**Fecha:** 2026-11-23  
**Estado:** modelo objetivo con el flujo de pagaduría y tolerancia de salida implementado en la rama `feat/flujo-pagaduria-salida-tolerancia`.  
**Nota:** este documento describe el **modelo objetivo y el flujo vigente**. El código es la fuente de verdad del estado exacto (ver `AGENTS.md`); las migraciones V17–V20 ya existen y no se reescriben.

La ejecución por hitos está en [`PLAN_IMPLEMENTACION.md`](PLAN_IMPLEMENTACION.md).

---

## 1. Contexto y objetivos

ParkFlow360 se plantea como un **SaaS multiempresa**: la plataforma es gestionada exclusivamente por nosotros (`PLATAFORMA`) y las empresas gestionan sus propios parqueaderos.

Objetivos:

1. Eliminar la necesidad de hacer filas para pagar, mediante saldo recargable y cobro automático.
2. Soportar varios parqueaderos, cada uno con identificador propio, capacidad y configuración por tipo de vehículo.
3. Separar claramente las responsabilidades: pagos (cajero), campo (guardia), cliente (usuario) y plataforma.
4. Operar el flujo `reservar → llegada (recibo) → pago en pagaduría → salida autorizada por el guardia`, con tolerancia configurable por empresa.

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
| Recibo | Factura abierta al registrar la llegada, con una unidad de la tarifa elegida. Debe pagarse en pagaduría antes de salir. |
| Pagaduría | Punto donde el cajero confirma el cobro de la factura o de la diferencia de una tolerancia vencida. |
| Tolerancia de salida | Minutos concedidos desde el pago para retirar el vehículo sin cargo adicional. La define la empresa y aplica a todos sus parqueaderos. |

## 3. Roles y matriz de permisos

| Rol | Quién es | Alcance de datos | Permisos clave |
|---|---|---|---|
| `PLATAFORMA` | Nosotros (antes `ADMIN`) | Todos los parqueaderos | Control absoluto: empresas, parqueaderos, roles, facturación, reportes y pagos |
| `EMPRESA` | Dueña de parqueaderos | Sus parqueaderos | Alta y configuración de parqueaderos y cupos; fijar la tolerancia de salida; asignar cajero y guardias; consultar reportes y usuarios vinculados |
| `CAJERO_PAGOS` | Usuario de pagos, creado automáticamente con cada parqueadero | **Exactamente 1** parqueadero | Revisar cobros pendientes (incluidas diferencias), confirmar pagos, ver facturas, créditos y estado de pago. No crea reservas ni modifica datos de campo |
| `GUARDIA` | Uno o varios por parqueadero | **Exactamente 1** parqueadero por guardia | Verificar datos, marcar llegada, completar campos faltantes, consultar estado de pago y autorizar la salida solo si está pagada y dentro de la tolerancia. No gestiona pagos |
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
| RN-02 | El recibo (factura) se abre **al registrar la llegada** del guardia con **1 unidad de la tarifa elegida**. Existe como máximo una factura por ingreso. `reservar` no factura. La expiración por no-show factura 1 hora solo si `tarifa.tipo = HORA`. |
| RN-03 | El valor vigente se calcula en backend desde `fecha_llegada`. Si la tarifa es `HORA`: `ceil(horas transcurridas) * tarifaValor`. Para `DIA`, `SEMANA`, `MES` y `MOMENTO`: una sola vez `tarifaValor`, aunque pasen horas. El cobro en pagaduría recalcula con la hora actual; la salida dentro de la tolerancia **no** vuelve a ajustar. |
| RN-04 | En pagaduría, el cajero confirma el cobro: se debita **solo el faltante** del saldo, se reutiliza el `Pago` confirmado existente y la factura pasa a `PAGADA` con `fecha_pago`. Ese momento reinicia la tolerancia de salida. |
| RN-05 | Si `saldo < faltante`, no se crea un pago nuevo: la factura queda `PENDIENTE` (o conserva el pago parcial) y el usuario puede recargar y reintentar o solicitar crédito por el faltante. |
| RN-06 | El crédito cubre **solo el faltante** de una factura pendiente. Máximo **1 crédito pendiente por usuario**, **sin intereses**. |
| RN-07 | El crédito vence **3 días después de otorgado**. Un crédito `PENDIENTE` con fecha límite vencida constituye deuda vencida. |
| RN-08 | La deuda vencida bloquea **únicamente** `POST /api/ingresos/reservar`, con respuesta 403 y mensaje que indique la deuda y la fecha límite. Los demás flujos siguen disponibles. |
| RN-09 | Cada reserva tiene `fecha_limite_llegada = fecha_reserva + 1 hora`. El guardia registra la llegada: dentro del límite no hay cargo; fuera del límite se factura **1 hora** y la estancia continúa normalmente. |
| RN-10 | Un job programado expira cada minuto las reservas `RESERVADO` vencidas: pasan a `CANCELADO`, liberan el espacio y generan la factura de 1 hora **solo si la tarifa es por horas** (`tipo = HORA`). El cobro es idempotente por ingreso. |
| RN-11 | Si el guardia registra la llegada antes de que el job expire la reserva, se cobra la hora y se activa la estancia. Si el job ya la canceló, la hora ya fue facturada y se requiere una nueva reserva (sin doble cobro). |
| RN-12 | La modalidad de la reserva se deriva del `tipo` de tarifa seleccionada; se elimina el valor `'POR HORAS'` fijo del frontend. |
| RN-13 | La **tolerancia de salida** se configura **por empresa** (0–240 minutos, valor por defecto `30`) y la heredan todos sus parqueaderos. Se cuenta desde `fecha_pago`. Dentro de la tolerancia no se cobra extra aunque se cruce la hora; si vence, se bloquea la salida y se cobra en pagaduría **solo la diferencia**, calculada con el `tipo` de tarifa vigente. |
| RN-14 | Los espacios tienen `numero` único **por parqueadero**; la placa del vehículo sigue siendo única a nivel global. |
| RN-15 | Si un tipo de vehículo tiene cupo fijo, no se pueden crear/vincular espacios de ese tipo por encima de la cantidad configurada. |
| RN-16 | El usuario vincula parqueaderos ingresando su identificador único. Solo ve los parqueaderos a los que está vinculado. |
| RN-17 | El guardia solo autoriza la salida si la factura está `PAGADA`, existe un `Pago` `CONFIRMADO` y la tolerancia sigue vigente. El usuario **no** registra su propia salida. Mensajes: `"El conductor debe pagar en pagaduría antes de salir del parqueadero."` y `"La tolerancia de salida venció; debe cancelar el tiempo adicional en pagaduría."` |
| RN-18 | `EMPRESA` ve reportes únicamente de sus parqueaderos; `PLATAFORMA` ve todos. El filtrado se hace en backend desde la vinculación. |

## 5. Modelo de datos y migraciones previstas

Las migraciones `V17` a `V20` ya están en el repositorio y no se reescriben. La última es `V20`; cualquier cambio posterior agregará `V21` o superior.

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

### V20 — Tolerancia de salida (F11 / RN-13)

- `empresas.tolerancia_salida_minutos INTEGER NOT NULL DEFAULT 30`.
- Restricción `ck_empresas_tolerancia_salida`: valor entre `0` y `240`.
- El contador del guardia se calcula como `fecha_pago + tolerancia_salida_minutos`.

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
| `POST /api/facturas/{id}/pagar` | `USUARIO`, `CAJERO_PAGOS`, `PLATAFORMA` | Confirma el cobro en pagaduría; si el ingreso está `ACTIVO`, recalcula y debita solo el faltante | 1, 11 |
| `PUT /api/ingresos/{id}/llegada` | `GUARDIA`, `PLATAFORMA` | Registra llegada dentro o fuera del límite y abre el recibo | 3, 11 |
| `GET /api/ingresos/{id}/estado-pago` | `GUARDIA`, `EMPRESA`, `CAJERO_PAGOS`, `PLATAFORMA` | Consulta pago, faltante, tolerancia y `puedeSalir` | 4, 11 |
| `PUT /api/ingresos/{id}/verificar` | `GUARDIA`, `EMPRESA`, `PLATAFORMA` | Verifica datos y completa campos faltantes | 4 |
| `PUT /api/ingresos/{id}/salida` | `GUARDIA`, `PLATAFORMA` | Autoriza la salida solo si hay pago confirmado y la tolerancia está vigente | 11 |
| `GET /api/ingresos/activos` | `CAJERO_PAGOS`, `GUARDIA`, `EMPRESA`, `PLATAFORMA` | Ingresos en curso para cobro y control de acceso | 4, 11 |
| `PUT /api/empresas/{id}/configuracion` | `EMPRESA`, `PLATAFORMA` | Actualiza la tolerancia de salida heredada por los parqueaderos | 11 |
| `GET /api/reportes/**` | `EMPRESA`, `PLATAFORMA` | Reportes filtrados por parqueadero | 5, 11 |
| `POST /api/pagos` | `CAJERO_PAGOS`, `PLATAFORMA` | Registro de pagos (antes solo `ADMIN`) | 4 |

Los endpoints actuales de vehículos, espacios, tarifas, ingresos, pagos, reportes y usuarios se conservan, ajustando los roles de `ADMIN` a `PLATAFORMA` y aplicando el filtro por parqueadero.

## 7. Cambios por capa

**Backend**

- Nuevos módulos `saldos`, `creditos`, `empresas`, `parqueaderos`; extensión de `pagos` con `FacturaService` y cobro automático.
- `IngresoService`: validación de deuda en `reservar()`, registro de llegada (abre recibo), expiración y validación de pago + tolerancia en `registrarSalida()`.
- `FacturaService.pagar()`: recalcula el valor si el ingreso sigue `ACTIVO`, debita solo el faltante y reutiliza el pago confirmado.
- `ToleranciaSalidaService` y `EstadoPagoService`: resuelven la tolerancia por empresa y exponen `minutosTolerancia`, `fechaLimiteSalida`, `faltante` y `puedeSalir`.
- `ReporteService`: filtra su propio universo de parqueaderos para `EMPRESA`; `PLATAFORMA` ve todo.
- `SecurityConfig`: roles nuevos y reglas por ruta; servicios con filtro por parqueadero.
- `@Scheduled` para expiración de reservas; transacciones con bloqueo pesimista para evitar cobros duplicados.

**Frontend**

- Rutas y menú por rol: panel `EMPRESA`, vista `CAJERO_PAGOS`, vista `GUARDIA`, `MI SALDO` del cliente.
- `RoleGuard` y menú inferior adaptados a los cinco roles.
- Selector de parqueadero activo para usuarios vinculados a varios.
- `modalidad` derivada de `tarifa.tipo`.
- Guardia: badge de pago, contador “Tiene X min para salir de la universidad” y botón de salida deshabilitado sin pago o con tolerancia vencida.
- Cajero: cobros pendientes con distinción de “Tiempo adicional” y mensaje de confirmación acorde.
- Empresa: campo editable de tolerancia que aplica a todos sus parqueaderos.

## 8. Criterios de aceptación (resumen)

- **Dado** un usuario con saldo suficiente, **cuando** el guardia registra su llegada, **entonces** se abre un recibo con 1 unidad de la tarifa.
- **Dado** un recibo pendiente, **cuando** el cajero confirma el cobro en pagaduría, **entonces** se debita el faltante, se crea/reutiliza el pago confirmado y se reinicia la tolerancia.
- **Dado** un pago confirmado, **cuando** el guardia registra la salida dentro de la tolerancia, **entonces** no se cobra ningún valor adicional aunque se haya cruzado la hora.
- **Dado** un pago confirmado con tolerancia vencida, **cuando** el guardia intenta registrar la salida, **entonces** recibe 400 y debe enviar al conductor a pagar la diferencia en pagaduría.
- **Dado** una tarifa `DIA`, `SEMANA`, `MES` o `MOMENTO`, **cuando** pasen horas o se cobre la diferencia, **entonces** el valor sigue siendo único y no se multiplica por horas.
- **Dado** un usuario sin saldo suficiente, **cuando** se confirma el cobro, **entonces** no se crea un pago nuevo y puede recargar y reintentar o solicitar crédito por el faltante.
- **Dado** un crédito con más de 3 días de otorgado sin pagar, **cuando** el usuario intente reservar, **entonces** recibe 403 y no se crea la reserva.
- **Dado** una reserva por tarifa `HORA` sin llegada registrada después de 1 hora, **entonces** se cancela, se libera el espacio y se factura 1 hora.
- **Dado** un guardia, **cuando** registra una llegada tardía, **entonces** se cobra 1 hora y la estancia queda `ACTIVO`.
- **Dado** un cajero, **cuando** inicie sesión, **entonces** solo ve los pagos de su parqueadero.
- **Dado** una empresa con dos parqueaderos, **cuando** consulta reportes, **entonces** ve solo datos de esos parqueaderos; la plataforma ve todos.
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
7. Recibo al entrar; crédito del faltante, 1 activo, 3 días, sin intereses. La expiración por no-show factura 1 hora solo con tarifa `HORA`.
8. No-show se cobra igual que cualquier factura, solo con tarifa por horas.
9. Llegada tardía: se cobra la hora y se permite estacionar.
10. El pago se confirma en pagaduría antes de salir; el cajero cobra el faltante y reutiliza el pago confirmado.
11. El guardia, no el usuario, autoriza la salida y solo si el pago está vigente.
12. La tolerancia es por empresa (no por parqueadero), por defecto 30 minutos, y se reinicia con cada cobro confirmado.
13. Dentro de la tolerancia no hay cobro extra; vencida, se cobra solo la diferencia. Las tarifas de valor único no se multiplican por horas.

## 11. Tabla maestra de implementación

| ID | Funcionalidad | Descripción | Roles | Backend | Frontend | Fase | Estado |
|---|---|---|---|---|---|---|---|
| F0 | Migración de roles | Renombrar `ADMIN` a `PLATAFORMA` y ajustar accesos | `PLATAFORMA` | V17: UPDATE de roles; `SecurityConfig`, `JwtService`, seed | `RoleGuard`, menú y textos | 0 | Pendiente |
| F1 | Saldo y recarga | Recarga con ledger de movimientos | `USUARIO`, `PLATAFORMA` | V18: `saldo`, `movimientos_saldo`; `/api/saldos/*` | Ruta `/mi-saldo` | 1 | Pendiente |
| F2 | Factura y cobro en pagaduría | Recibo al llegar; cobro del faltante con saldo y reutilización del pago | `USUARIO`, `CAJERO_PAGOS`, `PLATAFORMA` | V18 + V20: `facturas`; `FacturaService.pagar()` recalcula con `now` si el ingreso está `ACTIVO` | Cobros pendientes del cajero y aviso en `mis-parqueos` | 1, 11 | Implementado |
| F3 | Crédito y bloqueo | Faltante, 1 activo, 3 días, sin intereses; deuda bloquea reservar | `USUARIO`, `CAJERO_PAGOS` | V18: `creditos`; `POST /api/creditos`; 403 en `reservar()` | Pantalla de deuda y fecha límite | 2 | Pendiente |
| F4 | Hora de espera y no-show | Llegada marcada por guardia; cobro de 1 h solo con tarifa `HORA` | `GUARDIA`, `USUARIO`, `CAJERO_PAGOS` | V19: fechas en `ingresos`; `PUT /api/ingresos/{id}/llegada`; job `@Scheduled` | Botón "Registrar llegada" y contador | 3 | Pendiente |
| F5 | Empresas y parqueaderos | Nombre, identificador único, capacidad y tipos con cupo fijo/ilimitado | `PLATAFORMA`, `EMPRESA` | V17: `empresas`, `parqueaderos`, `parqueaderos_tipos_vehiculo`; `parqueadero_id` en espacios/planos/ingresos/pagos | Panel de alta y configuración | 4 | Pendiente |
| F6 | Usuario de pagos | Creado con el parqueadero (email y contraseña temporal del formulario); gestiona pagos de su único parqueadero | `CAJERO_PAGOS`, `EMPRESA`, `PLATAFORMA` | V17: usuario + asignación 1:1; filtro de pagos por parqueadero | Vista de pagos del cajero | 4 | Pendiente |
| F7 | Guardias | Verificación de datos, llegada, estado de pago y autorización de salida | `GUARDIA`, `EMPRESA`, `PLATAFORMA` | V17 + V20: `PUT /api/ingresos/{id}/salida` valida pago y tolerancia; `estado-pago` expone contador | Badge de pago, contador y botón bloqueado | 4, 11 | Implementado |
| F8 | Vinculación múltiple | El usuario se vincula por identificador y ve solo sus parqueaderos | `USUARIO`, `EMPRESA` | V17: `parqueadero_usuarios`; filtros por parqueadero en servicios | Selector de parqueadero | 4 | Implementado |
| F9 | Menú inferior responsive | Ítems clave + hamburguesa abajo | Todos | — | `app.component.html`, `styles.css` | 5 | Implementado |
| F10 | Pruebas y documentación | Tests unitarios, integración H2, Cypress y `AGENTS.md` | Todos | `mvn test` / `mvn verify`; `FacturaEntradaAjusteIntegrationTest`, `F7GuardiaIntegrationTest`, `ReportePorEmpresaIntegrationTest` | `flujo-pagaduria-salida.cy.ts` | 5, 11 | Implementado (sin CI) |
| F11 | Pagaduría, salida y tolerancia | Pago antes de salir, guardia autoriza con tolerancia por empresa y reportes filtrados | `CAJERO_PAGOS`, `GUARDIA`, `EMPRESA`, `PLATAFORMA` | V20: `empresas.tolerancia_salida_minutos`; `ToleranciaSalidaService`, `SecurityConfig`, `ReporteService` | Cajero con diferencias, guardia con contador, empresa edita tolerancia | 11 | Implementado |
