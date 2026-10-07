# ParkFlow360

SaaS de gestión de parqueaderos para empresas y universidades.

## Descripción

ParkFlow360 es una aplicación web SaaS que permite gestionar parqueaderos de manera eficiente, con módulos para vehículos, espacios, tarifas, pagos y reportes.

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| **Backend** | Java 17 + Spring Boot 3.x |
| **Frontend** | Angular 17+ |
| **Base de datos** | PostgreSQL 15+ |
| **ORM** | Spring Data JPA |
| **Migraciones** | Flyway |
| **Auth** | Spring Security + JWT |
| **Infraestructura** | Docker Compose |

## Estructura del Proyecto

```
ParkFlow360/
├── backend/                          # Spring Boot
│   ├── src/main/java/com/parkflow360/
│   │   ├── config/                   # CORS, Security, etc.
│   │   ├── shared/                   # Exceptions, utils
│   │   ├── modulos/
│   │   │   ├── vehiculos/            # Módulo 1
│   │   │   ├── espacios/             # Módulo 2
│   │   │   ├── tarifas/              # Módulo 3
│   │   │   ├── pagos/                # Módulo 4
│   │   │   └── reportes/             # Módulo 5
│   │   └── security/                 # JWT, roles
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/            # Flyway
│   └── pom.xml
├── frontend/                         # Angular
│   ├── src/app/
│   │   ├── core/                     # Servicios, guards, interceptors
│   │   ├── shared/                   # Componentes compartidos
│   │   ├── features/
│   │   │   ├── vehiculos/            # Módulo 1
│   │   │   ├── espacios/             # Módulo 2
│   │   │   ├── tarifas/              # Módulo 3
│   │   │   ├── pagos/                # Módulo 4
│   │   │   └── reportes/             # Módulo 5
│   │   └── auth/                     # Login
│   └── angular.json
├── docker-compose.yml                # PostgreSQL
└── README.md
```

## Requisitos

- Java 17+
- Node.js 18+
- Maven 3.9+
- Docker (opcional, para PostgreSQL)

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/JulianSalamanca-code/ParkFlow360.git
cd ParkFlow360
```

### 2. Levantar PostgreSQL con Docker (opcional)

```bash
docker compose up -d
```

### 3. Configurar variables de entorno

Crear archivo `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/parkflow360
    username: parkflow360
    password: parkflow360

jwt:
  secret: tu-clave-secreta-muy-segura-y-larga
  expiration: 86400000
```

### 4. Ejecutar el backend

```bash
cd backend
mvn spring-boot:run
```

El backend estará disponible en `http://localhost:8080`

### 5. Ejecutar el frontend

```bash
cd frontend
npm install
ng serve
```

El frontend estará disponible en `http://localhost:4200`

## Módulos

### Vehículos
- CRUD completo de vehículos
- Campos: placa, tipo, color, modelo

### Espacios
- CRUD completo de espacios
- Campos: número, tipo, estado, piso
- Estados: LIBRE, OCUPADO, RESERVADO, MANTENIMIENTO

### Tarifas
- CRUD completo de tarifas
- Campos: nombre, tipo, valor, duración
- Tipos: HORA, DIA, SEMANA, MES, MOMENTO

### Pagos
- CRUD completo de pagos
- Campos: vehículo, espacio, tarifa, valor, método de pago
- Métodos: EFECTIVO, TARJETA, TRANSFERENCIA, QR

### Reportes
- Reporte general del sistema
- Espacios por estado
- Ingresos por período

## Roles y permisos

El sistema tiene dos roles:

| Rol | Puede hacer |
|-----|-------------|
| `ADMIN` | Operación y supervisión: planos, espacios, tarifas, ingresos (entrada/salida), pagos, reportes y usuarios. **No** registra vehículos ni reserva. |
| `USUARIO` (cliente) | Registra y administra **sus** vehículos, reserva espacios, consulta sus parqueos y paga en línea. |

> El administrador no gestiona vehículos ni reservas: esas acciones son del cliente.

## Autenticación

- **Endpoint:** `POST /api/auth/login`
- **Body:**
  ```json
  {
    "email": "admin@parkflow360.com",
    "password": "admin123"
  }
  ```
