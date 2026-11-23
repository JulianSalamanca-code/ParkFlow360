# ParkFlow360

ParkFlow360 es un SaaS para la gestión de parqueaderos. La plataforma es operada por el rol `PLATAFORMA` y está pensada para que varias empresas administren sus propios parqueaderos, con roles separados para cajeros, guardias y usuarios.

## Guía de uso (funcionalidad vigente)

1. **Registro e inicio de sesión**: registro manual o inicio de sesión con Google (flujo manual con `state` y callback en el frontend).
2. **Reservas**: el usuario reserva un espacio seleccionando vehículo, espacio y tarifa.
3. **Ingresos y salidas**: el guardia/operador registra la llegada y la salida de la estancia; la salida requiere un pago confirmado.
4. **Pagos**: hoy los registra manualmente el administrador; la integración con Wompi aún no está implementada.
5. **Gestión de vehículos**: el usuario registra y gestiona sus vehículos (placa `AAA-000`).
6. **Importación de espacios**: se puede cargar un plano PDF/imagen y generar códigos por filas y columnas, o importar desde CSV UTF-8.

## Nuevas funcionalidades planificadas

El detalle normativo está en [`docs/01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md`](docs/01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md) y el alcance en la tabla F0–F10 de ese documento. **Aún no están implementadas**:

- Saldo recargable, facturas y cobro automático.
- Crédito del faltante con vencimiento a 3 días; deuda vencida que bloquea nuevas reservas.
- Hora de espera para reservas: llegada marcada por el guardia y cobro de una hora por no-show cuando la tarifa es por horas.
- Multiempresa: empresas con parqueaderos propios (identificador, capacidad y cupos por tipo de vehículo).
- Roles `PLATAFORMA` (antes `ADMIN`), `EMPRESA`, `CAJERO_PAGOS`, `GUARDIA` y `USUARIO`; vinculación de usuarios a varios parqueaderos por identificador.
- Menú inferior responsive con hamburguesa, pensado también para el futuro cliente Electron.

## Arranque local

Requiere Java 17, Maven, Node 22, pnpm y Docker.

```bash
docker compose up -d                    # PostgreSQL 15 en :5432
cd backend && mvn spring-boot:run       # API en :8080
cd frontend && pnpm install && pnpm start  # SPA en :4200, /api → :8080
```

Comandos de verificación: `mvn test` / `mvn verify` en backend; `pnpm build` y `pnpm e2e` en frontend. Las convenciones de código están en [`AGENTS.md`](AGENTS.md).

## Despliegue

- **Frontend**: Netlify (`netlify.toml`, build con pnpm, rewrite `/api/*` al backend).
- **Backend**: Render.
- **Base de datos**: Supabase (PostgreSQL).

## Documentación

- `AGENTS.md`: convenciones y reglas de negocio para agentes.
- `docs/01-especificaciones/ESPECIFICACIONES_PARKFLOW360.md`: especificaciones y tabla de implementación.
- `docs/01-especificaciones/BACKLOG_NUEVO.md`: historias derivadas del plan.
- `docs/01-especificaciones/PLAN_IMPLEMENTACION.md`: plan de ejecución por hitos para dos personas.
- `docs/INDICE_DOCUMENTACION.md`: clasificación de documentos vivos e históricos.

## Contribución

1. Haz un fork del proyecto.
2. Crea una rama para tu feature (convención: `feat/<descripcion>`).
3. Commitea tus cambios y haz push a la rama.
4. Abre un Pull Request.

## Licencia

MIT
