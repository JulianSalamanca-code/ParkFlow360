HISTORIAL DE USUARIO — ParkFlow360

## 1. Estructura base

```
Como [rol/usuario]
Quiero [acción o funcionalidad]
Para [beneficio/valor que obtengo]
```

## 2. Plantilla completa

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-001 · Iniciar sesión |
| **Épica** | Autenticación de usuarios |
| **Historia** | Como usuario, quiero iniciar sesión con email/contraseña o Google, para acceder al sistema de gestión de parqueaderos |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Alta |
| **Estimación** | 5 SP |
| **Dependencias** | HU-002 (registro de usuarios) |
| **Notas técnicas / UI** | Login con email/contraseña, botón de Google OAuth, redirección a /mis-parqueos |
| **Definition of Done** | JWT generado, usuario autenticado, redirección exitosa |

## 3. Criterios de aceptación (Given / When / Then)

```
Escenario 1: Inicio de sesión exitoso con email/contraseña
Dado que el usuario tiene credenciales válidas
Cuando ingresa email y contraseña válidas
Entonces el sistema genera un JWT y redirige a /mis-parqueos

Escenario 2: Inicio de sesión con Google OAuth
Dado que el usuario hace clic en el botón "Iniciar sesión con Google"
Cuando Google autentica al usuario
Entonces el sistema crea o actualiza el usuario con googleId y redirige a /mis-parqueos

Escenario 3: Fallo de inicio de sesión
Dado que el usuario ingresa credenciales inválidas
Cuando intenta iniciar sesión
Entonces el sistema muestra "Credenciales inválidas" y permanece en la página de login
```

## 4. Checklist de calidad (INVEST)

- **I**ndependiente: Se puede desarrollar sin depender de otras historias (excepto HU-002 para registro)
- **N**egociable: La historia puede ser refinada antes de comprometerse
- **V**aliosa: Proporciona acceso seguro al sistema
- **E**stimable: El equipo puede dimensionarla en story points
- **S**mall (pequeña): Cabe en un sprint
- **T**estable: Tiene criterios verificables

> Si una historia pasa de 8 puntos, divídela (por flujo, por rol o por operación CRUD).

---

## HU-002 · Registrar nuevo usuario

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-002 · Registrar nuevo usuario |
| **Épica** | Autenticación de usuarios |
| **Historia** | Como visitante, quiero registrarme con email/nombre/contraseña, para crear una cuenta de usuario |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Alta |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Formulario de registro con validación, redirección a /mis-parqueos |
| **Definition of Done** | Usuario creado con rol USUARIO, redirección exitosa |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Registro exitoso
Dado que el visitante completa el formulario de registro con datos válidos
Cuando hace clic en "Crear cuenta"
Entonces el sistema crea el usuario con rol USUARIO y redirige a /mis-parqueos

Escenario 2: Validación de email
Dado que el visitante ingresa un email inválido
Cuando intenta registrar
Entonces el sistema muestra "Email inválido" y permanece en el formulario

Escenario 3: Contraseñas no coincidentes
Dado que el visitante ingresa contraseñas diferentes
Cuando intenta registrar
Entonces el sistema muestra "Las contraseñas deben coincidir" y permanece en el formulario
```

---

## HU-003 · Gestionar vehículos

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-003 · Gestionar vehículos |
| **Épica** | Gestión de vehículos |
| **Historia** | Como usuario, quiero registrar y gestionar mis vehículos, para poder estacionarlos en el parqueadero |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Alta |
| **Estimación** | 13 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Formulario de vehículo con validación de placa (6 caracteres), selección de tipo (VEHICULO/MOTO/VEHICULO_ELECTRICO) |
| **Definition of Done** | Vehículo creado con placa válida y tipo permitido, mostrado en lista |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Registro de vehículo exitoso
Dado que el usuario está autenticado
Cuando registra un vehículo con placa válida (AAA-000) y tipo VEHICULO
Entonces el sistema crea el vehículo y lo muestra en la lista

Escenario 2: Validación de placa
Dado que el usuario intenta registrar un vehículo con placa inválida (AAA-0000)
Cuando intenta registrar
Entonces el sistema muestra "La placa debe tener 6 caracteres (AAA-000)" y permanece en el formulario

Escenario 3: Validación de tipo de vehículo
Dado que el usuario intenta registrar un vehículo con tipo inválido (CAMION)
Cuando intenta registrar
Entonces el sistema muestra "El tipo debe ser VEHICULO, MOTO o VEHICULO_ELECTRICO" y permanece en el formulario
```

