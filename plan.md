# Implementation Plan: Registro y recuperación de contraseña

**Fecha**: 2026-11-23 | **Alcance**: Frontend Angular y backend Spring Boot

## Summary

Corregir el registro público de usuarios y completar la recuperación de contraseña sin exponer datos ni generar sesiones inseguras. El registro usará el contrato existente `POST /api/auth/registro`. La recuperación se implementará con solicitud de enlace, token temporal de un solo uso y restablecimiento de contraseña.

## Technical Context

- **Frontend**: Angular 22 standalone, Reactive Forms, `HttpClient`, `RouterLink`.
- **Backend**: Java 17, Spring Boot 3.2.5, Bean Validation, Spring Security, BCrypt y PostgreSQL.
- **Autenticación**: JWT Bearer stateless con rol normalizado.
- **Despliegue**: Netlify para frontend y Render para backend; `/api/*` se redirige a Render.
- **Persistencia**: entidad `Usuario` y tabla `usuarios`.
- **Pruebas**: Cypress 16.2 en frontend y Maven/Surefire en backend.

## Constitution Check

- No existe `constitution.md` en el repositorio.
- Se conservarán las convenciones de standalone components, roles vigentes, respuestas globales de error y migraciones Flyway consecutivas.
- No se editarán migraciones existentes.
- Se prohibirá registrar o devolver contraseñas sin cifrar.

## Applied Guidelines

- Angular: importar explícitamente `RouterLink` en componentes que usen `routerLink`.
- Backend: mantener validación con DTOs `@Valid`, BCrypt y `GlobalExceptionHandler`.
- Seguridad: no revelar si un correo existe al solicitar recuperación.
- Datos: agregar una nueva migración Flyway para tokens de recuperación, sin modificar V1-V20.

## Implementation Steps

### Step 1: Diagnosticar y corregir navegación de autenticación
- **Requirements**: REQ-AUTH-001, REQ-AUTH-002
- **Description**: Añadir `RouterLink` al componente de login, definir correctamente los data-testid usados por Cypress y verificar que los enlaces a registro, login y recuperación resuelvan rutas Angular existentes.

### Step 2: Corregir el contrato de registro
- **Requirements**: REQ-AUTH-001
- **Description**: Normalizar email y nombre antes de enviar el registro, conservar la coincidencia de contraseñas, centralizar mensajes de error y confirmar que `POST /api/auth/registro` devuelve el mismo `LoginResponse` esperado por `AuthService`. Proteger contra correos duplicados independientemente de mayúsculas.

### Step 3: Añadir base persistente para recuperación
- **Requirements**: REQ-AUTH-003
- **Description**: Crear una nueva migración `V21` con identificador del usuario, token hash, expiración, fecha de uso y restricciones necesarias. No almacenar el token original en texto plano.

### Step 4: Implementar solicitud de recuperación
- **Requirements**: REQ-AUTH-003
- **Description**: Agregar `POST /api/auth/recuperacion/solicitar` para crear un token temporal y enviar un enlace. La respuesta será igual para correos existentes y inexistentes. El enlace consumirá la URL pública configurada en `APP_FRONTEND_URL`.

### Step 5: Implementar restablecimiento seguro
- **Requirements**: REQ-AUTH-003
- **Description**: Agregar `POST /api/auth/recuperacion/restablecer` con token, nueva contraseña y confirmación. Validar token activo, no usado y no vencido; actualizar la contraseña con BCrypt e invalidar el token. Reutilizar `PasswordEncoder` y `GlobalExceptionHandler`.

### Step 6: Crear pantallas frontend de recuperación
- **Requirements**: REQ-AUTH-002, REQ-AUTH-003
- **Description**: Registrar `/auth/recuperar` y `/auth/recuperar/:token`, crear componentes standalone con formularios Reactivos, estados de carga/error, enlace de retorno al login y navegación posterior al login.

### Step 7: Configurar correo y despliegue
- **Requirements**: REQ-AUTH-004
- **Description**: Seleccionar un proveedor SMTP real, agregar variables de entorno al backend y documentar únicamente nombres seguros de configuración. Configurar CORS y `APP_FRONTEND_URL` en Render; validar el proxy `/api/*` en Netlify.

### Step 8: Verificar flujos end-to-end
- **Requirements**: REQ-AUTH-001, REQ-AUTH-002, REQ-AUTH-003, REQ-AUTH-004
- **Description**: Cubrir registro válido, email duplicado, validaciones, solicitud de recuperación existente/inexistente, token válido/vencido/usado y restablecimiento seguido de login.

## Project Structure

```text
frontend/src/app/auth/
  auth.routes.ts
  components/login/
  components/registro/
  components/recuperar/
  components/restablecer/
  services/auth.service.ts

backend/src/main/java/com/parkflow360/security/
  controller/AuthController.java
  dto/
  entity/Usuario.java
  repository/
  service/AuthService.java
  service/RegistroService.java
  service/RecuperacionPasswordService.java

backend/src/main/resources/db/migration/
  V21__recuperacion_password.sql
```

## Testing Strategy

- **appType**: SPA Angular con API REST y persistencia PostgreSQL.
- **Critical user journeys**:
  1. Registro válido y redirección a `/mis-parqueos`.
  2. Registro con email inválido o duplicado.
  3. Solicitud de recuperación sin revelar existencia del correo.
  4. Cambio de contraseña con token válido.
  5. Rechazo de token vencido, usado o falso.
- **primaryValidationStack**: Maven + Spring Boot + PostgreSQL/Testcontainers + Cypress.
- **fallbackMatrix**:
  - Backend unitario/integración: Maven con H2 si Docker no está disponible.
  - Navegador: Cypress si está disponible; revisión manual como fallback.
