# ParkFlow360 - Manual de Contexto del Proyecto (MCP)

## Estado del Proyecto
- **Nombre:** ParkFlow360
- **Tipo:** SaaS de gestión de parqueaderos para empresas y universidades
- **Repositorio:** JulianSalamanca-code/ParkFlow360
- **Rama actual:** juliansalamanca-code-diagramas-cr-adr-proyecto
- **Estado:** En desarrollo activo
- **Monorepo:** Sí - Frontend + Backend

## Tecnologías Usadas

### Backend
- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Seguridad:** Spring Security + JWT
- **ORM:** Spring Data JPA
- **Migraciones:** Flyway
- **Base de datos:** PostgreSQL 15+
- **Infraestructura:** Docker Compose

### Frontend
- **Framework:** Angular 17+
- **Despliegue:** Netlify (estático)

### Infraestructura / DevOps
- **Contenedores:** Docker
- **Backend hosting:** Railway / Render
- **Base de datos cloud:** Supabase PostgreSQL
- **CI/CD:** Netlify para frontend

## Arquitectura General
Monorepo con separación clara:
- `backend/` → API REST Spring Boot
- `frontend/` → SPA Angular
- `docker-compose.yml` → PostgreSQL local

Comunicación: Frontend → API REST → PostgreSQL

## Módulos del Sistema

1. **Vehículos** - CRUD de vehículos (placa, tipo, color, modelo)
2. **Espacios** - Gestión de espacios (número, tipo, estado, piso)
3. **Tarifas** - Tarifas por hora/día/semana/mes
4. **Pagos** - Registro de pagos con métodos EFECTIVO/TARJETA/TRANSFERENCIA/QR
5. **Reportes** - Reportes generales, espacios por estado, ingresos por período
6. **Seguridad** - Autenticación JWT, roles ADMIN/USER

## Cómo se usa

### Flujo de usuario
1. Login vía `POST /api/auth/login` → recibe JWT
2. Navegación por módulos según rol
3. CRUD de entidades con validaciones
4. Generación de reportes

### Despliegue
- Frontend → Netlify con `netlify.toml`
- Backend → Railway/Render con variables de entorno
- DB → Supabase

### Requisitos locales
- Java 17+, Node.js 18+, Maven 3.9+, Docker opcional

## Endpoints principales
- `/api/vehiculos`
- `/api/espacios`
- `/api/tarifas`
- `/api/pagos`
- `/api/reportes`
- `/api/auth/login`

## Notas
Proyecto privado y confidencial. Documentación generada automáticamente para visualización.
