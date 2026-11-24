# SismosPerú — MVP (PA4)

Sistema de Gestión de Sismos del Perú en **Spring Boot 3.5 + Thymeleaf + Spring Data JPA + Spring Security + MySQL**.
Curso Desarrollo de Aplicaciones Empresariales Avanzado · NRC 3703 · Grupo 6.

## Puesta en marcha (cada integrante, una sola vez)

1. Ejecutar `database/01_schema.sql` en MySQL Workbench o DBeaver (script completo). Crea la base
   `sismos_peru`, todas las tablas y los 2 usuarios de prueba.
2. Copiar `src/main/resources/application-local.properties.example` como
   `src/main/resources/application-local.properties` y escribir su usuario y contraseña de MySQL.
   Ese archivo está en `.gitignore`: **la contraseña nunca se sube al repositorio**.
3. Abrir el proyecto en IntelliJ con SDK **JDK 21** (corretto-21) y dar a **Reload All Maven Projects**.
4. Ejecutar `SismosPeruApplication`, abrir <http://localhost:8080> e iniciar sesión como `admin`.
5. En **Importar CSV**, importar en este orden los archivos de la carpeta `datos/`:
   1. **Ubigeo oficial INEI** → `UBIGEOS_2022_1891_distritos.csv` (crea 25 departamentos,
      196 provincias y 1891 distritos: 2112 ubigeos; tarda unos segundos).
   2. **Tipos de alerta** → `tipo_alerta.csv` (4).
   3. **Tipos de daño** → `tipo_dano.csv` (7).
6. (Opcional) Ejecutar `database/02_datos_demo.sql` para cargar 10 estaciones y 30 sismos de prueba.

¿Ya tenía la base de la PA3 con datos y no quiere borrarla? Ejecute solo `database/03_seguridad.sql`,
que agrega la tabla `usuario` sin tocar lo demás.

## Usuarios de prueba (HU-19)

| Usuario | Contraseña | Rol | Qué puede hacer |
|---|---|---|---|
| `admin` | `Admin2026!` | ADMIN | Todo: registrar, editar, eliminar, cambiar estados e importar CSV |
| `consulta` | `Consulta2026!` | CONSULTA | Solo ver listas, detalles y el dashboard |

Las contraseñas están guardadas como hash **BCrypt** en la tabla `usuario`, nunca en texto plano.
Son credenciales de laboratorio, solo para la demostración.

**Reglas de acceso** (`security/SecurityConfig`):

- Sin sesión, cualquier ruta redirige a `/login`.
- Toda petición POST y las pantallas de registrar, editar, eliminar e importar exigen el rol ADMIN.
- Si el usuario CONSULTA intenta una acción de administrador, ve la página **Acceso denegado** (403, HU-37).
- En las vistas, los botones de administrador se ocultan con `sec:authorize="hasRole('ADMIN')"`.
  **Si agregan un botón que cambia datos, pónganle ese atributo** y, si es una pantalla GET nueva,
  agréguenla a `PANTALLAS_ADMIN` en `SecurityConfig`.

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
| 3 | HU-26 | Actualizar el estado de un reporte | Cristian | ✅ |
| 3 | HU-29 | Marcar un sismo como relevante para seguimiento | Cristian | ✅ |
| 3 | HU-14 | Dar seguimiento a un sismo relevante | Cristian | ✅ |
| 3 | HU-28 | Ver el total de personas afectadas por sismo | Nick | ✅ |
| 3 | HU-27 | Filtrar reportes por tipo, nivel y estado | Nick | ✅ |
| PA3 | — | Migración a Spring Boot, catálogos e importación CSV | José | ✅ |
| 4 | HU-19 | Iniciar sesión con perfiles y permisos | José | ✅ |
| 4 | HU-37 | Recibir aviso de acceso denegado | José | ✅ |
| 4 | HU-38 | Cerrar sesión | José | ✅ |
| 4 | HU-41 | Importar el catálogo oficial de ubigeos (1891 distritos) | José | ✅ |
| 4 | HU-39 | Validar el archivo CSV antes de importar | José | ✅ |
| 4 | HU-40 | Consultar los registros rechazados de una importación | José | ✅ |
| 4 | HU-16 | Ver indicadores del dashboard | Víctor | ⏳ |
| 4 | HU-17 | Ver gráficos estadísticos | Víctor | ⏳ |
| 4 | HU-42 | Elegir tamaño de página y ordenar reportes de afectación | Nick | ✅ |
| 4 | HU-24 | Ver los sismos registrados por una estación | Cristian | ⏳ |
| 4 | HU-23 | Filtrar estaciones por departamento y estado | Cristian | ⏳ |
| 4 | HU-22 | Proteger ubicaciones que están en uso | Cristian | ✅ |
| 4 | HU-18 | Visualizar sismos en un mapa | — | ✅ |
| 4 | HU-32 | Filtrar los sismos mostrados en el mapa | — | ✅ |
| 4 | HU-33 | Identificar la magnitud en el marcador | — | ✅ |
| 4 | HU-34 | Ver la ubicación del sismo en su detalle | — | ✅ |

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
├── security/     SecurityConfig (rutas y roles) y UsuarioDetailsService
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