- **Environment requirements**: Java 17, Node 22, Maven, PostgreSQL o Docker y proveedor de correo configurable.
- **knownGaps**: Sin proveedor SMTP real no puede verificarse la entrega física de correo.
- **Test data strategy**: usuarios con UUID de prueba y limpieza por email específico.
- **Acceptance criteria**: registro persiste una cuenta USUARIO, recuperación no filtra estados y una contraseña restablecida permite login con nueva contraseña y falla con la anterior.
- **Validation review expectations**: confirmar rutas, endpoints, tokens, migración, variables de entorno y mensajes desde un entorno desplegado.

## Task Breakdown

### Phase 1: Diagnóstico y preparación

- [ ] T001 [Plan:1.1] Actualizar `frontend/src/app/auth/components/login/login.component.ts` para importar `RouterLink` y verificar los enlaces de registro y recuperación.
- [ ] T002 [P] [Plan:1.1,8.1] Corregir los selectores de [frontend/cypress/e2e/registro.cy.ts](frontend/cypress/e2e/registro.cy.ts) y añadir pruebas de navegación.
- [ ] T003 [P] [Plan:7.1] Inventariar variables CORS, URL frontend y correo en [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) y [netlify.toml](netlify.toml).

### Phase 2: Registro público

- [ ] T004 [US1] [Plan:2.1] Normalizar email/nombre y centralizar respuestas en [frontend/src/app/auth/components/registro/registro.component.ts](frontend/src/app/auth/components/registro/registro.component.ts).
- [ ] T005 [P] [US1] [Plan:2.1] Reutilizar y completar la prueba Cypress de registro válido y email duplicado en [frontend/cypress/e2e/registro.cy.ts](frontend/cypress/e2e/registro.cy.ts).
- [ ] T006 [US1] [Plan:2.1] Revisar `RegistroService.registrar` y `RegistroRequest` para normalización y validación sin cambiar el contrato público.
- [ ] T007 [US1] [Plan:2.1] Añadir prueba backend de registro válido y correo duplicado en `backend/src/test/java/com/parkflow360/security/`.

### Phase 3: Persistencia y API de recuperación

- [ ] T008 [US2] [Plan:3.1] Crear `backend/src/main/resources/db/migration/V21__recuperacion_password.sql` sin modificar migraciones anteriores.
- [ ] T009 [US2] [Plan:3.1] Crear entidad y repositorio de token de recuperación bajo `backend/src/main/java/com/parkflow360/security/`.
- [ ] T010 [US2] [Plan:4.1,7.1] Agregar DTOs y endpoints públicos de solicitud/restablecimiento en `AuthController` y `RecuperacionPasswordService`.
- [ ] T011 [US2] [Plan:4.1] Implementar hash del token, expiración, invalidación y respuesta neutra ante correo inexistente.
- [ ] T012 [US2] [Plan:5.1] Integrar actualización de contraseña con `PasswordEncoder` y pruebas de login anterior/nuevo.

### Phase 4: Interfaz de recuperación

- [ ] T013 [P] [US2] [Plan:6.1] Agregar rutas `/auth/recuperar` y `/auth/recuperar/:token` en [frontend/src/app/auth/auth.routes.ts](frontend/src/app/auth/auth.routes.ts).
- [ ] T014 [US2] [Plan:6.1] Crear componentes standalone de solicitud y restablecimiento bajo `frontend/src/app/auth/components/`.
- [ ] T015 [US2] [Plan:6.1] Extender [frontend/src/app/auth/services/auth.service.ts](frontend/src/app/auth/services/auth.service.ts) con los dos endpoints de recuperación.

### Phase 5: Correo y despliegue

- [ ] T016 [US2] [Plan:7.1] Integrar el proveedor SMTP elegido y documentar variables en `backend/.env.example`.
- [ ] T017 [US2] [Plan:7.1] Configurar `APP_FRONTEND_URL`, CORS y correo en Render sin incluir secretos en el código.
- [ ] T018 [P] [US2] [Plan:7.1] Verificar que Netlify conserva `/api/*` y el fallback SPA.

### Phase 6: Validación final

- [ ] T019 [P] [US1,US2] [Plan:8.1] Ejecutar build frontend, Cypress enfocado y pruebas backend.
- [ ] T020 [US1,US2] [Plan:8.1] Ejecutar una prueba manual desplegada de registro y recuperación y registrar el resultado.

## Requirement Mapping

| REQ ID | Description | Plan Items | Implementation Evidence |
|--------|-------------|------------|-------------------------|
| REQ-AUTH-001 | El visitante puede crear una cuenta pública USUARIO | 1.1, 2.1, 8.1 | registro.component.ts, RegistroService.java, registro.cy.ts |
| REQ-AUTH-002 | Registro y recuperación son navegables desde login | 1.1, 6.1, 8.1 | login.component.ts, auth.routes.ts |
| REQ-AUTH-003 | La contraseña puede restablecerse con token temporal seguro | 3.1, 4.1, 5.1, 6.1, 8.1 | V21 SQL, RecuperacionPasswordService.java, recuperación frontend |
| REQ-AUTH-004 | Producción entrega correo y mantiene rutas API/SPA | 7.1, 8.1 | application.yml, netlify.toml, variables de despliegue |

## Complexity Tracking

| Violación | Justificación | Alternativa rechazada |
|-----------|---------------|-----------------------|
| No existe especificación formal con REQ-XXX | El repositorio conserva historias históricas y código fuente como fuente actual | Inventar requisitos sin evidencia sería más riesgoso |
| Falta proveedor de correo confirmado | No puede elegirse responsablemente sin un proveedor y credenciales válidos | Mostrar enlaces ficticios o guardar contraseñas sin envío sería inseguro |