---

## HU-004 · Reservar espacio

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-004 · Reservar espacio |
| **Épica** | Gestión de reservas |
| **Historia** | Como usuario, quiero reservar un espacio de parqueadero, para asegurar un lugar antes de llegar |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Alta |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión), HU-003 (gestionar vehículos) |
| **Notas técnicas / UI** | Calendario de selección de fecha/hora, validación de disponibilidad |
| **Definition of Done** | Reserva creada con código de confirmación, mostrada en lista de reservas |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Reserva exitosa
Dado que el usuario tiene vehículos registrados
Cuando selecciona fecha y hora y confirma la reserva
Entonces el sistema crea la reserva y muestra un código de confirmación

Escenario 2: Sin disponibilidad
Dado que no hay espacios disponibles en el horario seleccionado
Cuando el usuario intenta reservar
Entonces el sistema muestra "Sin disponibilidad" y sugiere otro horario
```

---

## HU-005 · Gestionar ingresos

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-005 · Gestionar ingresos |
| **Épica** | Gestión de ingresos |
| **Historia** | Como administrador, quiero registrar ingresos de entrada y salida, para controlar las finanzas del parqueadero |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Media |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Formulario de ingreso con selección de vehículo, cálculo automático de tarifa |
| **Definition of Done** | Ingreso creado con monto calculado, mostrado en lista de ingresos |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Registro de ingreso exitoso
Dado que el administrador está autenticado
Cuando registra un ingreso para un vehículo
Entonces el sistema calcula el monto y crea el ingreso

Escenario 2: Cálculo de tarifa
Dado que el vehículo tiene tarifa configurada
Cuando se registra un ingreso
Entonces el sistema calcula el monto según la tarifa por hora/día
```

---

## HU-006 · Gestionar tarifas

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-006 · Gestionar tarifas |
| **Épica** | Gestión de tarifas |
| **Historia** | Como administrador, quiero configurar tarifas para diferentes tipos de vehículos y horarios, para establecer precios de estacionamiento |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Media |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Formulario de tarifa con tipo de vehículo, precio por hora/día, días de semana/fin de semana |
| **Definition of Done** | Tarifa creada o actualizada, mostrada en lista de tarifas |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Creación de tarifa exitosa
Dado que el administrador está autenticado
Cuando crea una tarifa para VEHICULO con precio de $5 por hora
Entonces el sistema crea la tarifa y la muestra en la lista

Escenario 2: Actualización de tarifa
Dado que existe una tarifa para VEHICULO
Cuando el administrador actualiza el precio a $6 por hora
Entonces el sistema actualiza la tarifa y muestra el nuevo precio
```

---

## HU-007 · Gestionar espacios

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-007 · Gestionar espacios |
| **Épica** | Gestión de espacios |
| **Historia** | Como administrador, quiero gestionar espacios de parqueadero, para controlar la ocupación y disponibilidad |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Media |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de espacios con estado (LIBRE/OCUPADO/RESERVADO/MANTENIMIENTO), botón de cambio de estado |
| **Definition of Done** | Estado de espacio actualizado, mostrado en lista con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Cambio de estado a ocupado
Dado que existe un espacio con estado LIBRE
Cuando el administrador cambia su estado a OCUPADO
Entonces el sistema actualiza el estado y muestra el espacio como ocupado

Escenario 2: Visualización de estado
Dado que existen múltiples espacios
Cuando el administrador visita la lista de espacios
Entonces el sistema muestra cada espacio con su estado actual (LIBRE/OCUPADO/RESERVADO/MANTENIMIENTO)
```

---

## HU-008 · Generar reportes

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-008 · Generar reportes |
| **Épica** | Gestión de reportes |
| **Historia** | Como administrador, quiero generar reportes de ocupación e ingresos, para tomar decisiones de negocio |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Filtros de fecha, botón de generar reporte, tabla de resultados exportable |
| **Definition of Done** | Reporte generado con datos filtrados, mostrado en tabla con totales |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Generación de reporte de ocupación
Dado que el administrador está autenticado
Cuando selecciona rango de fechas y hace clic en "Generar reporte"
Entonces el sistema muestra ocupación por día con totales