## Guía para el sprint 4 (PA4)

- **HU-16 y HU-17 (Víctor) — Dashboard** en `/dashboard`, con el patrón de la v2.4 del profe:
  `DashboardController` → `DashboardService` → consultas en los repositorios → DTOs en `dto/reporte`
  → Thymeleaf con Chart.js. Mínimo 4 KPI, 2 gráficos y un filtro por periodo y departamento.
  Agregar "Dashboard" al menú en `fragments/layout.html`. El usuario CONSULTA también lo ve.
- **HU-42 — Reportes de afectación**: selector de 10, 25 y 50 filas y orden ascendente/descendente por
  sismo, ubicación, fecha, tipo de daño, cantidad, nivel y estado. El tamaño, orden y filtros se conservan
  al navegar entre páginas; cambiar los filtros, el tamaño o el orden vuelve a la primera página.
- **HU-18, HU-32, HU-33 y HU-34 — Mapa de sismos**: `/sismos/mapa` reutiliza los filtros por departamento,
  magnitud y fecha de HU-15, y el detalle incluye el mismo componente de mapa. Los marcadores muestran
  magnitud a una cifra decimal: verde para M < 4, ámbar para M 4–5.9 y rojo para M ≥ 6. Leaflet y el mapa
  base de OpenStreetMap se cargan desde CDN; si no hay conexión, el mapa informa que no pudo cargar y la
  aplicación permanece utilizable.
- **HU-24 y HU-23 (Cristian) — Estaciones**: sismos registrados por una estación y filtro de
  estaciones por departamento y estado, con paginación.

Reglas del equipo: una rama por HU desde `main` actualizado, Pull Request para unir, no subir
`application-local.properties`, `target/` ni `.idea/`, y sin líneas "Co-Authored-By" en los commits.

## Rutas

| Ruta | Qué hace |
|---|---|
| `/sismos` | Lista con búsqueda, filtros, orden y paginación (HU-01, 15, 30, 31) |
| `/sismos/mapa` | Mapa de sismos, filtrable por departamento, magnitud y periodo (HU-18, HU-32, HU-33) |
| `/sismos/nuevo`, `/sismos/{id}`, `/sismos/{id}/editar`, `/sismos/{id}/eliminar` | Registrar, detalle, editar, eliminar |
| `/sismos/{id}/relevante` (POST) | Marcar o quitar un sismo relevante (HU-29) |
| `/sismos/{id}/reportes/nuevo`, `/sismos/{id}/reportes/{r}/editar`, `/sismos/{id}/reportes/{r}/estado` | Registrar, editar y cambiar estado de un reporte (HU-12, HU-25, HU-26) |
| `/reportes` | Reportes de afectación con filtros por tipo, nivel y estado (HU-13, HU-27) |
| `/sismos/{id}/seguimientos/nuevo` | Registrar el seguimiento de un sismo relevante (HU-14) |
| `/ubigeos?padreId=` | Ubigeos por nivel; nuevo, editar y eliminar (HU-09) |
| `/estaciones` | Estaciones de monitoreo (HU-10, HU-11) |
| `/tipos-alerta`, `/tipos-dano` | Maestros: listar, registrar, activar o desactivar |
| `/importacion` | Importar el ubigeo oficial, `tipo_alerta.csv` y `tipo_dano.csv` (solo ADMIN) |
| `/login`, `/logout` (POST) | Iniciar y cerrar sesión |
| `/acceso-denegado` | Aviso cuando el rol no tiene permiso (403) |
