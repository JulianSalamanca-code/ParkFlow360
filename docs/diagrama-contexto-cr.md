# Diagrama de Contexto - ParkFlow360 (CR)

## Descripción
Diagrama de contexto C4 Level 1 que muestra el sistema ParkFlow360 y sus actores externos.

%%{init: {'theme':'dark','themeVariables':{'primaryColor':'#1e293b','primaryTextColor':'#e2e8f0','lineColor':'#94a3b8','tertiaryColor':'#334155','background':'#0f172a'}}}%%
```mermaid
graph TD
    subgraph Actores
        A1[Administrador<br/>Gestiona usuarios y reportes]
        A2[Usuario Operativo<br/>Registra vehículos, espacios y pagos]
        A3[Invitado<br/>Consulta información pública]
    end

    subgraph Sistema
        S[ParkFlow360<br/>SaaS gestión de parqueaderos]
    end

    subgraph Externos
        E1[Netlify<br/>Frontend Angular]
        E2[Railway/Render<br/>Backend Spring Boot]
        E3[Supabase<br/>PostgreSQL cloud]
        E4[Docker<br/>PostgreSQL local]
    end

    A1 -->|Gestiona| S
    A2 -->|Opera| S
    A3 -->|Consulta| S

    S -->|Despliega frontend| E1
    S -->|Despliega backend| E2
    S -->|Almacena datos| E3
    S -->|Desarrollo local| E4

    style S fill:#1e3a8a,stroke:#60a5fa,stroke-width:2px,color:#e2e8f0
    style A1 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A2 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style A3 fill:#78350f,stroke:#fbbf24,color:#fef3c7
    style E1 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E2 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E3 fill:#374151,stroke:#9ca3af,color:#e5e7eb
    style E4 fill:#374151,stroke:#9ca3af,color:#e5e7eb
```

## Actores
- **Administrador**: Acceso completo, gestión de usuarios y configuración
- **Usuario Operativo**: CRUD de módulos operativos
- **Invitado**: Acceso limitado a información pública

## Sistemas Externos
- Netlify: Frontend estático Angular
- Railway/Render: Backend Spring Boot
- Supabase: Base de datos PostgreSQL
- Docker: Entorno local de desarrollo

## Uso
Este diagrama se usa para:
- Entender límites del sistema
- Identificar dependencias externas
- Documentar actores y flujos de acceso
