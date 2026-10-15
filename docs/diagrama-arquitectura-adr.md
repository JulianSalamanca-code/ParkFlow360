# Diagrama de Arquitectura - ParkFlow360 (ADR)

## Descripción
Diagrama de componentes C4 Level 2 y decisiones de arquitectura.

%%{init: {'theme':'dark','themeVariables':{'primaryColor':'#1e293b','primaryTextColor':'#e2e8f0','lineColor':'#94a3b8','tertiaryColor':'#334155','background':'#0f172a'}}}%%
```mermaid
graph TD
    subgraph Frontend
        F[Frontend Angular<br/>SPA]
    end

    subgraph Backend
        B[Backend Spring Boot<br/>API REST]
        V[Vehículos<br/>CRUD]
        E[Espacios<br/>CRUD]
        T[Tarifas<br/>CRUD]
        P[Pagos<br/>CRUD]
        R[Reportes<br/>Consultas]
        S[Seguridad<br/>JWT]
    end

    subgraph Datos
        D[PostgreSQL<br/>DB]
    end

    F -->|API| B
    B -->|JPA| D
    B --> V
    B --> E
    B --> T
    B --> P
    B --> R
    B --> S

    style F fill:#1e3a8a,stroke:#60a5fa,stroke-width:2px,color:#e2e8f0
    style B fill:#78350f,stroke:#fbbf24,stroke-width:2px,color:#fef3c7
    style D fill:#374151,stroke:#9ca3af,stroke-width:2px,color:#e5e7eb
    style V fill:#064e3b,stroke:#34d399,color:#d1fae5
    style E fill:#064e3b,stroke:#34d399,color:#d1fae5
    style T fill:#064e3b,stroke:#34d399,color:#d1fae5
    style P fill:#064e3b,stroke:#34d399,color:#d1fae5
    style R fill:#064e3b,stroke:#34d399,color:#d1fae5
    style S fill:#7f1d1d,stroke:#f87171,color:#fee2e2
```

## Decisiones de Arquitectura (ADR)

### ADR-001: Monorepo
**Decisión:** Usar monorepo para frontend y backend
**Justificación:** Simplifica gestión de versiones y despliegue coordinado
**Estado:** Aceptada

### ADR-002: Spring Boot + Angular
**Decisión:** Backend Java Spring Boot, Frontend Angular
**Justificación:** Escalabilidad empresarial, tipado fuerte, ecosistema maduro
**Estado:** Aceptada

### ADR-003: JWT para autenticación
**Decisión:** Spring Security + JWT stateless
**Justificación:** Escalable horizontalmente, compatible con SPA
**Estado:** Aceptada

### ADR-004: PostgreSQL + Flyway
**Decisión:** PostgreSQL con migraciones Flyway
**Justificación:** Consistencia de datos, versionado de esquema
**Estado:** Aceptada

### ADR-005: Despliegue separado
**Decisión:** Frontend en Netlify, Backend en Railway/Render, DB en Supabase
**Justificación:** Optimización de costos y escalabilidad por capa
**Estado:** Aceptada

## Uso
Este diagrama se usa para:
- Documentar decisiones arquitectónicas
- Entender componentes internos
- Guiar desarrollo y mantenimiento
- Onboarding de nuevos desarrolladores
