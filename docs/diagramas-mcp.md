# Manual de Contexto de Diagramas - ParkFlow360

## Resumen
Este documento describe los diagramas CR y ADR creados para visualización de la arquitectura de ParkFlow360.

## Diagramas Disponibles

### 1. Diagrama de Contexto CR
**Archivo:** `docs/diagrama-contexto-cr.md`
**Tipo:** C4 Context Level 1
**Propósito:** Mostrar límites del sistema y actores externos

**Contenido:**
- Actores: Administrador, Usuario Operativo, Invitado
- Sistema principal: ParkFlow360
- Sistemas externos: Netlify, Railway/Render, Supabase, Docker
- Relaciones de comunicación

**Uso:**
- Entender quién interactúa con el sistema
- Identificar dependencias externas
- Documentar alcance del proyecto

**Tecnologías representadas:**
- Frontend hosting: Netlify
- Backend hosting: Railway/Render
- Base de datos: Supabase PostgreSQL
- Desarrollo local: Docker

### 2. Diagrama de Arquitectura ADR
**Archivo:** `docs/diagrama-arquitectura-adr.md`
**Tipo:** C4 Component Level 2 + ADR
**Propósito:** Detallar componentes internos y decisiones arquitectónicas

**Contenido:**
- Contenedor: Monorepo ParkFlow360
- Componentes: Frontend Angular, Backend Spring Boot, PostgreSQL
- Módulos: Vehículos, Espacios, Tarifas, Pagos, Reportes, Seguridad
- Decisiones ADR-001 a ADR-005

**Decisiones documentadas:**
- ADR-001: Monorepo
- ADR-002: Spring Boot + Angular
- ADR-003: JWT para autenticación
- ADR-004: PostgreSQL + Flyway
- ADR-005: Despliegue separado

**Uso:**
- Guiar desarrollo
- Documentar por qué se tomaron decisiones
- Facilitar mantenimiento y escalabilidad
- Onboarding de equipo

## Cómo visualizar
Los diagramas usan sintaxis Mermaid C4. Pueden visualizarse en:
- GitHub/GitLab (renderizado automático)
- VS Code con extensión Mermaid
- Herramientas como Mermaid Live Editor

## Mantenimiento
Actualizar diagramas cuando:
- Se agreguen nuevos módulos
- Cambien dependencias externas
- Se tomen nuevas decisiones arquitectónicas
- Cambie el stack tecnológico

## Relación con proyecto
Estos diagramas complementan `proyecto-mcp.md` y proporcionan vista visual de la arquitectura documentada en README.md.