Escenario 2: Exportación de reporte
Dado que se ha generado un reporte
Cuando el administrador hace clic en "Exportar a CSV"
Entonces el sistema descarga un archivo CSV con los datos del reporte
```

---

## HU-009 · Gestionar usuarios

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-009 · Gestionar usuarios |
| **Épica** | Gestión de usuarios |
| **Historia** | Como administrador, quiero gestionar usuarios del sistema, para controlar el acceso |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de usuarios con roles, botón de cambio de rol, filtro de búsqueda |
| **Definition of Done** | Usuario actualizado con nuevo rol, mostrado en lista con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Cambio de rol a admin
Dado que existe un usuario con rol USUARIO
Cuando el administrador cambia su rol a ADMIN
Entonces el sistema actualiza el rol y muestra el usuario como admin

Escenario 2: Búsqueda de usuario
Dado que existen múltiples usuarios
Cuando el administrador busca por email
Entonces el sistema muestra solo los usuarios que coinciden con la búsqueda
```

---

## HU-010 · Gestionar pagos

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-010 · Gestionar pagos |
| **Épica** | Gestión de pagos |
| **Historia** | Como administrador, quiero gestionar pagos de usuarios, para controlar el recaudo |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de pagos con estado, botón de cambio de estado, filtro de fecha |
| **Definition of Done** | Pago actualizado con nuevo estado, mostrado en lista con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Cambio de estado a pagado
Dado que existe un pago con estado PENDIENTE
Cuando el administrador cambia su estado a PAGADO
Entonces el sistema actualiza el estado y muestra el pago como pagado

Escenario 2: Filtro de fecha
Dado que existen múltiples pagos
Cuando el administrador selecciona rango de fechas
Entonces el sistema muestra solo los pagos dentro del rango seleccionado
```

---

## HU-011 · Gestionar reservas

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-011 · Gestionar reservas |
| **Épica** | Gestión de reservas |
| **Historia** | Como administrador, quiero gestionar reservas de usuarios, para controlar la ocupación del parqueadero |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de reservas con estado, botón de cancelación, filtro de fecha |
| **Definition of Done** | Reserva cancelada con motivo, mostrada en lista con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Cancelación de reserva
Dado que existe una reserva activa
Cuando el administrador cancela la reserva con motivo "Cambio de planes"
Entonces el sistema cancela la reserva y muestra el motivo

Escenario 2: Filtro de estado
Dado que existen múltiples reservas
Cuando el administrador filtra por estado ACTIVO
Entonces el sistema muestra solo las reservas con estado ACTIVO
```

---

## HU-012 · Gestionar vehículos (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-012 · Gestionar vehículos (admin) |
| **Épica** | Gestión de vehículos |
| **Historia** | Como administrador, quiero gestionar todos los vehículos del sistema, para controlar la flota completa |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todos los vehículos con propietario, botón de eliminación, filtro de búsqueda |
| **Definition of Done** | Vehículo eliminado del sistema, mostrado en lista sin el vehículo eliminado |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Eliminación de vehículo
Dado que existe un vehículo en el sistema
Cuando el administrador elimina el vehículo
Entonces el sistema elimina el vehículo y muestra la lista actualizada

Escenario 2: Búsqueda de vehículo
Dado que existen múltiples vehículos
Cuando el administrador busca por placa
Entonces el sistema muestra solo los vehículos que coinciden con la placa
```

---

## HU-013 · Gestionar espacios (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-013 · Gestionar espacios (admin) |
| **Épica** | Gestión de espacios |
| **Historia** | Como administrador, quiero gestionar todos los espacios del sistema, para controlar la ocupación completa |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Mapa de todos los espacios con estado, botón de cambio masivo, filtro de ubicación |
| **Definition of Done** | Estado de espacio actualizado masivamente, mostrado en mapa con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Cambio masivo a mantenimiento
Dado que existen múltiples espacios con estado LIBRE
Cuando el administrador cambia masivamente su estado a MANTENIMIENTO
Entonces el sistema actualiza todos los espacios y muestra el cambio

Escenario 2: Filtro de ubicación
Dado que existen múltiples espacios
Cuando el administrador filtra por ubicación "Piso 1"
Entonces el sistema muestra solo los espacios en el Piso 1
```

