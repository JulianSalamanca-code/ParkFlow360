# PROMPT MAESTRO — Documentación del proyecto ParkFlow360

Este archivo es el **índice y contexto global**. Los documentos del curso ya están generados en la carpeta [`docs/`](docs/) como archivos `.docx`. Las secciones de abajo siguen sirviendo como guía de contenido y como contexto para regenerar o actualizar cualquier documento pegándolo en una IA y guardando como `.docx` (o exportando a PDF cuando se indique).

---

## Cómo usar estos prompts

1. Abre el archivo del documento que necesitas (tabla de abajo).
2. Copia **todo** su contenido y pégalo en la IA.
3. Pide el documento completo y, si la IA se corta, pide "continúa".
4. Copia el resultado y pégalo en un documento de Word. Los `#`, `##`, `###` se convierten en Título 1/2/3 y las tablas en tablas reales (Word: *Insertar > Tabla > Convertir texto en tabla*, o pegar con formato Markdown si la IA lo genera compatible).
5. Guarda con el **nombre de archivo exacto** que indica cada prompt.
6. Antes de radicar, llena los campos marcados `[COMPLETAR: ...]` o `[MEDIR: ...]` con datos reales.

---

## Contexto del proyecto (no lo cambies)

- **Proyecto:** ParkFlow360 — SaaS web de gestión de parqueaderos para empresas y universidades.
- **Repositorio:** `https://github.com/JulianSalamanca-code/ParkFlow360`
- **Descripción:** aplicación web para el control operacional de un parqueadero: registro de vehículos, espacios, tarifas, ingresos (entrada/salida de vehículos), reservas, pagos y reportes.
- **Stack real:**
  - Backend: **Java 17 + Spring Boot 3.2.5** (Spring Web, Spring Data JPA, Spring Security, Validation, JWT `jjwt 0.12.5`, Lombok).
  - Frontend: **Angular 17.3** (RxJS, Zone.js).
  - Base de datos: **PostgreSQL 15 + Flyway** (migraciones `V1`–`V8`).
  - Despliegue: **Netlify** (frontend) + **Render/Railway** (backend) + **Supabase** (PostgreSQL).
- **Módulos implementados (backend):** `vehiculos`, `espacios`, `tarifas`, `pagos`, `reportes`, `ingresos`, `security` (usuarios + autenticación JWT).
- **Módulos implementados (frontend):** los anteriores más `reservar`, `mis-vehiculos`, `mis-parqueos`, `usuarios` y `login`.
- **Dominios reales del sistema:**
  - Estado de espacio: `LIBRE`, `OCUPADO`, `RESERVADO`, `MANTENIMIENTO`.
  - Tipo de tarifa: `HORA`, `DIA`, `SEMANA`, `MES`, `MOMENTO`.
  - Método de pago: `EFECTIVO`, `TARJETA`, `TRANSFERENCIA`, `QR`.
  - Rol de usuario: `ADMIN`, `OPERADOR`, `CLIENTE`.
  - Tablas: `vehiculos`, `espacios`, `tarifas`, `pagos`, `usuarios`, `ingresos`.
- **Estado técnico actual (importante para varios documentos):**
  - **No hay pruebas automatizadas** en el repositorio (no existen `*.spec.ts` ni `*.Test.java`).
  - **No hay integración continua** (la carpeta `.github/` no tiene workflows).
  - Historial de commits con **Conventional Commits** y ramas `feat/` integradas por Pull Requests.
  - Existen `.opencode/CONVENTIONS.md` (convenciones) y `.opencode/PROJECT_STATE.md` (estado).

## Equipo (2 integrantes — no agregar a nadie más)

| Integrante | Enfoque | Roles rotativos en los documentos |
|---|---|---|
| **Julian Salamanca** | Backend / Líder técnico | Responsable del repositorio · Guardián de lo verificable |
| **Ruben Viasus** | Frontend | Redactor · Abogado del diablo |

> **Instrucción estricta:** el equipo está formado **únicamente por Ruben Viasus y Julian Salamanca**. **No menciones a Diego Barbosa** ni a ningún otro integrante en ninguna parte de los documentos. Los cuatro roles del curso (Redactor, Guardián de lo verificable, Responsable del repositorio, Abogado del diablo) se reparten entre las dos personas y **rotan**; indica la fecha de rotación.