- **Respuesta:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "email": "admin@parkflow360.com",
    "rol": "ADMIN",
    "nombre": "Administrador"
  }
  ```

### Inicio de sesión con Google

- **Endpoint:** `POST /api/auth/google`
- **Body:** `{ "credential": "<ID token de Google Identity Services>" }`
- El backend verifica el ID token, crea el usuario con rol `USUARIO` si no existe y devuelve el mismo JWT.
- Requiere configurar `GOOGLE_CLIENT_ID` (backend) y `googleClientId` en `frontend/src/environments/environment.ts`.

> La base de datos parte limpia: solo existe el usuario administrador **admin@parkflow360.com / admin123**. El resto de las tablas está vacío.


## API Endpoints

### Vehículos
- `GET /api/vehiculos` - Listar todos
- `GET /api/vehiculos/{id}` - Obtener por ID
- `POST /api/vehiculos` - Crear
- `PUT /api/vehiculos/{id}` - Actualizar
- `DELETE /api/vehiculos/{id}` - Eliminar

### Espacios
- `GET /api/espacios` - Listar todos
- `GET /api/espacios/estado/{estado}` - Listar por estado
- `GET /api/espacios/{id}` - Obtener por ID
- `POST /api/espacios` - Crear
- `PUT /api/espacios/{id}` - Actualizar
- `DELETE /api/espacios/{id}` - Eliminar

### Tarifas
- `GET /api/tarifas` - Listar todos
- `GET /api/tarifas/tipo/{tipo}` - Listar por tipo
- `GET /api/tarifas/{id}` - Obtener por ID
- `POST /api/tarifas` - Crear
- `PUT /api/tarifas/{id}` - Actualizar
- `DELETE /api/tarifas/{id}` - Eliminar

### Pagos
- `GET /api/pagos` - Listar todos
- `GET /api/pagos/vehiculo/{vehiculoId}` - Listar por vehículo
- `GET /api/pagos/espacio/{espacioId}` - Listar por espacio
- `GET /api/pagos/fecha?inicio=&fin=` - Listar por rango de fechas
- `GET /api/pagos/{id}` - Obtener por ID
- `POST /api/pagos` - Crear
- `PUT /api/pagos/{id}` - Actualizar
- `DELETE /api/pagos/{id}` - Eliminar

### Reportes
- `GET /api/reportes/general` - Reporte general
- `GET /api/reportes/espacios-por-estado` - Espacios por estado
- `GET /api/reportes/ingresos-por-periodo?inicio=&fin=` - Ingresos por período

### Vehículos del cliente
- `GET /api/vehiculos/mios` - Listar mis vehículos
- `POST /api/vehiculos/mios` - Registrar un vehículo a mi nombre
- `PUT /api/vehiculos/mios/{id}` - Actualizar mi vehículo
- `DELETE /api/vehiculos/mios/{id}` - Eliminar mi vehículo

### Planos y espacios
- `GET /api/planos` - Listar planos
- `POST /api/planos` - Crear plano
- `PUT /api/planos/{id}` - Actualizar plano
- `DELETE /api/planos/{id}` - Eliminar plano y sus espacios
- `POST /api/planos/{id}/generar-espacios` - Generar los espacios en bloque (cuadrícula)
- `GET /api/planos/{id}/espacios` - Espacios ubicados en el plano (mapa de ocupación)

### Pagos en línea (cliente)
- `POST /api/pagos/online` - Inicia un pago con Wompi y devuelve la URL de checkout
- `GET /api/pagos/mios` - Historial de pagos del cliente
- `POST /api/pagos/wompi/webhook` - Webhook público de Wompi (confirmación de la transacción)

## Plano del parqueadero

En lugar de crear los espacios uno por uno, el administrador carga un plano y genera la cuadrícula de espacios en bloque:

1. Crea un plano (nombre, piso, imagen opcional, filas y columnas).
2. Genera los espacios con `POST /api/planos/{id}/generar-espacios` (prefijo, tipo y estado inicial).
3. Visualiza la ocupación en tiempo real en el **Mapa de ocupación** (colores por estado: libre, ocupado, reservado, mantenimiento).

## Pagos en línea con Wompi

El cliente genera un recibo y paga con Wompi (tarjeta, PSE o Nequi):

1. `POST /api/pagos/online` crea una transacción `PENDIENTE` y devuelve la URL del checkout con la firma de integridad.
2. El cliente paga en Wompi.
3. Wompi notifica al `POST /api/pagos/wompi/webhook`, donde se verifica la firma y el pago pasa a `PAGADO`.

> Configura `WOMPI_PUBLIC_KEY`, `WOMPI_PRIVATE_KEY`, `WOMPI_EVENTS_SECRET` y `WOMPI_INTEGRITY_SECRET`. Usa primero el entorno sandbox de Wompi.

## Despliegue

El proyecto es un **monorepo**: el frontend y el backend se despliegan en plataformas distintas.

```
GitHub (ParkFlow360)
├── frontend/  ──►  Netlify         (sitio estático Angular)
└── backend/   ──►  Railway / Render (Spring Boot)
                          │
                          ▼
                   Supabase (PostgreSQL)
```

### Frontend en Netlify

El archivo `netlify.toml` en la raíz ya contiene la configuración:

```toml
[build]
  base = "frontend"
  command = "npm install && npm run build"
  publish = "dist/frontend/browser"
```

Conectar el repositorio en https://app.netlify.com y Netlify tomará esta configuración automáticamente.

### Backend en Railway / Render

1. Crear un proyecto desde el repositorio de GitHub.
2. Configurar el **Root Directory** en `backend`.
3. Definir las variables de entorno (ver `backend/.env.example`):

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL JDBC de Supabase (`...?prepareThreshold=0`) |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET` | Clave secreta para firmar tokens |
| `CORS_ALLOWED_ORIGINS` | Dominios del frontend separados por coma |
| `APP_FRONTEND_URL` | URL del frontend (redirección de Wompi) |
| `GOOGLE_CLIENT_ID` | Client ID de Google para el login con Google |
| `WOMPI_PUBLIC_KEY` | Llave pública de Wompi |
| `WOMPI_PRIVATE_KEY` | Llave privada de Wompi |
| `WOMPI_EVENTS_SECRET` | Secreto de eventos (webhook) de Wompi |
| `WOMPI_INTEGRITY_SECRET` | Secreto de integridad de Wompi |
| `PORT` | Se asigna automáticamente en la plataforma |

El backend incluye `Dockerfile`, `Procfile` y `system.properties` para facilitar el despliegue.

### Conectar frontend con backend

Una vez desplegado el backend, descomentar en `netlify.toml` y reemplazar la URL:

```toml
[[redirects]]
  from = "/api/*"
  to = "https://TU-BACKEND.up.railway.app/api/:splat"
  status = 200
  force = true
```

## Licencia

Este proyecto es privado y confidencial.