---

## HU-014 · Gestionar tarifas (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-014 · Gestionar tarifas (admin) |
| **Épica** | Gestión de tarifas |
| **Historia** | Como administrador, quiero gestionar todas las tarifas del sistema, para controlar la política de precios completa |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todas las tarifas con tipo de vehículo, botón de edición, filtro de tipo |
| **Definition of Done** | Tarifa actualizada con nuevo precio, mostrada en lista con indicador visual |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Actualización de tarifa masiva
Dado que existen múltiples tarifas para VEHICULO
Cuando el administrador actualiza masivamente el precio a $7 por hora
Entonces el sistema actualiza todas las tarifas y muestra el nuevo precio

Escenario 2: Filtro de tipo
Dado que existen múltiples tarifas
Cuando el administrador filtra por tipo "MOTO"
Entonces el sistema muestra solo las tarifas para MOTO
```

---

## HU-015 · Gestionar ingresos (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-015 · Gestionar ingresos (admin) |
| **Épica** | Gestión de ingresos |
| **Historia** | Como administrador, quiero gestionar todos los ingresos del sistema, para controlar las finanzas completas |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todos los ingresos con totales, botón de eliminación, filtro de fecha |
| **Definition of Done** | Ingreso eliminado del sistema, mostrado en lista sin el ingreso eliminado |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Eliminación de ingreso
Dado que existe un ingreso en el sistema
Cuando el administrador elimina el ingreso
Entonces el sistema elimina el ingreso y muestra la lista actualizada

Escenario 2: Filtro de fecha
Dado que existen múltiples ingresos
Cuando el administrador selecciona rango de fechas
Entonces el sistema muestra solo los ingresos dentro del rango seleccionado
```

---

## HU-016 · Gestionar pagos (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-016 · Gestionar pagos (admin) |
| **Épica** | Gestión de pagos |
| **Historia** | Como administrador, quiero gestionar todos los pagos del sistema, para controlar el recaudo completo |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todos los pagos con monto, botón de eliminación, filtro de estado |
| **Definition of Done** | Pago eliminado del sistema, mostrado en lista sin el pago eliminado |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Eliminación de pago
Dado que existe un pago en el sistema
Cuando el administrador elimina el pago
Entonces el sistema elimina el pago y muestra la lista actualizada

Escenario 2: Filtro de estado
Dado que existen múltiples pagos
Cuando el administrador filtra por estado PAGADO
Entonces el sistema muestra solo los pagos con estado PAGADO
```

---

## HU-017 · Gestionar reportes (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-017 · Gestionar reportes (admin) |
| **Épica** | Gestión de reportes |
| **Historia** | Como administrador, quiero gestionar todos los reportes del sistema, para controlar la información completa |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 5 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todos los reportes con fecha, botón de eliminación, filtro de tipo |
| **Definition of Done** | Reporte eliminado del sistema, mostrado en lista sin el reporte eliminado |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Eliminación de reporte
Dado que existe un reporte en el sistema
Cuando el administrador elimina el reporte
Entonces el sistema elimina el reporte y muestra la lista actualizada

Escenario 2: Filtro de tipo
Dado que existen múltiples reportes
Cuando el administrador filtra por tipo "ocupación"
Entonces el sistema muestra solo los reportes de tipo ocupación
```

---

## HU-018 · Gestionar usuarios (admin)

| Campo | Contenido |
| --- | --- |
| **ID / Título** | HU-018 · Gestionar usuarios (admin) |
| **Épica** | Gestión de usuarios |
| **Historia** | Como administrador, quiero gestionar todos los usuarios del sistema, para controlar el acceso completo |
| **Criterios de aceptación** | Lista en formato Given/When/Then (ver abajo) |
| **Prioridad** | Baja |
| **Estimación** | 8 SP |
| **Dependencias** | HU-001 (inicio de sesión) |
| **Notas técnicas / UI** | Lista de todos los usuarios con email, botón de eliminación, filtro de rol |
| **Definition of Done** | Usuario eliminado del sistema, mostrado en lista sin el usuario eliminado |

