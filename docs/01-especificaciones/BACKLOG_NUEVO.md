# Backlog nuevo — ParkFlow360

> Historias derivadas del plan [`ESPECIFICACIONES_PARKFLOW360.md`](ESPECIFICACIONES_PARKFLOW360.md).  
> Estado: **implementado** (F0–F11). El estado por funcionalidad está en la tabla maestra de la [especificación](ESPECIFICACIONES_PARKFLOW360.md). El backlog original de 18 historias se conserva en [`HISTORIAL_USUARIO_PARKFLOW360.md`](../04-historico/HISTORIAL_USUARIO_PARKFLOW360.md) como documento histórico.

## Resumen

| ID | Historia | Rol principal | Fase | Funcionalidad |
|---|---|---|---|---|
| HU-N01 | Migrar el rol ADMIN a PLATAFORMA | PLATAFORMA | 0 | F0 |
| HU-N02 | Recargar saldo y consultar movimientos | USUARIO | 1 | F1 |
| HU-N03 | Pago automático al salir o expirar | USUARIO | 1 | F2 |
| HU-N04 | Crédito del faltante y bloqueo por deuda | USUARIO | 2 | F3 |
| HU-N05 | Hora de espera, llegada y no-show | GUARDIA | 3 | F4 |
| HU-N06 | Crear y configurar parqueaderos | EMPRESA | 4 | F5 |
| HU-N07 | Usuario de pagos por parqueadero | CAJERO_PAGOS | 4 | F6 |
| HU-N08 | Verificación en campo y estado de pago | GUARDIA | 4 | F7 |
| HU-N09 | Vinculación a varios parqueaderos | USUARIO | 4 | F8 |
| HU-N10 | Menú inferior responsive | Todos | 5 | F9 |
| HU-N11 | Pruebas y documentación del plan | Equipo | 5 | F10 |
| HU-N12 | Pagaduría, salida y tolerancia por empresa | CAJERO_PAGOS, GUARDIA, EMPRESA | 11 | F11 |

## HU-N01 — Migrar el rol ADMIN a PLATAFORMA

**Como** plataforma, **quiero** que el rol con control absoluto se llame `PLATAFORMA` **para** diferenciarlo de los nuevos roles del SaaS.

- Criterios: `ADMIN` pasa a `PLATAFORMA` en `SecurityConfig`, JWT y frontend; se acepta `ADMIN` de forma transitoria para no invalidar tokens vigentes; datos semilla actualizados.
- Reglas: RN-01 a RN-15 aplican al nuevo modelo. Ver F0.

## HU-N02 — Recargar saldo y consultar movimientos

**Como** usuario, **quiero** recargar dinero y ver mi saldo y movimientos **para** pagar sin hacer filas.

- Criterios: recarga mayor a cero; saldo nunca negativo; cada movimiento registra saldo anterior y posterior.
- Reglas: RN-01. Ver F1.

## HU-N03 — Recibo al entrar y cobro en pagaduría

**Como** usuario, **quiero** que se abra un recibo al llegar y pagarlo en pagaduría antes de salir **para** que el guardia pueda autorizar mi salida.

- Criterios: factura única por ingreso, abierta al registrar la llegada con 1 unidad de tarifa; `reservar` no factura; el cajero confirma el cobro con `POST /api/facturas/{id}/pagar`, recalculando el valor si el ingreso está `ACTIVO` y debitando solo el faltante; sin saldo suficiente la factura queda `PENDIENTE`.
- Reglas: RN-02, RN-03, RN-04, RN-05. Ver F2.

## HU-N04 — Crédito del faltante y bloqueo por deuda

**Como** usuario, **quiero** solicitar un crédito por el faltante cuando no tengo saldo suficiente **para** poder usar el parqueadero y regularizar después.

- Criterios: máximo un crédito pendiente, sin intereses, vencimiento a 3 días; la deuda vencida bloquea solo nuevas reservas con 403 y mensaje explicativo.
- Reglas: RN-06, RN-07, RN-08. Ver F3.

## HU-N05 — Hora de espera, llegada y no-show

**Como** guardia, **quiero** registrar la llegada del usuario y verificar sus datos **para** controlar la hora de espera y completar información faltante.

- Criterios: límite de una hora desde la reserva; llegada tardía factura una hora y permite estacionar; expiración sin llegada cancela, libera el espacio y factura una hora solo si la tarifa es `HORA`; cobro idempotente.
- Reglas: RN-09, RN-10, RN-11, RN-12. Ver F4.

## HU-N06 — Crear y configurar parqueaderos

**Como** empresa, **quiero** crear parqueaderos con nombre, identificador único, capacidad y cupos por tipo de vehículo **para** gestionar mi operación.

- Criterios: identificador único; capacidad de vehículos; tipos con cupo fijo o ilimitado; espacios únicos por parqueadero.
- Reglas: RN-14, RN-15. Ver F5.

## HU-N07 — Usuario de pagos por parqueadero

**Como** plataforma, **quiero** que al crear un parqueadero se cree automáticamente su usuario de pagos **para** que cada parqueadero tenga un responsable de cobros.

- Criterios: el formulario solicita email y contraseña temporal; se guarda con BCrypt; el cajero pertenece a un único parqueadero y solo ve/gestiona sus pagos y facturas.
- Ver F6.

## HU-N08 — Verificación en campo y estado de pago

**Como** guardia, **quiero** verificar la veracidad de los datos, completar campos faltantes y consultar si un vehículo está al día **para** operar el parqueadero sin gestionar pagos.

- Criterios: el guardia no puede crear ni marcar pagos; puede consultar el estado de pago del vehículo; pertenece a un único parqueadero (puede haber varios guardias por parqueadero).
- Ver F7.

## HU-N09 — Vinculación a varios parqueaderos

**Como** usuario, **quiero** vincularme a varios parqueaderos mediante su identificador **para** usar cualquiera de ellos sin cambiar de cuenta.

- Criterios: vinculación por identificador; solo se ven los parqueaderos propios; el rol sigue siendo `USUARIO` en todos.
- Reglas: RN-16. Ver F8.

## HU-N10 — Menú inferior responsive

**Como** usuario en pantalla pequeña, **quiero** una barra inferior con las opciones principales y un menú de hamburguesa **para** navegar cuando el sidebar se oculta.

- Criterios: barra fija abajo con versión por rol; hamburguesa al final; `padding-bottom` en el contenido; sin menú en la parte superior.
- Ver F9.

## HU-N11 — Pruebas y documentación del plan

**Como** equipo, **queremos** pruebas automáticas y documentación actualizada **para** verificar las nuevas reglas de negocio.

- Criterios: tests de integración del backend y specs Cypress del flujo de pagaduría; columna `Estado` de la spec actualizada al cerrar cada fase.
- Ver F10.

## HU-N12 — Pagaduría, salida y tolerancia por empresa

**Como** empresa, **quiero** fijar una tolerancia de salida que aplique a todos mis parqueaderos **para** que el guardia autorice la salida sin cobros sorpresa dentro del plazo.

- Criterios: tolerancia de 0–240 minutos por empresa (por defecto 30) vía `PUT /api/empresas/{id}/configuracion`; se cuenta desde `fecha_pago` y se reinicia con cada cobro; dentro de la tolerancia no se cobra extra aunque se cruce la hora; si vence, se bloquea la salida y el cajero cobra solo la diferencia; las tarifas `DIA`, `SEMANA`, `MES` y `MOMENTO` conservan su valor único.
- Reglas: RN-13, RN-17, RN-18. Ver F11.
