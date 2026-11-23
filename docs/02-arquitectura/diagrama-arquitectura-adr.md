# Diagrama de Arquitectura — ParkFlow360 (ADR)

## Descripción

Diagrama de componentes C4 Level 2 y decisiones de arquitectura. Los módulos
`saldos`, `creditos`, `empresas` y `parqueaderos` son **previstos** (ver
[`especificación`](../01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md)); hoy no existen en el código.

%%{init: {'theme':'dark','themeVariables':{'primaryColor':'#1e293b','primaryTextColor':'#e2e8f0','lineColor':'#94a3b8','tertiaryColor':'#334155','background':'#0f172a'}}}%%
```mermaid
graph TD
    subgraph Frontend
        F[Frontend Angular<br/>SPA standalone]
    end

    subgraph Backend
        B[Backend Spring Boot<br/>API REST]
        V[Vehículos<br/>CRUD]
        E[Espacios<br/>CRUD y planos]
        T[Tarifas<br/>CRUD]
        I[Ingresos<br/>reservas, llegada, salida]
        P[Pagos<br/>CRUD y facturas]
        R[Reportes<br/>consultas]
        U[Usuarios<br/>JWT y roles]
        S2[Saldos y créditos<br/>planeado]
        M[Empresas y parqueaderos<br/>planeado]
    end

    subgraph Datos
        D[PostgreSQL<br/>Flyway V1-V16]
    end

    F -->|API REST /api| B
    B -->|JPA| D
    B --> V
    B --> E
    B --> T
    B --> I
    B --> P
    B --> R
    B --> U
    B -.-> S2
    B -.-> M

    style F fill:#1e3a8a,stroke:#60a5fa,stroke-width:2px,color:#e2e8f0
    style B fill:#78350f,stroke:#fbbf24,stroke-width:2px,color:#fef3c7
    style D fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style V fill:#064e3b,stroke:#34d399,color:#d1fae5
    style E fill:#064e3b,stroke:#34d399,color:#d1fae5
    style T fill:#064e3b,stroke:#34d399,color:#d1fae5
    style I fill:#064e3b,stroke:#34d399,color:#d1fae5
    style P fill:#064e3b,stroke:#34d399,color:#d1fae5
    style R fill:#064e3b,stroke:#34d399,color:#d1fae5
    style U fill:#7f1d1d,stroke:#f87171,color:#fee2e2
    style S2 fill:#1e293b,stroke:#64748b,stroke-dasharray: 4 3,color:#cbd5e1
    style M fill:#1e293b,stroke:#64748b,stroke-dasharray: 4 3,color:#cbd5e1
```

## Decisiones de Arquitectura (ADR)

### ADR-001: Monorepo
**Decisión:** usar monorepo para frontend y backend.  
**Justificación:** simplifica gestión de versiones y despliegue coordinado.  
**Estado:** aceptada.

### ADR-002: Spring Boot + Angular
**Decisión:** backend Java Spring Boot 3.2.5 y frontend Angular 22 standalone.  
**Justificación:** escalabilidad empresarial, tipado fuerte y ecosistema maduro.  
**Estado:** aceptada.

### ADR-003: JWT para autenticación
**Decisión:** Spring Security + JWT stateless, con Google OAuth2 manual.  
**Justificación:** escalable horizontalmente y compatible con SPA.  
**Estado:** aceptada.

### ADR-004: PostgreSQL + Flyway
**Decisión:** PostgreSQL con migraciones versionadas Flyway.  
**Justificación:** consistencia de datos y versionado del esquema.  
**Estado:** aceptada; migraciones actuales V1–V16.

### ADR-005: Despliegue separado
**Decisión:** frontend en Netlify, backend en Render y base de datos en Supabase.  
**Justificación:** optimización de costos y escalabilidad por capa. No se usa Railway.  
**Estado:** aceptada.

### ADR-006: Modelo multiempresa
**Decisión:** empresas con parqueaderos propios (identificador único, capacidad y cupos por tipo de vehículo); filtrado de datos por parqueadero en backend.  
**Justificación:** soportar SaaS con varios clientes y aislamiento de información.  
**Estado:** propuesta (F5–F8).

### ADR-007: Cobro automático con saldo y crédito
**Decisión:** factura única por ingreso, cobro con saldo y, si falta dinero, crédito del faltante con vencimiento a 3 días sin intereses.  
**Justificación:** eliminar filas de pago y mantener trazabilidad con ledger y pagos confirmados.  
**Estado:** propuesta (F1–F3).

### ADR-008: Hora de espera y no-show
**Decisión:** la reserva tiene un límite de una hora; el guardia registra la llegada y un job programado expira las reservas vencidas facturando una hora solo para tarifas `HORA`.  
**Justificación:** desincentivar ausencias sin cobrar tarifas de días u otras modalidades.  
**Estado:** propuesta (F4).

### ADR-009: Roles por responsabilidad
**Decisión:** `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA` y `USUARIO`, con cajero y guardia asignados a un único parqueadero.  
**Justificación:** separar pagos, operación en campo y gestión de empresas.  
**Estado:** propuesta (F0, F6–F8).

## Uso

Este diagrama se usa para documentar decisiones técnicas, entender componentes internos, guiar el desarrollo y facilitar el onboarding.