### Criterios de aceptación (Given / When / Then)

```
Escenario 1: Eliminación de usuario
Dado que existe un usuario en el sistema
Cuando el administrador elimina el usuario
Entonces el sistema elimina el usuario y muestra la lista actualizada

Escenario 2: Filtro de rol
Dado que existen múltiples usuarios
Cuando el administrador filtra por rol "ADMIN"
Entonces el sistema muestra solo los usuarios con rol ADMIN
```

---

## 3. Resumen del backlog

| ID | Título | SP | Prioridad | Estado |
|----|-------|----|----------|--------|
| HU-001 | Iniciar sesión | 5 | Alta | ✅ Completado |
| HU-002 | Registrar nuevo usuario | 8 | Alta | ✅ Completado |
| HU-003 | Gestionar vehículos | 13 | Alta | ✅ Completado |
| HU-004 | Reservar espacio | 5 | Alta | ✅ Completado |
| HU-005 | Gestionar ingresos | 8 | Media | ✅ Completado |
| HU-006 | Gestionar tarifas | 5 | Media | ✅ Completado |
| HU-007 | Gestionar espacios | 8 | Media | ✅ Completado |
| HU-008 | Generar reportes | 5 | Baja | ✅ Completado |
| HU-009 | Gestionar usuarios | 8 | Baja | ✅ Completado |
| HU-010 | Gestionar pagos | 5 | Baja | ✅ Completado |
| HU-011 | Gestionar reservas | 5 | Baja | ✅ Completado |
| HU-012 | Gestionar vehículos (admin) | 8 | Baja | ✅ Completado |
| HU-013 | Gestionar espacios (admin) | 5 | Baja | ✅ Completado |
| HU-014 | Gestionar tarifas (admin) | 5 | Baja | ✅ Completado |
| HU-015 | Gestionar ingresos (admin) | 8 | Baja | ✅ Completado |
| HU-016 | Gestionar pagos (admin) | 5 | Baja | ✅ Completado |
| HU-017 | Gestionar reportes (admin) | 5 | Baja | ✅ Completado |
| HU-018 | Gestionar usuarios (admin) | 8 | Baja | ✅ Completado |

**Total: 95 SP**

**Must: 45 SP (HU-001 a HU-004)** ✅
**Should: 31 SP (HU-005 a HU-007)** ✅
**Could: 19 SP (HU-008 a HU-018)** ✅

## 4. Próximos pasos

1. **Validar el historial de usuario** con el equipo y el administrador
2. **Crear el tablero de GitHub Projects** con las historias
3. **Planificar el primer sprint** según la velocidad real del equipo
4. **Implementar las historias restantes** (HU-008 a HU-018) en la rama `feat/registro-placa-tipos-vehiculo`
5. **Ejecutar pruebas E2E** para validar todas las historias
6. **Integrar a main** tras validación completa

## 5. Notas técnicas

- **Validación de placa**: Regex `^[A-Z]{3}-[A-Z0-9]{3}$` (6 caracteres con guion)
- **Tipos de vehículo**: `VEHICULO`, `MOTO`, `VEHICULO_ELECTRICO`
- **Roles de usuario**: `ADMIN`, `USUARIO`
- **Autenticación**: JWT con Google OAuth y email/contraseña
- **Despliegue**: Netlify (frontend), Render (backend), Supabase (PostgreSQL)
- **Base de datos**: PostgreSQL 15 con Flyway migrations
- **Frontend**: Angular 17 con componentes standalone
- **Backend**: Java 17 + Spring Boot 3.2.5

## 6. Checklist de calidad (INVEST)

- **I**ndependiente: Cada historia puede ser desarrollada de forma independiente
- **N**egociable: Las historias pueden ser refinadas antes de comprometerse
- **V**aliosa: Proporciona valor real al usuario/administrador
- **E**stimable: El equipo puede dimensionar en story points
- **S**mall (pequeña): Cabe en un sprint
- **T**estable: Tiene criterios verificables

> Si una historia pasa de 8 puntos, divídela (por flujo, por rol o por operación CRUD).

---

*Documentado el 27 de octubre de 2026*
*Equipo: Ruben Viasus y Julian Salamanca*
*Curso: Gerencia de Software — Universidad Santo Tomás*