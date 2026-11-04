# SismosPerú — Sprint 3 (PA3)

Sistema de Gestión de Sismos del Perú migrado a **Spring Boot 3.5 + Thymeleaf + Spring Data JPA + MySQL**.
Curso Desarrollo de Aplicaciones Empresariales Avanzado · NRC 3703 · Grupo 6.

## Puesta en marcha (cada integrante, una sola vez)

1. Ejecutar `database/01_schema.sql` en MySQL Workbench o DBeaver (ejecutar el script completo). Crea la base `sismos_peru`.
2. Copiar `src/main/resources/application-local.properties.example` como
   `src/main/resources/application-local.properties` y escribir su usuario y contraseña de MySQL.
   Ese archivo está en `.gitignore`: **la contraseña nunca se sube al repositorio**.
3. Abrir el `pom.xml` en IntelliJ (Open as Project), con SDK **JDK 21** (corretto-21), y dar a
   **Reload All Maven Projects**.
4. Ejecutar `SismosPeruApplication` y abrir <http://localhost:8080>.
5. En **Importar CSV**, importar en este orden los archivos de la carpeta `datos/`:
   `ubigeo.csv` (25), `tipo_alerta.csv` (4) y `tipo_dano.csv` (7).
6. (Opcional) Ejecutar `database/02_datos_demo.sql` para cargar provincias, distritos, 10 estaciones
   y 30 sismos de prueba. Requiere haber importado los CSV del paso 5.

## Estado de las historias de usuario

| Sprint | HU | Historia | Responsable | Estado |
|---|---|---|---|---|
| 1 | HU-01 | Listar sismos | Víctor | ✅ |
| 1 | HU-02 | Registrar un nuevo sismo | Nick | ✅ |
| 1 | HU-03 | Validar campos obligatorios y código único | José | ✅ |
| 1 | HU-04 | Validar magnitud, profundidad, coordenadas y fecha | Cristian | ✅ |
| 1 | HU-08 | Gestionar el estado del evento | Cristian | ✅ |
| 1 | HU-05 | Ver el detalle de un sismo | José | ✅ |
| 1 | HU-06 | Editar un sismo | Nick | ✅ |
| 1 | HU-07 | Eliminar un sismo con confirmación | Víctor | ✅ |
| 2 | HU-15 | Buscar, filtrar, ordenar y paginar sismos | José | ✅ |
| 2 | HU-09 | Gestionar ubicaciones geográficas (ubigeo) | José | ✅ |
| 2 | HU-21 | Seleccionar la ubicación en cascada | Víctor | ✅ |
| 2 | HU-10 | Gestionar estaciones de monitoreo | Víctor | ✅ |
| 2 | HU-11 | Asociar un sismo a la estación que lo registró | Cristian | ✅ |
| 2 | HU-30 | Filtrar sismos por estado del evento | Cristian | ✅ |
| 2 | HU-31 | Buscar un sismo por código o referencia geográfica | Nick | ✅ |
| 3 | HU-12 | Registrar reportes de afectación (daño) | Víctor | ✅ |
| 3 | HU-13 | Consultar reportes de afectación | Víctor | ✅ |
| 3 | HU-25 | Editar un reporte de afectación | Víctor | ✅ |
| 3 | HU-26 | Actualizar el estado de un reporte | Cristian | ⏳ |
| 3 | HU-29 | Marcar un sismo como relevante para seguimiento | Cristian | ⏳ |
| 3 | HU-14 | Dar seguimiento a un sismo relevante | Cristian | ⏳ |
| 3 | HU-28 | Ver el total de personas afectadas por sismo | Nick | ✅ |
| 3 | HU-27 | Filtrar reportes por tipo, nivel y estado | Nick | ✅ |
| PA3 | — | Migración a Spring Boot, catálogos e importación CSV | José | ✅ |

## Modelo de datos

```
Ubigeo      1 ── N  Sismo           (cada sismo en un ubigeo ACTIVO)
TipoAlerta  1 ── N  Sismo           (cada sismo con una alerta ACTIVA)
Sismo       1 ── N  ReporteDano     (cantidad afectada, descripción, nivel, estado)
TipoDano    1 ── N  ReporteDano     (cada reporte con un tipo de daño ACTIVO)
Ubigeo      1 ── N  EstacionMonitoreo,  EstacionMonitoreo 1 ── N Sismo (opcional)
Sismo       1 ── N  SeguimientoSismo
Ubigeo      1 ── N  Ubigeo          (departamento > provincia > distrito, por padre_id)
```

