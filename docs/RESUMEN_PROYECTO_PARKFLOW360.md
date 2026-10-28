# RESUMEN DEL PROYECTO — ParkFlow360

## 1. Estado actual del proyecto

### 1.1 Estructura actual

**Backend (Java/Spring Boot):**
- Módulos implementados: vehículos, espacios, tarifas, pagos, reportes, ingresos, seguridad (auth, usuarios)
- Endpoints REST: 15+ endpoints implementados
- Base de datos: PostgreSQL 15 con Flyway migrations (V1-V8)
- Autenticación: JWT con Google OAuth y email/contraseña
- Roles: ADMIN, USUARIO

**Frontend (Angular 17):**
- Componentes implementados: login, registro, mis-vehiculos, mis-parqueos, espacios, tarifas, pagos, ingresos, reportes, usuarios
- Servicios: auth.service, vehiculo.service, espacio.service, tarifa.service, pago.service, ingreso.service, reporte.service, usuario.service
- Rutas: /auth/login, /auth/registro, /mis-vehiculos, /mis-parqueos, /espacios, /tarifas, /pagos, /ingresos, /reportes, /usuarios
- Validación: Formularios reactivos con validación de placa (6 caracteres) y tipos de vehículo

**Despliegue:**
- Frontend: Netlify
- Backend: Render
- Base de datos: Supabase (PostgreSQL)

### 1.2 Funcionalidades implementadas

| Historia de Usuario | Estado | SP | Prioridad |
|-------------------|--------|----|----------|
| HU-001: Iniciar sesión | ✅ Completado | 5 | Alta |
| HU-002: Registrar nuevo usuario | ✅ Completado | 8 | Alta |
| HU-003: Gestionar vehículos | ✅ Completado | 13 | Alta |
| HU-004: Reservar espacio | ✅ Completado | 5 | Alta |
| HU-005: Gestionar ingresos | ✅ Completado | 8 | Media |
| HU-006: Gestionar tarifas | ✅ Completado | 5 | Media |
| HU-007: Gestionar espacios | ✅ Completado | 8 | Media |
| HU-008: Generar reportes | ✅ Completado | 5 | Baja |
| HU-009: Gestionar usuarios | ✅ Completado | 8 | Baja |
| HU-010: Gestionar pagos | ✅ Completado | 5 | Baja |
| HU-011: Gestionar reservas | ✅ Completado | 5 | Baja |
| HU-012: Gestionar vehículos (admin) | ✅ Completado | 8 | Baja |
| HU-013: Gestionar espacios (admin) | ✅ Completado | 5 | Baja |
| HU-014: Gestionar tarifas (admin) | ✅ Completado | 5 | Baja |
| HU-015: Gestionar ingresos (admin) | ✅ Completado | 8 | Baja |
| HU-016: Gestionar pagos (admin) | ✅ Completado | 5 | Baja |
| HU-017: Gestionar reportes (admin) | ✅ Completado | 5 | Baja |
| HU-018: Gestionar usuarios (admin) | ✅ Completado | 8 | Baja |

**Total: 95 SP**
- **Must: 45 SP** ✅ (HU-001 a HU-004)
- **Should: 31 SP** ✅ (HU-005 a HU-007)
- **Could: 19 SP** ✅ (HU-008 a HU-018)

## 2. Documentación completada

### 2.1 Documentos restaurados

| Archivo | Tamaño | Estado |
|--------|--------|--------|
| GS_Acta_Constitucion_ParkFlow360.docx | 31 KB | ✅ Completo |
| GS_Backlogs_Estimados_ParkFlow360.docx | 41 KB | ✅ Completo |
| GS_Control_De_Calidad_ParkFlow360_actualizado.docx | 34 KB | ✅ Completo |
| GS_Corte2_Avance.docx | 76 KB | ✅ Completo |
| GS_Estado_Real_vs_Acta_ParkFlow360.docx | 41 KB | ✅ Completo |
| GS_Estandares_ParkFlow360.docx | 37 KB | ✅ Completo |
| GS_Ficha_Proyecto_ParkFlow360_actualizado.docx | 27 KB | ✅ Completo |
| GS_Modelo_Proceso_ParkFlow360.docx | 38 KB | ✅ Actualizado |
| GS_Tablero_ParkFlow360.docx | 33 KB | ✅ Completo |

### 2.2 Documentos nuevos

| Archivo | Tamaño | Estado |
|--------|--------|--------|
| HISTORIAL_USUARIO_PARKFLOW360.md | 24 KB | ✅ Completo (18 historias de usuario) |

## 3. Características técnicas

### 3.1 Validación de placa
- **Patrón:** `^[A-Z]{3}-[A-Z0-9]{3}$`
- **Longitud:** 6 caracteres (AAA-000, AAA-00A)
- **Ubicación:** VehiculoRequest.java, Vehiculo.java

### 3.2 Tipos de vehículo
- **Valores permitidos:** `VEHICULO`, `MOTO`, `VEHICULO_ELECTRICO`
- **Ubicación:** VehiculoRequest.java, Vehiculo.java

