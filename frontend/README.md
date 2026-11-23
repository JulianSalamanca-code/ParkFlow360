# Frontend ParkFlow360

SPA Angular 22 (standalone, sin NgModules) dentro del monorepo de ParkFlow360.

## Desarrollo

```bash
pnpm install
pnpm start          # ng serve en http://localhost:4200
```

El servidor de desarrollo usa `proxy.conf.json`: todas las peticiones a `/api` se redirigen a `http://localhost:8080` (backend). La SPA se recarga automáticamente al cambiar archivos.

## Build

```bash
pnpm build          # genera dist/frontend/browser
```

## Pruebas

```bash
pnpm e2e            # Cypress contra http://localhost:4200
pnpm e2e:open       # interfaz interactiva de Cypress
npm test            # target Karma/Jasmine (actualmente sin archivos *.spec.ts)
```

Las especificaciones E2E están en `cypress/e2e/` y usan `data-testid` y los comandos personalizados de `cypress/support/commands.ts`.

## Estructura

- `src/app/auth`: login, registro y callback de Google.
- `src/app/core`: guards (`RoleGuard`) e interceptor JWT.
- `src/app/features`: módulos por dominio con `components/`, `services/`, `models/` y `<dominio>.routes.ts`.
- `src/app/shared`: componentes y servicios compartidos.

## Referencias

- Comandos y convenciones: [`../AGENTS.md`](../AGENTS.md).
- Funcionalidades planificadas: [`../docs/01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md`](../docs/01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md).

Stack: Angular `^22.2.4`, TypeScript `~6.0.3`, RxJS `7.8`, Cypress `^16.2.0`, Node 22 en Netlify (usa `pnpm-lock.yaml`).
