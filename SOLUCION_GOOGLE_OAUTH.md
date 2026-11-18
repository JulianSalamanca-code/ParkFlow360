# Configuración actual de Google OAuth

El frontend inicia el flujo desde `/api/auth/google/authorize`; el backend construye la URL con `GOOGLE_CLIENT_ID`. Google retorna al callback del frontend (`/auth/google/callback`), que envía el código al backend para intercambiarlo usando el secreto del servidor.

## Variables de entorno del backend

Configura en Render:

```text
GOOGLE_CLIENT_ID=<client-id de tipo aplicación web>
GOOGLE_CLIENT_SECRET=<client-secret>
OAUTH2_REDIRECT_URI=https://heartfelt-starlight-83718b.netlify.app/auth/google/callback
CORS_ALLOWED_ORIGINS=https://heartfelt-starlight-83718b.netlify.app
```

No publiques ni incluyas `GOOGLE_CLIENT_SECRET` en el frontend. El valor de `OAUTH2_REDIRECT_URI` debe coincidir exactamente con el callback registrado en Google Cloud Console. En **APIs y servicios > Credenciales > ID de cliente OAuth 2.0**, agrega esa URL a **URIs de redireccionamiento autorizados**. La URL autorizada es la del frontend, no `/api/auth/google/callback` en Render.

Netlify debe conservar la regla `/api/*` que redirige al backend de Render. En entornos locales, el callback predeterminado es `http://localhost:4200/auth/google/callback`.

## Diagnóstico de errores

- Si Google muestra `redirect_uri_mismatch`, compara la URL del navegador y `OAUTH2_REDIRECT_URI` carácter por carácter con el URI autorizado.
- Si el backend responde que Google rechazó el código, confirma que el `client ID`, el secreto y el URI de retorno pertenezcan al mismo cliente OAuth y despliega las variables actualizadas.
- Si `/api/auth/google/authorize` indica que el flujo no está configurado, falta `GOOGLE_CLIENT_ID` en Render.
- La respuesta a `/api/auth/google/callback` no se prueba con `code=test`: Google emite códigos de un solo uso, ligados al cliente y al URI de retorno.

El código puede revisarse localmente, pero confirmar una respuesta 401 en producción requiere ejecutar un inicio de sesión real con las credenciales de Google configuradas en Render.
