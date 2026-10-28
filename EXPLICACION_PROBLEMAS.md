# Explicación de los problemas identificados

## 1. Validación de placa no funciona en producción

### ¿Por qué la placa "ABC1234" fue aceptada?

**Causa raíz:** El backend desplegado en Render está ejecutando la rama `main`, NO la rama `feat/registro-placa-tipos-vehiculo`.

### Evidencia:

1. **Rama main** (desplegada en Render):
   - No tiene validación de placa
   - VehiculoRequest no tiene anotaciones @Pattern
   - Acepta cualquier formato de placa

2. **Rama feat/registro-placa-tipos-vehiculo** (no desplegada):
   - Tiene validación de placa con regex `^[A-Z]{3}-[A-Z0-9]{3}$`
   - Rechaza placas sin guión o con formato incorrecto
   - Validación activa en código

### Verificación:

```bash
# En rama feat/registro-placa-tipos-vehiculo
git show fb1cd5e --name-only
# backend/src/main/java/com/parkflow360/modulos/vehiculos/dto/VehiculoRequest.java

# En rama main
git log --oneline main | grep -i "placa\|vehiculo"
# No hay commits de validación de placa
```

### Solución:

1. Hacer merge de `feat/registro-placa-tipos-vehiculo` a `main`
2. Desplegar a Render
3. La validación funcionará correctamente

---

## 2. Google OAuth callback da error 500

### ¿Por qué GET /api/auth/google/callback?code=test da 500?

**Causa raíz:** El endpoint NO existe en la rama `main` desplegada en Render.

### Evidencia:

1. **Rama feat/google-oauth-completo** (no desplegada):
   - Tiene endpoint `GET /api/auth/google/callback`
   - Implementa `handleGoogleCallback()` en GoogleAuthService
   - Maneja intercambio de código por token

2. **Rama main** (desplegada en Render):
   - NO tiene endpoint `/api/auth/google/callback`
   - Solo tiene `POST /api/auth/google` (flujo simulado)
   - Al intentar acceder a `/api/auth/google/callback`, Spring devuelve 500

### Error específico:

```
{"path":"uri=/api/auth/google/callback","error":"Error interno del servidor","message":"No static resource api/auth/google/callback.","timestamp":"2026-10-28T19:40:54.96577205","status":500}
```

**Explicación:** Spring Boot está intentando encontrar un recurso estático en `/api/auth/google/callback` y no lo encuentra, por lo que devuelve 500.

### Solución:

1. Hacer merge de `feat/google-oauth-completo` a `main`
2. Desplegar a Render
3. Configurar Google Cloud Console con redirect URI correcto
4. Configurar variables de entorno en Render

---

## 3. Resumen de ramas

### Rama main (desplegada en Render)
- ✅ Login básico
- ✅ Google OAuth simulado (POST /api/auth/google)
- ✅ CRUD vehículos (sin validaciones)
- ❌ Registro de usuarios
- ❌ Validación de placa
- ❌ Google OAuth real con callback

### Rama feat/registro-placa-tipos-vehiculo
- ✅ Registro de usuarios
- ✅ Validación de placa (AAA-000)
- ✅ Tipos de vehículo limitados
- ❌ Google OAuth real

### Rama feat/google-oauth-completo
- ✅ Google OAuth real con callback
- ✅ Flujo completo OAuth2
- ❌ Registro de usuarios (ya está en otra rama)

---

## 4. Plan de acción

### Paso 1: Merge de ramas
```bash
git checkout main
git merge feat/registro-placa-tipos-vehiculo
git merge feat/google-oauth-completo
```

### Paso 2: Resolver conflictos
- Revisar conflictos en AuthController
- Combinar endpoints de registro y Google OAuth

### Paso 3: Desplegar
```bash
git push origin main
# Render auto-desplegará
```

### Paso 4: Configurar Google Cloud Console
- Redirect URI: `https://parkflow360.onrender.com/api/auth/google/callback`
- Authorized origins: `https://heartfelt-starlight-83718b.netlify.app`

### Paso 5: Configurar variables de entorno en Render
```
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
OAUTH2_REDIRECT_URI=https://parkflow360.onrender.com/api/auth/google/callback
```

### Paso 6: Probar
```bash
# Validación de placa
curl -X POST https://parkflow360.onrender.com/api/vehiculos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"placa":"ABC123","tipo":"VEHICULO"}'
# Debería fallar con 400 Bad Request

# Google OAuth callback
curl https://parkflow360.onrender.com/api/auth/google/callback?code=test
# Debería procesar el código
```

---

## 5. Conclusión

Los problemas identificados NO son bugs en el código, sino que las features **no están desplegadas en producción**.

El backend en Render está ejecutando la rama `main` que es anterior a las features implementadas en las ramas de desarrollo.

**Acción inmediata:** Hacer merge de las ramas a main y desplegar.
