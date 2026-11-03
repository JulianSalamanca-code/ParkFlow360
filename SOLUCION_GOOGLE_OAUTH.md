# SOLUCIÓN AL PROBLEMA DE GOOGLE OAUTH EN PRODUCCIÓN

## 📋 Resumen del problema

- **Síntoma:** `GET /api/auth/google/callback?code=...` devuelve 500 con error "No static resource api/auth/google/callback"
- **Causa:** Spring Boot intenta servir un recurso estático en lugar de enrutar a la controlador
- **Impacto:** Google OAuth no funciona en producción

## 🔍 Análisis técnico

### ¿Por qué ocurre el error 500?

Spring Boot tiene un manejador de recursos estáticos que tiene prioridad sobre las anotaciones `@Controller`/`@RestController` cuando existe un conflicto de rutas.

El error `"No static resource api/auth/google/callback"` significa que Spring Boot busca un archivo físico en el classpath o en el sistema de archivos en la ruta `/api/auth/google/callback` y no lo encuentra, devolviendo 500 en lugar de enrutar a nuestro controlador.

### ¿Por qué solo en producción?

- **Local:** Spring Boot se ejecuta con perfil `local` y configuración diferente
- **Producción (Render):** Se usa configuración por defecto de Spring Boot
- **Diferencia clave:** Configuración de recursos estáticos en producción

## 🛠️ Solución implementada

### Opción 1: Cambiar ruta del endpoint (recomendado)

```java
// En AuthController.java
@GetMapping("/google/callback")
@ResponseBody
public ResponseEntity<LoginResponse> googleCallback(@RequestParam String code) {
    return ResponseEntity.ok(googleAuthService.handleGoogleCallback(code));
}
```

**Cambio:** Se agrega `@ResponseBody` explícitamente para forzar la respuesta como JSON.

### Opción 2: Usar flujo OAuth2 estándar de Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        // Configuración OAuth2 estándar de Spring Security
    }
    
    @GetMapping("/oauth2/authorization/google")
    public String googleAuth() {
        return "redirect:" + googleOAuth2AuthenticationUrl;
    }
    
    @GetMapping("/login/oauth2/code/google")
    public String googleCallback(@RequestParam String code) {
        // Manejar callback aquí
    }
}
```

**Ventaja:** Usa la implementación OAuth2 nativa de Spring Security
**Desventaja:** Requiere más configuración

### Opción 3: Usar Google JavaScript SDK (frontend only)

```javascript
// En login.component.ts
async loginWithGoogle() {
  const { google } = window as any;
  const auth2 = google.accounts.oauth2.initAuth2({
    client_id: 'YOUR_GOOGLE_CLIENT_ID',
    scope: 'email profile'
  });
  
  auth2.grantOfflineAccess().then(({ code }) => {
    this.authService.handleGoogleCallback(code).subscribe(...);
  });
}
```

**Ventaja:** Evita completamente el callback en backend
**Desventaja:** Requiere habilitar Google JavaScript SDK

## 📋 Pasos para implementar solución en producción

### 1. Aplicar cambio en AuthController

```bash
# Editar el archivo
cd /home/juliansalamanca/Documentos/Proyects/Parqueadero/ParkFlow360/backend/src/main/java/com/parkflow360/security/controller/AuthController.java
# Agregar @ResponseBody a googleCallback method
```

### 2. Reconstruir y desplegar
```bash
# En el directorio del backend
cd /home/juliansalamanca/Documentos/Proyects/Parqueadero/ParkFlow360/backend
./mvnw clean package -DskipTests
# Luego hacer push a GitHub para que Render reconstruya
```

### 3. Configurar Google Cloud Console
- Redirect URI: `https://parkflow360.onrender.com/api/auth/google/callback`
- Authorized origins: `https://heartfelt-starlight-83718b.netlify.app`

### 4. Configurar variables de entorno en Render
```bash
GOOGLE_CLIENT_ID=tu-client-id
GOOGLE_CLIENT_SECRET=tu-client-secret
OAUTH2_REDIRECT_URI=https://parkflow360.onrender.com/api/auth/google/callback
```

## ✅ Verificación

Después de aplicar el cambio:

```bash
# Test en producción
curl -s https://parkflow360.onrender.com/api/auth/google/callback?code=test
# Debería devolver un JSON con el token
```

## 📝 Documentación adicional

Se ha creado un archivo `SOLUCION_GOOGLE_OAUTH.md` con la explicación completa y pasos para implementar.

**Nota:** El problema de validación de placa también se resolverá al hacer merge de la rama a main, ya que ahora el backend en Render ejecutará la versión con validaciones activas.