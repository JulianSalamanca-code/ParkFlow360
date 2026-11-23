# Configuración actual de Google OAuth

El frontend inicia el flujo desde `GET /api/auth/google/authorize?state=...`; el backend construye la URL con `GOOGLE_CLIENT_ID` y valida el `state`. Google retorna al callback del frontend (`/auth/google/callback`), que envía el código al backend (`GET /api/auth/google/callback?code=...`) para intercambiarlo usando el secreto del servidor.

> No existe un flujo simulado `POST /api/auth/google`: ese endpoint aparece en documentos históricos, pero no está en el código actual.

## Variables de entorno del backend

Configura en Render, reemplazando `<dominio-de-netlify>` por el dominio real de la app:

```text
GOOGLE_CLIENT_ID=<client-id de tipo aplicación web>
GOOGLE_CLIENT_SECRET=<client-secret>
OAUTH2_REDIRECT_URI=https://<dominio-de-netlify>/auth/google/callback
CORS_ALLOWED_ORIGINS=https://<dominio-de-netlify>
```

No publiques ni incluyas `GOOGLE_CLIENT_SECRET` en el frontend. El valor de `OAUTH2_REDIRECT_URI` debe coincidir exactamente con el callback registrado en Google Cloud Console. En **APIs y servicios > Credenciales > ID de cliente OAuth 2.0**, agrega esa URL a **URIs de redireccionamiento autorizados**. La URL autorizada es la del frontend, no `/api/auth/google/callback` en Render.

Netlify debe conservar la regla `/api/*` que redirige al backend de Render. En entornos locales, el callback predeterminado es `http://localhost:4200/auth/google/callback` y el origen permitido es `http://localhost:4200`.

## Usuarios creados por Google

`GoogleAuthService` crea o reutiliza el usuario con el email verificado y asigna el rol `USUARIO` (en el código actual). En el modelo objetivo de la [especificación](docs/ESPECIFICACIONES_PARKFLOW360.md), los roles `PLATAFORMA`, `EMPRESA`, `CAJERO_PAGOS` y `GUARDIA` se crean por alta administrativa, no por OAuth.

## Diagnóstico de errores

- Si Google muestra `redirect_uri_mismatch`, compara la URL del navegador y `OAUTH2_REDIRECT_URI` carácter por carácter con el URI autorizado.
- Si el backend responde que Google rechazó el código, confirma que el `client ID`, el secreto y el URI de retorno pertenezcan al mismo cliente OAuth y despliega las variables actualizadas.
- Si `/api/auth/google/authorize` indica que el flujo no está configurado, falta `GOOGLE_CLIENT_ID` en Render.
- La respuesta a `/api/auth/google/callback` no se prueba con `code=test`: Google emite códigos de un solo uso, ligados al cliente y al URI de retorno.
- Si el callback frontend no se ejecuta, revisa que la regla `/api/*` de Netlify apunte al backend y que `CORS_ALLOWED_ORIGINS` incluya el dominio correcto.

El código puede revisarse localmente, pero confirmar una respuesta 401 en producción requiere ejecutar un inicio de sesión real con las credenciales de Google configuradas en Render.
