# Reporte de Pruebas E2E - ParkFlow360

## Fecha: 2026-10-28

## Resumen Ejecutivo

Se realizaron pruebas E2E contra el backend desplegado en Render (https://parkflow360.onrender.com) para verificar endpoints de las ramas:
- `feat/registro-placa-tipos-vehiculo`
- `feat/google-oauth-completo`

**Estado del backend desplegado:** Rama `main` (no incluye features de las ramas de desarrollo)

---

## Rama: feat/registro-placa-tipos-vehiculo

### Estado: ❌ NO DESPLEGADA EN PRODUCCIÓN

El backend en Render está ejecutando la rama `main`, que no incluye las features de esta rama.

### Features implementadas en esta rama:

#### 1. Registro de usuarios
- **Endpoint:** `POST /api/auth/registro`
- **Estado en producción:** ❌ No disponible
- **Estado en código:** ✅ Implementado
- **Descripción:** Permite registro de usuarios con rol USUARIO por defecto

#### 2. Validación de placa (6 caracteres)
- **Endpoint:** `POST /api/vehiculos`
- **Estado en producción:** ❌ No validado
- **Estado en código:** ✅ Implementado
- **Regla:** `^[A-Z]{3}-[A-Z0-9]{3}$` (ej: AAA-000, AAA-00A)
- **Prueba realizada:**
  - ✅ `ABC-123` → Aceptado (correcto)
  - ❌ `ABC123` → Aceptado (debería fallar)
  - ❌ `ABC1234` → Aceptado (debería fallar)
- **Conclusión:** Validación no está activa en producción

#### 3. Tipos de vehículo limitados
- **Endpoint:** `POST /api/vehiculos`
- **Estado en producción:** ❌ No validado
- **Estado en código:** ✅ Implementado
- **Tipos permitidos:** VEHICULO, MOTO, VEHICULO_ELECTRICO
- **Prueba realizada:** Tipos aceptados sin validación en producción

---

## Rama: feat/google-oauth-completo

### Estado: ❌ NO DESPLEGADA EN PRODUCCIÓN

El backend en Render está ejecutando la rama `main`, que no incluye las features de esta rama.

### Features implementadas en esta rama:

#### 1. Google OAuth Callback
- **Endpoint:** `GET /api/auth/google/callback?code=...`
- **Estado en producción:** ❌ No disponible
- **Estado en código:** ✅ Implementado
- **Descripción:** Maneja callback de Google OAuth2 con intercambio de código por token

#### 2. Google OAuth con flujo completo
- **Endpoint:** `GET /api/auth/google/callback`
- **Estado en producción:** ❌ No disponible
- **Estado en código:** ✅ Implementado
- **Flujo:** Google → Callback → Backend → JWT → Frontend

#### 3. Componente Google Callback
- **Archivo:** `frontend/src/app/auth/components/google-callback/google-callback.component.ts`
- **Estado:** ✅ Implementado
- **Descripción:** Maneja redirección de Google y procesa código de autorización

---

## Rama: main (Desplegada en producción)

### Estado: ✅ DESPLEGADA EN PRODUCCIÓN

### Endpoints verificados:

#### 1. Login
- **Endpoint:** `POST /api/auth/login`
- **Estado:** ✅ Funcional
- **Prueba:** Login con admin@parkflow360.com / admin123 → ✅ Exitoso
- **Respuesta:** Token JWT válido

#### 2. Google OAuth (simulado)
- **Endpoint:** `POST /api/auth/google`
- **Estado:** ✅ Funcional
- **Prueba:** Login con email/nombre/googleId → ✅ Exitoso
- **Nota:** Flujo simulado, no OAuth2 real

#### 3. Vehículos CRUD
- **Endpoint:** `GET /api/vehiculos`
- **Estado:** ✅ Funcional
- **Prueba:** Listado de vehículos → ✅ Exitoso

- **Endpoint:** `POST /api/vehiculos`
- **Estado:** ✅ Funcional
- **Prueba:** Creación con placa ABC-123 → ✅ Exitoso
- **Nota:** Sin validación de formato de placa

#### 4. Registro de usuarios
- **Endpoint:** `POST /api/auth/registro`
- **Estado:** ❌ No disponible
- **Error:** 500 - No static resource

---

## Recomendaciones

### Prioridad Alta:
1. **Desplegar rama `feat/registro-placa-tipos-vehiculo` a producción**
   - Incluye registro de usuarios
   - Validación de placa
   - Tipos de vehículo limitados

2. **Desplegar rama `feat/google-oauth-completo` a producción**
   - Google OAuth real con callback
   - Flujo completo de autenticación

### Prioridad Media:
3. **Configurar Google Cloud Console**
   - Actualizar redirect URI a `/api/auth/google/callback`
   - Agregar URIs para desarrollo y producción

4. **Variables de entorno en Render**
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`
   - `OAUTH2_REDIRECT_URI`

### Prioridad Baja:
5. **Ejecutar pruebas E2E con Cypress**
   - Requiere frontend en ejecución local
   - Backend en Render
   - Tests de registro, placa, tipos vehículo

---

## Conclusión

Las features implementadas en las ramas de desarrollo **NO están desplegadas en producción**. El backend en Render está ejecutando la rama `main` que solo incluye:
- Login básico
- Google OAuth simulado
- CRUD de vehículos sin validaciones

Para verificar completamente las features, es necesario:
1. Hacer merge de las ramas a main
2. Desplegar a Render
3. Ejecutar pruebas E2E completas

---

## Comandos de prueba utilizados

```bash
# Login
curl -s https://parkflow360.onrender.com/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@parkflow360.com","password":"admin123"}'

# Crear vehículo
TOKEN=$(curl -s https://parkflow360.onrender.com/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@parkflow360.com","password":"admin123"}' \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")

curl -s https://parkflow360.onrender.com/api/vehiculos \
  -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"placa":"ABC-123","tipo":"VEHICULO","color":"Azul","modelo":"Test"}'
```

---

**Generado:** 2026-10-28 14:40:00
**Backend:** https://parkflow360.onrender.com
**Rama desplegada:** main
