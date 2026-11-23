# AGENTS.md — Instrucciones para agentes en ParkFlow360

## 1. Reglas generales

- Trabaja y responde en **español**.
- El **código es la fuente de verdad**. La documentación de `docs/`, los README y los archivos `*.md` de la raíz pueden estar desactualizados (hay referencias a Angular 17, migraciones V1–V8 y endpoints que ya no existen). Verifica en el código antes de afirmar algo.
- No inventes rutas, endpoints, versiones ni convenciones: confírmalos en los archivos correspondientes.
- No edites migraciones Flyway ya existentes; agrega una nueva con el siguiente número consecutivo.
- No subas secretos ni artefactos generados (`target/`, `dist/`, `node_modules/`, `.angular/`).
- Antes de terminar, comprueba que los cambios siguen las convenciones de este documento y que el proyecto compila.

## 2. Stack y versiones

| Capa | Tecnología |
|------|------------|
| Backend | Java 17, Spring Boot 3.2.5, Maven (sin wrapper), Lombok, Bean Validation |
| Seguridad | Spring Security (stateless), JJWT 0.12.5, Google OAuth2 con flujo manual |
| Datos | PostgreSQL 15, Spring Data JPA, Flyway (`V1`–`V16` existentes) |
| Frontend | Angular 22 (standalone, sin NgModules), TypeScript ~6.0.3, RxJS 7.8, zone.js 0.16 |
| Pruebas | Cypress 16.2 (E2E); Testcontainers declarado en el backend |
| Despliegue | Node 22 en Netlify (usa pnpm), Render (backend), Supabase (DB) |

Las funcionalidades y migraciones previstas (V17 en adelante) están en `docs/ESPECIFICACIONES_PARKFLOW360.md`. No asumas que ya existen en el código: verifica siempre.

## 3. Estructura del repositorio

```
backend/
  pom.xml, Dockerfile, Procfile, .env.example, system.properties
  src/main/java/com/parkflow360/
    ParkFlow360Application.java
    config/             SecurityConfig, CorsConfig
    security/           JwtService, JwtAuthenticationFilter,
                        controller/, dto/, entity/, repository/, service/
    modulos/            espacios, ingresos, pagos, reportes, tarifas, vehiculos
                        → controller/, dto/, entity/, repository/, service/
    shared/exception/   GlobalExceptionHandler
  src/main/resources/
    application.yml
    db/migration/Vn__descripcion_snake_case.sql
frontend/
  package.json, angular.json, proxy.conf.json, cypress.config.ts
  package-lock.json + pnpm-lock.yaml  (Netlify usa pnpm)
  src/app/
    app.component.*, app.config.ts, app.routes.ts
    auth/               auth.routes.ts, components/, services/, models/
    core/               guards/, interceptors/
    features/           <dominio>/{*.routes.ts, components/, services/, models/}
                        dominios: espacios, ingresos, pagos, reportes, tarifas,
                        vehiculos, usuarios, cliente/
    shared/             components/, services/
  cypress/e2e/, cypress/support/
docs/                   documentación histórica del proyecto (ver regla 1)
docker-compose.yml      solo servicio postgres:15
netlify.toml            build y rewrite de /api/*
```

## 4. Comandos

**Backend** (`cd backend`, requiere Java 17 y `mvn` en el PATH):

```bash
mvn spring-boot:run             # arranca en :8080 (variable PORT)
mvn test                        # Surefire: **/*Test.java
mvn verify                      # Failsafe: **/*IntegrationTest.java
mvn clean package -DskipTests   # build usado por el Dockerfile
```

**Frontend** (`cd frontend`, Netlify usa `pnpm-lock.yaml`):

```bash
pnpm install
pnpm start                      # ng serve en :4200, /api → http://localhost:8080
pnpm build                      # genera dist/frontend/browser
pnpm e2e                        # Cypress contra http://localhost:4200
pnpm e2e:open
```

**Base de datos local**:

```bash
docker compose up -d            # postgres:15, DB parkflow360, puerto 5432
```

Puertos: frontend 4200, backend 8080, PostgreSQL 5432.

## 5. Convenciones del backend