Las tablas las crean los scripts de `database/`; Hibernate solo las valida (`ddl-auto=validate`).
Si una HU necesita una columna nueva, se agrega en `01_schema.sql` **y** en la entidad.

## Estructura

```
com.grupo6.sismos
├── controller/   un @Controller por módulo; sin reglas de negocio
├── service/      reglas de negocio y @Transactional
├── repository/   interfaces JpaRepository (+ SismoEspecificaciones para la búsqueda)
├── entity/       entidades JPA
├── importacion/  lectura de CSV y resultado de la importación
└── web/
    ├── form/     formularios con Bean Validation (@NotBlank, @Positive, ...)
    └── error/    ReglaNegocioException, RecursoNoEncontradoException, ManejadorErrores
templates/<módulo>/lista.html, formulario.html, detalle.html
```

## Cómo se hace una HU (patrón del proyecto)

1. **Form** en `web/form` con Bean Validation para los datos de entrada.
2. **Service** con `@Transactional`: reglas de negocio. Si una regla falla, lanza
   `new ReglaNegocioException("campo", "mensaje")` (campo `null` si es un mensaje general).
3. **Controller**: `@Valid @ModelAttribute` + `BindingResult`. Si no hay errores llama al servicio;
   si el servicio lanza `ReglaNegocioException`, `Errores.registrar(errores, error)` la muestra en el campo.
   Después de guardar, `redirect` con `addFlashAttribute("exito", ...)`.
4. **Vista** Thymeleaf usando `fragments/layout` (`head`, `barra`, `mensajes`, `erroresFormulario`).

Ejemplos completos: `SismoController` + `SismoService` + `SismoForm` + `templates/sismos/`.

## Guía para el sprint 3

Ya existen la tabla, la entidad y el repositorio de **ReporteDano** y **SeguimientoSismo**, y la columna
`sismo.relevante`. Falta construir las pantallas:

Orden sugerido (por dependencias): primero HU-12; luego HU-13, HU-25, HU-26 y HU-28; al final HU-27.
HU-29 y HU-14 no dependen de los reportes y se pueden hacer en paralelo.

- **HU-12 (Víctor):** `ReporteDanoForm` + `ReporteDanoService.registrar` + `ReporteDanoController`.
  Registrar desde el detalle del sismo (`/sismos/{id}/reportes/nuevo`). Reglas: el tipo de daño debe
  estar ACTIVO (`TipoDanoService.activos()` para el select), cantidad afectada ≥ 0, descripción obligatoria.
- **HU-13 (Víctor):** listado de reportes (general y en el detalle del sismo, donde está el comentario
  `Sprint 3` en `sismos/detalle.html`). `ReporteDanoRepository.findBySismoIdOrderByFechaReporteDesc`.
- **HU-25 (Víctor):** editar un reporte reutilizando el formulario de HU-12.
- **HU-26 (Cristian):** cambiar el estado del reporte (PENDIENTE → VERIFICADO → CERRADO; un reporte
  CERRADO ya no se modifica).
- **HU-29 (Cristian):** marcar o desmarcar `relevante` desde el detalle del sismo.
- **HU-14 (Cristian):** bitácora de seguimiento de un sismo relevante (`SeguimientoSismo`).
- **HU-28 (Nick):** total de afectados en el detalle del sismo (consulta `SUM(cantidadAfectada)`).
- **HU-27 (Nick):** filtros de reportes por tipo de daño, nivel y estado.

Reglas del equipo: una rama por HU desde `main` actualizado, Pull Request para unir, no subir
`application-local.properties`, `target/` ni `.idea/`.

## Rutas

| Ruta | Qué hace |
|---|---|
| `/sismos` | Lista con búsqueda, filtros, orden y paginación (HU-01, 15, 30, 31) |
| `/sismos/nuevo`, `/sismos/{id}`, `/sismos/{id}/editar`, `/sismos/{id}/eliminar` | Registrar, detalle, editar, eliminar |
| `/ubigeos?padreId=` | Ubigeos por nivel; nuevo, editar y eliminar (HU-09) |
| `/estaciones` | Estaciones de monitoreo (HU-10, HU-11) |
| `/tipos-alerta`, `/tipos-dano` | Maestros: listar, registrar, activar o desactivar |
| `/importacion` | Importar `ubigeo.csv`, `tipo_alerta.csv` y `tipo_dano.csv` |