- **Curso:** Gerencia de Software.
- **Universidad:** Universidad Santo Tomás — Facultad de Ingeniería de Sistemas.
- **Fecha base del proyecto:** 6 de octubre de 2026.

---

## Reglas de redacción (aplican a todos los documentos)

1. Responde **en español**, tono profesional, concreto y verificable.
2. **Nada de texto genérico**: cada afirmación relevante debe mencionar ParkFlow360, Angular, Spring Boot, PostgreSQL, Flyway, JWT o los módulos reales.
3. **No inventes** cifras, fechas, personas ni herramientas que no estén en el contexto. Si falta un dato, escribe `[COMPLETAR: descripción del dato]`.
4. Todo dato que se afirme medible debe poder **comprobarlo un tercero** abriendo el repositorio o ejecutando un comando.
5. Un atributo de calidad, requisito no funcional o estándar **siempre trae un "cuánto"** (un umbral numérico). Un requisito funcional (registrar, listar, crear) **no** es un atributo de calidad.
6. Cuando el documento pida **valores medidos** (métricas de calidad, cobertura, etc.), usa el formato `[MEDIR: <comando>] = ___ (fecha: ____)` en lugar de inventar el número, e incluye los comandos del **Anexo de métricas** del prompt `06_taller5_control_calidad.md`.
7. Respeta los **nombres de archivo** y los **límites de páginas** que indique cada prompt.

## Formato de salida para Word

- Entrega el contenido en Markdown con esta correspondencia:
  - `#` → Título 1
  - `##` → Título 2
  - `###` → Título 3
  - Tablas con sintaxis Markdown (se convierten en tablas de Word).
- **No uses emojis.**
- Al inicio indica el **nombre exacto del archivo**.
- Al final agrega una **lista de verificación** con casillas `[ ]` de los puntos que el docente revisará.

---

## Documentos y sus prompts

| N.º | Documento | Archivo generado |
|---:|---|---|
| 1 | Ficha de proyecto | [`docs/GS_Ficha_Proyecto_ParkFlow360.docx`](docs/GS_Ficha_Proyecto_ParkFlow360.docx) |
| 2 | Modelo de proceso | [`docs/GS_Modelo_Proceso_ParkFlow360.docx`](docs/GS_Modelo_Proceso_ParkFlow360.docx) |
| 3 | Acta de constitución (10 campos) | [`docs/GS_Acta_Constitucion_ParkFlow360.docx`](docs/GS_Acta_Constitucion_ParkFlow360.docx) |
| 4 | Estándares del equipo | [`docs/GS_Estandares_ParkFlow360.docx`](docs/GS_Estandares_ParkFlow360.docx) |
| 5 | Tablero (GitHub Projects) | [`docs/GS_Tablero_ParkFlow360.docx`](docs/GS_Tablero_ParkFlow360.docx) |
| 6 | **Taller 5 — Plan de control de calidad** | [`docs/GS_Control_De_Calidad_ParkFlow360.docx`](docs/GS_Control_De_Calidad_ParkFlow360.docx) |
| 7 | Estado real contra el acta | [`docs/GS_Estado_Real_vs_Acta_ParkFlow360.docx`](docs/GS_Estado_Real_vs_Acta_ParkFlow360.docx) |
| 8 | Backlogs estimados | [`docs/GS_Backlogs_Estimados_ParkFlow360.docx`](docs/GS_Backlogs_Estimados_ParkFlow360.docx) |

> **Alcance actualizado:** el proyecto ahora incluye **plano interactivo con generación de espacios en bloque**, **inicio de sesión con Google** y **pago en línea con Wompi**; el administrador ya no registra vehículos ni reserva (eso es función del cliente). Los documentos 1, 3, 6 y 8 ya reflejan este alcance.

---

## Orden recomendado de generación

1. **Acta de constitución** (es la base de casi todos los demás).
2. **Estándares** (aporta la Definition of Done que usa el Taller 5).
3. **Ficha de proyecto** y **Modelo de proceso**.
4. **Backlogs estimados**.
5. **Taller 5 — Plan de control de calidad** (necesita las historias y la DoD).
6. **Tablero**.
7. **Estado real contra el acta** (al final, cuando ya haya avance real).
