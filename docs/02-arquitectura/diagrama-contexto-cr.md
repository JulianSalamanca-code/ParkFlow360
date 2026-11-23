# Diagrama de Contexto — ParkFlow360 (CR)

## Descripción

Diagrama de contexto C4 Level 1. Los actores reflejan el modelo objetivo de la
[`especificación`](../01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md): `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA` y `USUARIO`.
Los cinco roles están implementados. `PLATAFORMA` acepta `ADMIN` como alias transitorio para no invalidar tokens vigentes.

%%{init: {'theme':'dark','themeVariables':{'primaryColor':'#1e293b','primaryTextColor':'#e2e8f0','lineColor':'#94a3b8','tertiaryColor':'#334155','background':'#0f172a'}}}%%
```mermaid
graph TD
    subgraph Actores
        A1[PLATAFORMA<br/>Control total de la plataforma]
        A2[EMPRESA<br/>Administra sus parqueaderos]
        A3[CAJERO_PAGOS<br/>Gestiona pagos de su parqueadero]
        A4[GUARDIA<br/>Verifica datos y marca llegadas]
        A5[USUARIO<br/>Reserva y recarga saldo]
    end

    subgraph Sistema
        S[ParkFlow360<br/>SaaS gestión de parqueaderos]
    end

    subgraph Externos
        E1[Netlify<br/>Frontend Angular]
        E2[Render<br/>Backend Spring Boot]
        E3[Supabase<br/>PostgreSQL cloud]
        E4[Docker<br/>PostgreSQL local]
        E5[Google OAuth<br/>Autenticación]
    end

    A1 -->|Administra| S
    A2 -->|Configura parqueaderos| S
    A3 -->|Registra pagos| S
    A4 -->|Opera en campo| S
    A5 -->|Reserva y paga| S

    S -->|Despliega frontend| E1
    S -->|Despliega backend| E2
    S -->|Almacena datos| E3
    S -->|Desarrollo local| E4
    S -->|Autentica con| E5

    style S fill:#1e3a8a,stroke:#60a5fa,stroke-width:2px,color:#e2e8f0
    style A1 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A2 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A3 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A4 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A5 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style E1 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E2 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E3 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E4 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E5 fill:#374151,stroke:#9ca3af,color:#e5e7eb
```

## Actores

- **PLATAFORMA**: control absoluto; reemplaza al actual `ADMIN`.
- **EMPRESA**: alta de parqueaderos, configuración de capacidad y cupos, asignación de cajeros y guardias.
- **CAJERO_PAGOS**: usuario exclusivo de un parqueadero, creado con él; revisa, crea y marca pagos.
- **GUARDIA**: verifica datos, marca la llegada y consulta el estado de pago del vehículo.
- **USUARIO**: se vincula a varios parqueaderos por identificador, reserva, recarga y solicita crédito.

## Sistemas externos

- Netlify: frontend estático Angular.
- Render: backend Spring Boot (antes se documentaba también Railway; no está en la configuración del repo).
- Supabase: PostgreSQL de producción.
- Docker: PostgreSQL local de desarrollo.
- Google OAuth: proveedor de identidad del flujo manual existente.

## Uso

Este diagrama se usa para entender los límites del sistema, identificar dependencias externas y documentar actores y flujos de acceso.
