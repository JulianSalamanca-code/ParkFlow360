# Manual de Contexto de Diagramas — ParkFlow360

## Resumen

Este documento describe los diagramas CR y ADR de la arquitectura de ParkFlow360.
Los actores y decisiones nuevas corresponden al plan descrito en
[`ESPECIFICACIONES_PARKFLOW360.md`](ESPECIFICACIONES_PARKFLOW360.md).

## Diagramas disponibles

### 1. Diagrama de Contexto CR

**Archivo:** `docs/diagrama-contexto-cr.md`  
**Tipo:** C4 Context Level 1  
**Propósito:** mostrar límites del sistema y actores externos.

**Contenido:**

- Actores: `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA`, `USUARIO`.
- Sistema principal: ParkFlow360.
- Sistemas externos: Netlify, Render, Supabase, Docker y Google OAuth.
- Relaciones de comunicación.

**Tecnologías representadas:** frontend en Netlify, backend en Render, PostgreSQL en Supabase, Docker para desarrollo local y Google como proveedor de identidad.

### 2. Diagrama de Arquitectura ADR

**Archivo:** `docs/diagrama-arquitectura-adr.md`  
**Tipo:** C4 Component Level 2 + ADR  
**Propósito:** detallar componentes internos y decisiones arquitectónicas.

**Contenido:**

- Contenedor: monorepo ParkFlow360.
- Componentes: frontend Angular, backend Spring Boot y PostgreSQL.
- Módulos actuales: Vehículos, Espacios, Tarifas, Ingresos, Pagos, Reportes, Usuarios y Seguridad.
- Módulos previstos: Saldos/Créditos y Empresas/Parqueaderos.

**Decisiones documentadas:**

- ADR-001: Monorepo.
- ADR-002: Spring Boot + Angular.
- ADR-003: JWT y Google OAuth.
- ADR-004: PostgreSQL + Flyway.
- ADR-005: despliegue separado (Netlify / Render / Supabase).
- ADR-006: modelo multiempresa (propuesta).
- ADR-007: cobro automático con saldo y crédito (propuesta).
- ADR-008: hora de espera y no-show (propuesta).
- ADR-009: roles por responsabilidad (propuesta).

## Cómo visualizar

Los diagramas usan sintaxis Mermaid. Pueden visualizarse en GitHub, VS Code con extensión Mermaid o Mermaid Live Editor.

## Mantenimiento

Actualizar los diagramas cuando:

- se agreguen nuevos módulos o cambien los roles;
- cambien dependencias externas;
- se acepten nuevas decisiones arquitectónicas;
- cambie el stack tecnológico (versiones vigentes en `AGENTS.md`).

## Relación con el proyecto

Estos diagramas complementan `proyecto-mcp.md`, `AGENTS.md` y la especificación de funcionalidades.
