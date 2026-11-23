# ParkFlow360

ParkFlow360 es una aplicación SaaS para la gestión de parqueaderos. Permite a los usuarios reservar espacios de estacionamiento de manera eficiente y segura.

## Guía de Uso

1. **Registro e Inicio de Sesión**: Los usuarios pueden registrarse manualmente o iniciar sesión con Google.
2. **Reservas**: Los usuarios registrados pueden reservar espacios seleccionando fecha y hora.
3. **Pagos**: El administrador registra cada pago vinculado a una estancia y marca su resultado como confirmado o fallido. Solo una estancia con pago confirmado puede finalizarse. La integración con Wompi aún no está implementada.
4. **Gestión de Vehículos**: Los usuarios pueden registrar y gestionar sus vehículos.
5. **Importación de parqueaderos**: Los administradores pueden cargar un plano PDF/imagen y generar códigos por filas y columnas, o importar espacios desde un archivo CSV. Las hojas de Excel se pueden guardar como CSV UTF-8 antes de cargarlas.

## Despliegue

- **Frontend**: Netlify
- **Backend**: Render
- **Base de Datos**: Supabase

## Contribución

Para contribuir, sigue estos pasos:
1. Haz un fork del proyecto.
2. Crea una branch para tu feature.
3. Commitea tus cambios.
4. Haz un push a la branch.
5. Abre un Pull Request.

## Licencia

MIT
