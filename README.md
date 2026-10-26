# ParkFlow360

SaaS de gestión de parqueaderos para empresas y universidades.

## ¿Qué es ParkFlow360?

ParkFlow360 es una aplicación web completa para la administración de parqueaderos. Permite controlar el ingreso y salida de vehículos, gestionar espacios de estacionamiento, definir tarifas flexibles, procesar pagos y generar reportes de ocupación e ingresos — todo desde una interfaz moderna y fácil de usar.

Está diseñado para:
- **Empresas** que necesitan administrar estacionamientos para empleados y visitantes
- **Universidades** que gestionan parqueaderos para estudiantes, docentes y personal
- **Operadores de parqueaderos** que requieren control total de sus instalaciones

## Funcionalidades principales

| Módulo | Qué hace |
|--------|----------|
| **Vehículos** | Registro y control de placas, tipos, colores y modelos |
| **Espacios** | Administración de cupos por piso, tipo y estado (libre, ocupado, reservado, mantenimiento) |
| **Tarifas** | Configuración de precios por hora, día, semana, mes o tarifa única |
| **Pagos** | Procesamiento de pagos en efectivo, tarjeta, transferencia o QR |
| **Reportes** | Visión general del sistema, ocupación por estado e ingresos por período |

## Cómo empezar

### Requisitos previos
- Java 17+
- Node.js 18+
- Maven 3.9+
- Docker (opcional, para la base de datos)

### Instalación rápida

```bash
# 1. Clonar el repositorio
git clone https://github.com/JulianSalamanca-code/ParkFlow360.git
cd ParkFlow360

# 2. Levantar la base de datos (PostgreSQL)
docker compose up -d

# 3. Configurar el backend
# Copia backend/.env.example a backend/src/main/resources/application-local.yml
# y ajusta las credenciales de base de datos y JWT

# 4. Iniciar el backend
cd backend
mvn spring-boot:run
# Disponible en http://localhost:8080

# 5. En otra terminal, iniciar el frontend
cd frontend
npm install
ng serve
# Disponible en http://localhost:4200
```

### Credenciales por defecto
```
Email:    admin@parkflow360.com
Password: admin123
```

## Despliegue en producción

El proyecto está preparado para desplegarse como monorepo en plataformas separadas:

| Componente | Plataforma recomendada | Configuración |
|------------|------------------------|---------------|
| **Frontend** | Netlify | `netlify.toml` incluido (build desde `frontend/`) |
| **Backend** | Railway / Render | Root directory `backend/`, variables de entorno en `.env.example` |
| **Base de datos** | Supabase / Neon / Railway | PostgreSQL gestionado |

Ver `backend/.env.example` para las variables de entorno necesarias en producción.

## Flujo de trabajo típico

1. **Administrador** configura espacios, tarifas y usuarios
2. **Operador** registra ingresos de vehículos y asigna espacios
3. **Sistema** calcula automáticamente el tiempo y tarifa aplicable
4. **Usuario/Operador** procesa el pago al salir
5. **Reportes** muestran ocupación en tiempo real e ingresos históricos

## Tecnologías clave

- **Backend**: Spring Boot 3, Spring Security + JWT, Spring Data JPA, Flyway
- **Frontend**: Angular 17+, TypeScript, RxJS, Angular Material
- **Base de datos**: PostgreSQL 15+
- **Contenedores**: Docker Compose para desarrollo local

## Licencia

Proyecto privado y confidencial.

## Estado del despliegue

Última actualización: 2026-10-26 17:27:13