- Paquete base `com.parkflow360`. Cada dominio va en `modulos/<dominio>/` con `controller/`, `dto/`, `entity/`, `repository/`, `service/`. El módulo `reportes` no tiene entidad ni repositorio propios.
- **Entidades**: `@Entity`, `@Table(name = "snake_case")`, `@Id` + `GenerationType.IDENTITY`, Lombok (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`), columnas con `@Column(name = "...")` y `@PrePersist` para fechas.
- **DTOs**: sufijos `Request` y `Response`. Validación Bean Validation con mensajes en español y `@Valid @RequestBody` en los controllers.
- **Repositorios**: interfaces `JpaRepository` con `@Repository` y derived query methods.
- **Servicios**: `@Service` + `@RequiredArgsConstructor` (inyección por constructor), `@Transactional` (`readOnly = true` en consultas) y mapeo manual a `Response` mediante `mapToResponse` (no se usa MapStruct).
- **Controllers**: `@RestController`, `@RequestMapping("/api/...")`, respuestas `ResponseEntity`; `201` al crear y `204` al eliminar.
- **Excepciones**: usa `NoSuchElementException` (404) o `IllegalArgumentException` (400). No crees otro mecanismo: `GlobalExceptionHandler` ya devuelve `{timestamp, status, error, message, path}`.
- **Migraciones**: `src/main/resources/db/migration/Vn__descripcion_snake_case.sql`. La última existente es `V16`; nunca reescribas una migración aplicada, agrega la siguiente.
- **Seguridad y configuración**: JWT Bearer stateless con claim `rol`; reglas centralizadas en `SecurityConfig`; CORS desde `app.cors.allowed-origins`. Toda configuración sensible se lee desde variables de entorno en `application.yml` (los valores por defecto sirven solo para desarrollo local). Roles **actuales** en código: `ADMIN` y `USUARIO`. Roles del modelo objetivo (pendientes de implementar): `PLATAFORMA` (renombra `ADMIN`), `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA` y `USUARIO`, con filtrado de datos por parqueadero.

## 6. Convenciones del frontend

- Componentes **standalone** con `template` y `styles` inline y `changeDetection: ChangeDetectionStrategy.Eager`. `app.component` es la excepción (usa archivos externos). No se usan NgModules.
- Rutas en `app.routes.ts` y `<dominio>.routes.ts` con `loadChildren`/`loadComponent`. El control de acceso se hace con `RoleGuard` y `data: { roles: [...] }`.
- Servicios `@Injectable({ providedIn: 'root' })` con `HttpClient` y URLs relativas `/api/...` (el proxy local o el rewrite de Netlify resuelven el destino). Modelos como interfaces en `*.model.ts`.
- Autenticación: `AuthService` guarda token y usuario en `localStorage`; `authInterceptor` agrega el header `Authorization: Bearer`. `RoleGuard` es el guard efectivamente usado en las rutas.
- Nombres: `features/<dominio>/{components/, services/, models/, <dominio>.routes.ts}`.
- Usa `data-testid` en los elementos interactuados por Cypress y reutiliza los comandos custom de `cypress/support/commands.ts`.
- TypeScript estricto (`strict`, `strictTemplates`): evita `any` e imports sin usar en código nuevo.

## 7. Reglas de negocio confirmadas (a implementar)

Detalle completo, criterios de aceptación y tabla de alcance en `docs/ESPECIFICACIONES_PARKFLOW360.md`. **Hoy no están implementadas**: no asumas que existen tablas, endpoints ni campos nuevos.

- **Saldo y cobro automático**: el usuario recarga saldo; al generarse una factura (al salir o al expirar una reserva), si el saldo alcanza se debita y se crea un `Pago` confirmado; si no alcanza, la factura queda pendiente.
- **Crédito**: cubre solo el faltante, máximo uno pendiente por usuario, sin intereses, con vencimiento a 3 días desde que se otorga. La deuda vencida bloquea únicamente nuevas reservas (403 en `POST /api/ingresos/reservar`).
- **Hora de espera**: la reserva tiene un límite de 1 hora para la llegada. El guardia registra la llegada; si es tardía se factura 1 hora y la estancia continúa. Si expira sin llegada, se cancela, se libera el espacio y se factura 1 hora solo cuando la tarifa es `HORA`. El cobro es idempotente por ingreso.
- **Roles objetivo**: `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA`, `USUARIO`. El cajero se crea con cada parqueadero y solo gestiona pagos de ese parqueadero; el guardia verifica datos, marca llegadas y consulta estado de pago; el usuario puede vincularse a varios parqueaderos por identificador y solo ve los suyos.
- **Datos**: los parqueaderos tienen identificador único, capacidad y tipos de vehículo con cupo fijo o ilimitado; el `numero` de espacio es único por parqueadero; el filtrado se hace en backend desde la vinculación.

---

Pendiente para una siguiente entrega: pruebas, despliegue, flujo de trabajo y deuda técnica conocida.