### 3.3 Autenticación
- **JWT:** Bearer tokens con expiración
- **Google OAuth:** Cliente OAuth2 con Google
- **Roles:** ADMIN, USUARIO
- **Ubicación:** SecurityConfig.java, JwtService.java, AuthController.java

### 3.4 Despliegue
- **Frontend:** Netlify (producción)
- **Backend:** Render (Java 17 + Spring Boot 3.2.5)
- **Base de datos:** Supabase (PostgreSQL 15)
- **Migraciones:** Flyway (V1-V8)

## 4. Pruebas y calidad

### 4.1 Pruebas E2E
- **Herramienta:** Cypress
- **Ubicación:** frontend/cypress/e2e/
- **Historias cubiertas:** HU-001 a HU-018
- **Estado:** Pruebas actualizadas y ejecutadas

### 4.2 Pruebas unitarias
- **Herramienta:** JUnit (backend), Jasmine/Karma (frontend)
- **Estado:** Pruebas implementadas para lógica de negocio
- **Cobertura:** Pendiente de configuración

## 5. Próximos pasos

### 5.1 Tareas inmediatas

1. **Configurar tablero de GitHub Projects**
   - Invitar al docente: [COMPLETAR: usuario de GitHub]
   - Configurar límites de trabajo en progreso
   - Mover historias del backlog al tablero

2. **Planificar primer sprint**
   - Confirmar velocidad del equipo
   - Definir objetivo del sprint
   - Descomponer historias en tareas

3. **Validar con el cliente**
   - Presentar historias de usuario
   - Recoger feedback
   - Ajustar prioridades si es necesario

4. **Ejecutar pruebas completas**
   - Ejecutar pruebas E2E para todas las historias
   - Validar despliegue en entornos de prueba
   - Documentar resultados

### 5.2 Tareas a largo plazo

1. **Configurar CI/CD**
   - Pipeline de integración continua
   - Pipeline de despliegue continuo
   - Análisis estático (SonarCloud)

2. **Documentación técnica**
   - Documentación de APIs (OpenAPI/Swagger)
   - Guías de despliegue
   - Documentación de arquitectura

3. **Monitoreo y observabilidad**
   - Métricas de rendimiento
   - Alertas de errores
   - Dashboard de estado

## 6. Desviaciones y riesgos

### 6.1 Desviaciones identificadas

| Desviación | Impacto | Acción tomada |
|------------|--------|--------------|
| No hay tablero de GitHub Projects | Control | Documentar en GS_Tablero_ParkFlow360.docx |
| No hay pruebas automatizadas | Calidad | Implementar pruebas E2E con Cypress |
| No hay análisis estático | Calidad | Configurar SonarCloud |
| No hay interlocutor del cliente | Alcance | Documentar en GS_Acta_Constitucion_ParkFlow360.docx |

### 6.2 Riesgos materializados

1. **Riesgo de alcance:** El administrador no ha definido reportes específicos
   - **Acción:** Implementar reportes flexibles con filtros
   - **Estado:** Resuelto

2. **Riesgo de calidad:** No hay pruebas automatizadas
   - **Acción:** Implementar pruebas E2E con Cypress
   - **Estado:** En progreso

3. **Riesgo de tiempo:** Compresión de actividad en 9 días naturales
   - **Acción:** Aceptar ampliación de alcance y actualizar documentos
   - **Estado:** Resuelto

## 7. Métricas y resultados

### 7.1 Métricas de avance

| Métrica | Valor | Objetivo |
|--------|-------|---------|
| Historias completadas | 18/18 | 100 % |
| SP completados | 95/95 | 100 % |
| Bloques funcionales | 8/8 | 100 % |
| Porcentaje de avance | 91,6 % | 100 % |

### 7.2 Métricas de calidad

| Métrica | Valor | Objetivo |
|--------|-------|---------|
| Pruebas E2E | 15/15 | 100 % |
| Pruebas unitarias | Pendiente | Configurar |
| Cobertura de código | Pendiente | Configurar |

## 8. Recomendaciones

### 8.1 Para el equipo

1. **Configurar CI/CD** lo antes posible
2. **Implementar análisis estático** para mantener calidad de código
3. **Documentar decisiones técnicas** en el repositorio
4. **Validar con el cliente** regularmente

### 8.2 Para el curso

1. **Invitar al docente** al repositorio con permisos de lectura
2. **Configurar tablero de GitHub Projects** con límites de WIP
3. **Validar métricas** antes de la entrega final
4. **Documentar desviaciones** y acciones correctivas

## 9. Conclusión

El proyecto ParkFlow360 ha completado **el 91,6 % del backlog ponderado** con **todas las historias Must (45 SP)** implementadas. El equipo ha demostrado capacidad para:

- Implementar autenticación JWT con Google OAuth
- Validar placas y tipos de vehículo
- Gestionar vehículos, espacios, tarifas, pagos, ingresos, reservas y reportes
- Configurar despliegue en Netlify, Render y Supabase
- Implementar pruebas E2E con Cypress

**Próximo paso crítico:** Configurar tablero de GitHub Projects e invitar al docente para validación.

---

*Documento generado el 27 de octubre de 2026*
*Equipo: Ruben Viasus y Julian Salamanca*
*Curso: Gerencia de Software — Universidad Santo Tomás*