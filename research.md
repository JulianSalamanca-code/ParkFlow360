# Research: Registro y recuperación

## Decisión 1: alcance de recuperación

- **Decisión**: implementar recuperación mediante token temporal enviado por correo y restablecimiento de contraseña.
- **Rationale**: el login enlaza a `/auth/recuperar`, pero no existe ruta ni backend de recuperación.
- **Alternativas consideradas**: mostrar solo un mensaje sin enviar correo (incompleto), reutilizar contraseña temporal (menos seguro).

## Decisión 2: respuesta ante correo inexistente

- **Decisión**: responder el mismo mensaje cuando el correo exista o no.
- **Rationale**: evita enumeración de cuentas.
- **Alternativas consideradas**: distinguir correo existente (vulnerable).

## Decisión 3: almacenamiento del token

- **Decisión**: guardar solamente el hash del token y una fecha de expiración.
- **Rationale**: reduce el impacto de una fuga de base de datos.
- **Alternativas consideradas**: almacenar el token completo (desaconsejado).

## Pendiente

- Confirmar proveedor SMTP del despliegue y disponer credenciales fuera del repositorio.
