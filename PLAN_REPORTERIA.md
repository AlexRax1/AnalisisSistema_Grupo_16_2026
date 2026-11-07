# Plan de Implementación — Reporte de muestra y bitácora

> Documento vivo. Debe guardarse después de cada tarea o fase.
> Ubicación: `PLAN_REPORTERIA.md` (raíz del proyecto).
> Alcance: **un solo reporte de muestra para el módulo de administrador**, para probar filtros y gráficos; no se implementan los reportes oficiales todavía.
> Meta: dejar la bitácora verificada y una base funcional en **90 minutos**, lista para adaptar el reporte cuando se conozca el tema real.

---

## Estado Actual

- **Avance global:** `88%` — `76 de 86 casillas` (Fases 0–7 completas; plantilla congelada y exportaciones con gráficos; dashboard en tiempo real agregado; quedan las tareas de adaptación al tema real).
- **Fases completadas:** `Fase 0 — Verificación previa`, `Fase 1 — Bitácora`, `Fase 2 — Verificación de bitácora`, `Fase 3 — Contrato`, `Fase 4 — SQL/opciones`, `Fase 5 — Backend/exportaciones`, `Fase 6 — Frontend/gráficos`, `Fase 7 — Página admin y pruebas`, `Fase 8.1 — Plantilla congelada`, `Fase 8.2 — Dashboard en tiempo real`.
- **Fase en curso:** ninguna; `Fase 8 — Adaptación al tema real` queda lista para ejecutarse cuando se conozca el reporte solicitado.
- **Pendientes:** sólo la adaptación al tema real y las tareas opcionales de la Fase 8.
- **Estado:** MVP completo y verificado con PostgreSQL. Backend y frontend arrancan; `POST /admin/reportes/quejas/consultar` devuelve KPIs, series, tabla y detalle; Excel/PDF son válidos e incluyen gráficos de barras y pastel; la pantalla muestra barras, pastel y barras apiladas; B1–B11 pasan; cada consulta/exportación deja fila en `bitacora`. Se corrigió un bug real de la consulta de bitácora con parámetros nulos. El dashboard quedó como módulo independiente (`com.muni.backend.dashboard` y `modules/dashboard`) con `GET /admin/dashboard`, exportación PDF/Excel auditada, contadores en vivo, dona de distribución y código de colores.
- **Plantilla congelada:** el reporte de quejas queda como **ejemplo canónico**. Para un reporte nuevo se edita el dataset/filtros, no se duplica la pantalla ni los exportadores. Ver sección 7.
- **Última actualización:** `2026-11-07`.
- **Tiempo objetivo del MVP:** `90 min` (sin contar la Fase 1, ya implementada).
- **Regla de persistencia:** al terminar cada tarea, marcar `[x]`, actualizar este bloque y guardar el archivo. Si se interrumpe, se retoma desde la primera casilla sin marcar.

### Decisiones de alcance vigentes (2026-11-04)

- **Sin índices ni migraciones por ahora.** Es un proyecto local de alcance corto; si una consulta se vuelve lenta, se mide y se agrega sólo el índice necesario.
- **Sin auditoría de intentos fallidos ni resultado éxito/fallo.** Se considera fuera del alcance; la bitácora registra acciones exitosas.
- **Sin zonas/localidades.** No se usa `zona`. Si más adelante se pide dividir por localidad, se hará con un módulo aparte basado en coordenadas.
- **Sin entidad “cuadrilla”.** El trabajo se asigna con `dependencia` y `especialista`; las cuadrillas no se modelaron.
- **Materiales en texto libre.** No se normalizan ni se reporta consumo; eso requeriría un módulo de inventario.
- **La consulta de bitácora ya existe.** Se implementó el endpoint; la pantalla de bitácora queda como extensión opcional.

### Cómo leer este plan

- `[ ]` pendiente.
- `[x]` completado y verificado.
- `[~]` en curso.
- `[!]` bloqueado (anotar el motivo en el Registro de ejecución).
- **MVP:** imprescindible para la demostración de 90 minutos.
- **Opcional:** sólo si sobra tiempo; no bloquea la entrega.

### Ruta rápida de 90 minutos

| Orden | Fase | Tiempo | ¿MVP? |
|---|---|---:|---|
| 1 | Fase 0 — Verificación previa | 5 min | Sí |
| 2 | Fase 2 — Verificación de bitácora | 10 min | Sí |
| 3 | Fase 3 — Contrato del reporte de muestra | 10 min | Sí |
| 4 | Fase 4 — Consultas SQL y opciones | 15 min | Sí |
| 5 | Fase 5 — Servicio, controller y exportaciones | 20 min | Sí |
| 6 | Fase 6 — Frontend: modelos, servicio y gráficos | 15 min | Sí |
| 7 | Fase 7 — Página admin y pruebas | 15 min | Sí |
| — | Fase 1 — Consulta de bitácora | 0 min | Ya implementada |
| — | Fase 8 — Adaptación al tema real | variable | No |

> La Fase 1 ya está implementada y no consume tiempo del MVP. Si el tiempo se agota, el primer recorte es el PDF; Excel y la consulta siguen funcionando.

---

## 1. Resumen de la revisión

### 1.1 Qué ya cubre la bitácora

La bitácora actual registra correctamente las acciones críticas del negocio:

| Acción | Tabla afectada | ¿Se registra? | Observación |
|---|---|---|---|
| Login exitoso | `credenciales` | Sí | `accion = LOGIN` |
| Registro de ciudadano | `credenciales`, `usuarios` | Sí | Doble registro trazable |
| Actualización de perfil | `usuarios`, `credenciales` | Sí | Un registro por campo modificado |
| Recuperación de contraseña | `recuperacion_password`, `credenciales` | Sí | No expone el hash |
| Registro de queja | `quejas` | Sí | Incluye correlativo |
| Agravamiento / reincidencia | `quejas` | Sí | Incluye queja origen |
| Carga de evidencias | `evidencias_digitales` | Sí | Ciudadano, inspector y especialista |
| Asignación de funcionario | `quejas` | Sí | En asignación automática |
| Asignación de inspector | `quejas` | Sí | Incluye estado |
| Autorización de reparación | `quejas` | Sí | Especialista y dependencia |
| Cambio de estado | `quejas` | Sí | Estado anterior y nuevo |
| Rechazo / devolución | `quejas` | Sí | Motivo en texto |
| Informe de inspección | `informes_queja` | Sí | Creación |
| Informe de solución técnica | `informes_queja` | Sí | Creación |
| Cierre administrativo | `informes_queja` | Sí | Creación |
| Cierre de sesión | `credenciales` | Sí | `accion = CERRAR_SESION`; el frontend llama a `POST /auth/logout` |
| Descarga de constancia | `quejas` | Sí | `accion = DESCARGAR` |
| Exportación de catálogo | `quejas` | Sí | PDF y Excel |
| Exportación de reportes | `reportes` | Sí | PDF/Excel con `accion = DESCARGAR` |
| Gráficos de reporte | — | No aplica | Son representaciones de los datos; no generan fila propia |

### 1.2 Qué falta o está débil

| Brecha | Impacto en reportes | Acción en este plan |
|---|---|---|
| No había endpoint de lectura de bitácora | La bitácora no se podía consultar | **Fase 1 — implementado** |
| Consulta y exportación de reportes no se registran | No hay auditoría de quién consultó qué reporte | **Fase 5** |
| No hay filtros `idAfectado` ni `campoAfectado` | Rastrear una queja o un campo exige revisar la respuesta | **Fase 2 — evaluar; no bloquea** |
| No hay verificación explícita de cobertura | No se sabe si la bitácora alcanza para el reporte real | **Fase 2 — B1 a B11** |
| No hay catálogo documentado de acciones | Dificulta filtros y reportes de auditoría | **Anexo A** |
| No se registran intentos fallidos ni resultado éxito/fallo | Fuera del alcance actual | **No se implementa** |
| El logout ya se registra; no se calcula duración de sesión | No se mide tiempo conectado | **Fuera de alcance** |
| Las exportaciones no incluían los gráficos | El PDF/Excel perdía la lectura visual de la pantalla | **Corregido:** se incrustan barras y pastel en PDF y Excel |
| `BitacoraService.registrar` puede revertirse con la transacción principal | Pérdida de auditoría si falla el negocio | Se acepta en el MVP; se evalúa `registrarSeguro` sólo si aparece el problema |

### 1.3 Datos disponibles para reportes

| Dimensión | Fuente | ¿Sirve? | Nota |
|---|---|---|---|
| Fecha de registro | `quejas.fecha_registro` | Sí | Base para series temporales |
| Fecha de cierre | `quejas.fecha_cierre` | Sí | Tiempo de resolución |
| Estado actual | `quejas.estado_actual` | Sí | 7 estados |
| Historial de estados | `historial_estados_queja` | Sí | Tiempos por etapa y re-trabajos |
| Categoría / subcategoría | `categorias_queja`, `subcategorias_queja` | Sí | 4 categorías base |
| Zona | `quejas.zona` | No | No se usa; no incluir en el MVP. Para localidad usar coordenadas en un módulo aparte |
| Dirección exacta | `quejas.direccion_exacta` | Sí, texto libre | Sirve para listado, no para agrupar |
| Latitud / longitud | `quejas.latitud`, `quejas.longitud` | Sí | Base para mapa de calor |
| Prioridad | `quejas.prioridad_confirmada` | Sí | BAJA, MEDIA, ALTA, URGENTE |
| Tipo de registro | `quejas.tipo_registro` | Sí | PRINCIPAL, AGRAVAMIENTO, REINCIDENCIA |
| Funcionario / inspector / especialista | `quejas.*_id` | Sí | Carga y asignaciones |
| Dependencia | `quejas.dependencia_asignada_id` | Sí | Se usa como aproximación de cuadrilla; no hay entidad “cuadrilla” |
| Informes | `informes_queja` | Sí | Inspección, solución, cierre |
| Horas trabajadas | `informes_queja.horas_trabajadas` | Sí | Promedios por especialista |
| Materiales | `informes_queja.materiales_utilizados` | No | Texto libre; no se reporta consumo |
| Dictamen de calidad | `informes_queja.dictamen_calidad` | No confiable | El flujo actual no lo registra |
| Evidencias | `evidencias_digitales` | Sí | Conteo por etapa |
| Usuarios / roles | `usuarios`, `credenciales`, `rol_user` | Sí | Reporte de usuarios por rol o dependencia |
| Localidad de usuario | — | No | No existe; si se pide, usar coordenadas en un módulo aparte |
| Bitácora | `bitacora` | Sí | Actividad y auditoría; ya existe endpoint de consulta |

### 1.4 Limitaciones que se deben explicar

1. **No hay localidad/municipio normalizado.** El proyecto maneja una sola municipalidad y la ubicación de la queja es texto libre + latitud/longitud. Si se pide “por localidad”, se hará un módulo aparte con coordenadas; no se agrega al MVP.
2. **La zona está en desuso.** El formulario ciudadano envía `zona = 1` por compatibilidad. No se incluye en filtros ni agrupaciones; no se invierte tiempo en ella.
3. **No existe entidad “cuadrilla”.** Se usa `dependencia` o `especialista` como aproximación. Las cuadrillas se registraron como texto.
4. **No se registra dictamen de conformidad.** El flujo actual no tiene la segunda validación del inspector; se deja como extensión sólo si se solicita.
5. **Los materiales son texto libre.** No se puede sumar “cuántos postes” o “cuántos metros de cable”. No se normalizan; eso requeriría un módulo de inventario.
6. **No hay marcas de tiempo de asignación independientes.** Se pueden inferir del historial de estados.
7. **El rol `FUNCIONARIO_MUNICIPAL` no coincide con el rol que espera el frontend (`FUNCIONARIO`).** Para la demo se usará el usuario administrador. La normalización queda como extensión.
8. **No se auditan intentos fallidos ni resultado éxito/fallo.** Se considera fuera del alcance; no se promete en la demo.
9. **No se crean índices.** Es un proyecto local de alcance corto; si una consulta se vuelve lenta, se mide y se agrega sólo el índice necesario.

---

## 2. Decisiones de arquitectura

1. **Módulo backend separado**, siguiendo el patrón simple de `bitacora`:
   - Backend: `com.muni.backend.reportes`.
   - Frontend: se reutiliza el módulo existente `frontend/src/app/modules/admin/pages/admin-reportes`. No se crea un módulo frontend nuevo.
2. **Un solo reporte de muestra** que responde a “Quejas por categoría” y permite cambiar la agrupación:
   - Es una sola página y un solo endpoint; el selector de agrupación no crea reportes separados.
   - Filtros: rango de fechas, estado, categoría, prioridad y agrupación.
   - Salida: KPIs, gráfico de barras, gráfico de pastel, gráfico de barras apiladas, tabla agrupada y detalle.
   - Exportación: PDF y Excel con barras y pastel incrustados.
3. **Sin Chart.js.** Los gráficos se hacen con HTML/CSS (barras y apilado) y `conic-gradient` (pastel). Las exportaciones usan Java2D para generar PNG sin dependencias externas.
4. **Sin tabla nueva de reportes.** La auditoría de consultas/exportaciones usa la bitácora existente.
5. **Una consulta SQL genérica con `CASE`** para agrupar por dimensión. Evita SQL dinámico y facilita la explicación.
6. **Reutilización del catálogo de quejas** para el detalle, en lugar de duplicar filtros.
7. **No se implementan los reportes oficiales todavía.** Sólo el reporte de muestra; la adaptación al tema real queda en Fase 8.
8. **Sin índices, sin zonas y sin auditoría de fallos.** Se prioriza que el reporte funcione y sea explicable; el rendimiento y la seguridad se amplían sólo si se piden.
9. **La bitácora se consulta con `GET /admin/bitacora`.** No se crea una pantalla de bitácora en el MVP; queda como extensión.
10. **Consultas y exportaciones por `POST`.** Los filtros viajan en JSON, no en la URL. `GET` queda para opciones y bitácora.
11. **Módulo sólo para administrador.** La ruta es `/admin/reportes` y el backend `/admin/reportes/**`.

---

## 3. Arquitectura objetivo

> Organización por módulo, igual que el proyecto actual: cada paquete tiene `controller`, `dto`, `model`, `repository` y `service`. No se crea un módulo `reportes` genérico con decenas de responsabilidades; se crea un paquete pequeño para **un solo reporte de muestra**.

### 3.1 Backend

```text
backend/src/main/java/com/muni/backend/
├── bitacora/
│   ├── controller/
│   │   └── BitacoraController.java                 (existente; GET de consulta ya implementado)
│   ├── dto/
│   │   ├── BitacoraDTO.java                        (existente)
│   │   └── BitacoraRespuestaDTO.java               (nuevo, ya implementado)
│   ├── model/
│   │   └── Bitacora.java                           (existente; sin cambios)
│   ├── repository/
│   │   └── BitacoraRepository.java                 (existente + buscarConFiltros)
│   └── service/
│       ├── BitacoraService.java                    (existente + listar)
│       └── ClientIpService.java                    (existente)
│
├── quejas/                                         (existente; no se modifica)
│   ├── controller/
│   │   ├── AdminQuejaController.java
│   │   ├── QuejaController.java
│   │   ├── FuncionarioController.java
│   │   ├── InspectorController.java
│   │   └── EspecialistaController.java
│   ├── dto/
│   │   ├── ciudadano/
│   │   ├── especialista/
│   │   ├── funcionario/
│   │   ├── inspector/
│   │   └── shared/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   │   └── QuejaRepository.java                    (se reutiliza para el detalle)
│   └── service/
│
├── security/
│   └── config/
│       └── SecurityConfig.java                     (modificar: reglas para /admin/reportes/** y /admin/dashboard/**)
│
├── usuarios/                                       (existente; se reutiliza para opciones)
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
│
├── dashboard/                                      (nuevo módulo independiente del dashboard)
│   ├── controller/
│   │   └── DashboardController.java                (GET dashboard + exportar PDF/Excel)
│   ├── dto/
│   │   ├── DashboardContadorDTO.java
│   │   ├── DashboardEstadoDTO.java
│   │   ├── DashboardQuejaDTO.java
│   │   └── DashboardResponseDTO.java
│   ├── model/
│   │   └── GrupoEstadoDashboard.java
│   ├── repository/
│   │   ├── DashboardRepository.java
│   │   └── proyeccion/
│   │       └── DashboardEstadoProjection.java
│   └── service/
│       ├── DashboardService.java                   (agrupación, porcentajes y recientes)
│       └── DashboardExportService.java             (PDF/Excel simples, sin depender de reportes)
│
└── reportes/                                       (nuevo, un solo reporte de muestra)
    ├── controller/
    │   └── ReporteQuejaController.java             (POST consultar y exportar; GET opciones)
    ├── dto/
    │   ├── ReporteFiltroDTO.java
    │   ├── ReporteResponseDTO.java
    │   ├── ReporteKpiDTO.java
    │   ├── ReporteSerieDTO.java
    │   ├── ReporteFilaDTO.java
    │   ├── ReporteTablaDTO.java
    │   ├── ReporteDetalleDTO.java
    │   ├── ReporteOpcionesDTO.java
    │   └── OpcionDTO.java
    ├── model/
    │   └── ReporteAgrupacion.java
    ├── repository/
    │   ├── ReporteQuejaRepository.java
    │   └── proyeccion/
    │       ├── ReporteAgrupadoProjection.java
    │       └── ReporteKpiProjection.java
    ├── service/
    │   ├── ReporteQuejaService.java
    │   ├── ReporteChartService.java                (genera PNG de barras/pastel con Java2D)
    │   └── ReporteExportService.java               (PDF/Excel con gráficos incrustados)
    └── exception/
        └── ReporteExceptionHandler.java
```

### 3.2 Frontend

```text
frontend/src/app/
├── core/
│   ├── models/
│   │   └── reporte-queja.model.ts                  (nuevo)
│   └── services/
│       └── reporte-queja.service.ts                (nuevo)
│
├── modules/admin/
│   ├── components/
│   │   ├── grafico-barras/
│   │   │   ├── grafico-barras.ts
│   │   │   ├── grafico-barras.html
│   │   │   └── grafico-barras.css
│   │   ├── grafico-pastel/
│   │   │   ├── grafico-pastel.ts
│   │   │   ├── grafico-pastel.html
│   │   │   └── grafico-pastel.css
│   │   └── grafico-apilado/
│   │       ├── grafico-apilado.ts
│   │       ├── grafico-apilado.html
│   │       └── grafico-apilado.css
│   └── pages/admin-reportes/
│       ├── admin-reportes.ts                       (reemplazar placeholder)
│       ├── admin-reportes.html                     (reemplazar placeholder)
│       └── admin-reportes.css                      (reemplazar placeholder)
│
├── modules/dashboard/                              (nuevo módulo independiente del dashboard)
│   ├── components/grafico-distribucion/
│   │   ├── grafico-distribucion.ts                 (dona con conic-gradient y leyenda)
│   │   ├── grafico-distribucion.html
│   │   └── grafico-distribucion.css
│   ├── models/
│   │   └── dashboard.model.ts
│   ├── pages/admin-dashboard/
│   │   ├── admin-dashboard.ts                      (polling 15 s, exportaciones)
│   │   ├── admin-dashboard.html
│   │   └── admin-dashboard.css
│   ├── services/
│   │   └── dashboard.service.ts                    (GET + PDF + Excel)
│   └── dashboard.routes.ts                         (rutas lazy del módulo)
│
├── app.routes.ts                                   (/admin/reportes y /admin/dashboard lazy)
└── shared/layouts/dashboard-layout/dashboard-layout.ts
                                                    (ya tiene los menús Reportes y Dashboard; no tocar)
```

### 3.3 Persistencia

- No se crea tabla nueva.
- No se crea `sql/migracion_reporteria.sql`.
- No se crean índices.
- El reporte de muestra lee de `quejas`, `categorias_queja`, `subcategorias_queja`, `dependencias` y `usuarios`.
- La bitácora sigue siendo la única tabla de auditoría.

---

## 4. Contrato de API

### 4.0 ¿GET o POST?

- **GET** manda los filtros en la URL. Es lo correcto para consultas simples, pero los datos quedan visibles en el historial, logs y proxies; además la URL tiene límite de longitud.
- **POST** manda los filtros en un JSON dentro del cuerpo. No oculta la información por cifrado, pero evita que los filtros viajen en la URL. Es útil para reportes con muchos filtros o para que el usuario no vea un “chorizo” de parámetros.
- **Decisión para este proyecto:** usar `POST` para consultar y exportar el reporte; usar `GET` para `opciones` y para la consulta de bitácora (son lecturas simples).

| Operación | Método | Ruta |
|---|---|---|
| Consultar reporte | `POST` | `/admin/reportes/quejas/consultar` |
| Opciones de filtros | `GET` | `/admin/reportes/opciones` |
| Exportar Excel | `POST` | `/admin/reportes/quejas/exportar/excel` |
| Exportar PDF | `POST` | `/admin/reportes/quejas/exportar/pdf` |
| Consultar bitácora | `GET` | `/admin/bitacora` |

### 4.1 Consultar reporte

```http
POST /admin/reportes/quejas/consultar
Content-Type: application/json
Authorization: Bearer <token>

{
  "fechaDesde": "2026-01-01",
  "fechaHasta": "2026-10-31",
  "estado": "TODAS",
  "categoriaId": null,
  "prioridad": "TODAS",
  "agrupacion": "CATEGORIA"
}
```

Campos del cuerpo:

| Campo | Tipo | Obligatorio | Valores |
|---|---|---|---|
| `fechaDesde` | `YYYY-MM-DD` | No | Por defecto: primer día del mes actual |
| `fechaHasta` | `YYYY-MM-DD` | No | Por defecto: hoy |
| `estado` | texto | No | `TODAS` o un estado válido |
| `categoriaId` | entero | No | `null` o ID de categoría |
| `prioridad` | texto | No | `TODAS`, `BAJA`, `MEDIA`, `ALTA`, `URGENTE` |
| `agrupacion` | texto | No | `CATEGORIA` por defecto; también `ESTADO`, `SUBCATEGORIA`, `PRIORIDAD`, `TIPO_REGISTRO`, `DEPENDENCIA`, `ESPECIALISTA`, `INSPECTOR` |

Respuesta:

```json
{
  "titulo": "Reporte de quejas por categoría",
  "subtitulo": "Periodo: 01/01/2026 - 31/10/2026",
  "fechaEmision": "2026-11-04T10:30:00",
  "filtrosAplicados": {
    "Fecha desde": "01/01/2026",
    "Fecha hasta": "31/10/2026",
    "Estado": "Todas",
    "Categoría": "Todas",
    "Prioridad": "Todas",
    "Agrupación": "Categoría"
  },
  "kpis": [
    { "etiqueta": "Total quejas", "valor": "1,247", "icono": "description", "tono": "primary" },
    { "etiqueta": "Resueltas", "valor": "1,089", "icono": "check_circle", "tono": "success" },
    { "etiqueta": "En proceso", "valor": "134", "icono": "pending", "tono": "warning" },
    { "etiqueta": "Rechazadas", "valor": "24", "icono": "cancel", "tono": "danger" },
    { "etiqueta": "Tasa de resolución", "valor": "87.3%", "icono": "percent", "tono": "info" },
    { "etiqueta": "Días promedio de cierre", "valor": "4.2 días", "icono": "timer", "tono": "neutral" }
  ],
  "series": [
    { "etiqueta": "Alumbrado Público", "valor": 423, "porcentaje": 33.9 },
    { "etiqueta": "Drenajes y Alcantarillado", "valor": 312, "porcentaje": 25.0 }
  ],
  "tabla": {
    "titulo": "Resumen por categoría",
    "columnas": ["Dimensión", "Total", "Resueltas", "En proceso", "Rechazadas", "%"],
    "filas": [
      {
        "etiqueta": "Alumbrado Público",
        "total": 423,
        "cerradas": 380,
        "enProceso": 35,
        "rechazadas": 8,
        "porcentaje": 33.9
      }
    ]
  },
  "detalle": [
    {
      "quejaId": 101,
      "correlativo": "QUE-2026-000101",
      "fechaRegistro": "2026-10-30T08:15:00",
      "ciudadano": "María López",
      "dpi": "1234567890101",
      "categoria": "Drenajes y Alcantarillado",
      "subcategoria": "Tragante obstruido",
      "estadoActual": "EN INSPECCIÓN",
      "prioridad": "ALTA",
      "inspector": "Carlos Gómez",
      "especialista": "Sin asignar",
      "dependencia": "Fontanería y Drenajes",
      "fechaCierre": null,
      "direccionExacta": "3a calle, barrio El Centro"
    }
  ]
}
```

### 4.2 Opciones de filtros

```http
GET /admin/reportes/opciones
Authorization: Bearer <token>
```

```json
{
  "categorias": [{ "id": 1, "nombre": "Alumbrado Público" }],
  "subcategorias": [{ "id": 3, "nombre": "Tragante obstruido", "categoriaId": 2 }],
  "dependencias": [{ "id": 1, "nombre": "Dirección de Servicios Públicos" }],
  "especialistas": [{ "id": 8, "nombre": "Jorge Morales" }],
  "estados": ["REGISTRADA", "EN INSPECCIÓN", "EN REPARACIÓN TÉCNICA"],
  "prioridades": ["BAJA", "MEDIA", "ALTA", "URGENTE"],
  "agrupaciones": ["CATEGORIA", "ESTADO", "SUBCATEGORIA", "PRIORIDAD", "DEPENDENCIA", "ESPECIALISTA", "INSPECTOR"]
}
```

Este endpoint es el que deja “listos” los filtros del punto 4.2. Se llena con los repositorios existentes: `CategoriaQuejaRepository`, `SubcategoriaQuejaRepository`, `DependenciaRepository` y usuarios activos por rol.

> El formulario del reporte de muestra usará fecha, estado, categoría, prioridad y agrupación. `dependencias` y `especialistas` quedan disponibles para la adaptación de la Fase 8 sin cambiar el endpoint.

### 4.3 Exportaciones

```http
POST /admin/reportes/quejas/exportar/excel
POST /admin/reportes/quejas/exportar/pdf
Content-Type: application/json
Authorization: Bearer <token>
```

El cuerpo es el mismo de la sección 4.1.

- `Content-Type`: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` / `application/pdf`.
- `Content-Disposition`: `attachment; filename="Reporte_Quejas_20261104.xlsx"` / `.pdf`.
- En Angular se descarga con `HttpClient.post(..., { responseType: 'blob' })`.

### 4.4 Consulta de bitácora (implementada)

```http
GET /admin/bitacora?usuario=admin&tabla=quejas&accion=CONSULTAR&fechaInicio=2026-01-01&fechaFin=2026-12-31&pagina=0&tamanoPagina=20
Authorization: Bearer <token>
```

| Parámetro | Tipo | Obligatorio | Nota |
|---|---|---|---|
| `usuario` | texto | No | Búsqueda parcial por `usuarioAccion` |
| `tabla` | texto | No | Coincidencia exacta con `tablaAfectada` |
| `accion` | texto | No | Coincidencia exacta con `accion` |
| `fechaInicio` | `YYYY-MM-DD` | No | Desde el inicio del día |
| `fechaFin` | `YYYY-MM-DD` | No | Hasta el final del día |
| `pagina` | entero | No | Base 0; por defecto `0` |
| `tamanoPagina` | entero | No | Por defecto `20`; máximo `100` |

La respuesta es un `Page<BitacoraRespuestaDTO>` con `content`, `totalElements`, `totalPages`, `number` y `size`. No se crea pantalla de bitácora en el MVP.

> Filtros `idAfectado` y `campoAfectado` no están implementados todavía. Se evaluarán en la Fase 2 sólo si la verificación demuestra que hacen falta.

### 4.5 Auditoría del reporte

Cuando el reporte consulte o exporte, debe dejar una fila en `bitacora`:

| Campo | Consulta | Exportación |
|---|---|---|
| `tablaAfectada` | `reportes` | `reportes` |
| `idAfectado` | `quejas` | `quejas` |
| `campoAfectado` | `consulta` | `exportacion_excel` / `exportacion_pdf` |
| `accion` | `CONSULTAR` | `DESCARGAR` |
| `registroNuevo` | JSON con fechas, estado, categoría, prioridad y agrupación | El mismo JSON + formato |

Esto permite verificar después quién consultó o descargó un reporte y con qué filtros.

### 4.6 Dashboard en tiempo real

```http
GET /admin/dashboard?limiteRecientes=10
GET /admin/dashboard/exportar/pdf
GET /admin/dashboard/exportar/excel
Authorization: Bearer <token>
```

El dashboard vive en un módulo propio (`com.muni.backend.dashboard` en backend y
`frontend/src/app/modules/dashboard` en frontend) para no mezclarlo con el reporte de muestra.
Comparte únicamente el código de colores `estado-queja.util.ts`.

`GET /admin/dashboard` devuelve una foto del estado actual de todas las quejas. No audita en
`bitacora` porque el frontend lo consulta cada 15 segundos y llenaría la tabla de ruido.
Las exportaciones sí se auditan con `tablaAfectada = dashboard`, `accion = DESCARGAR`.

```json
{
  "fechaActualizacion": "2026-11-07T11:45:00",
  "total": 16,
  "contadores": [
    { "clave": "NUEVOS", "etiqueta": "Nuevos", "valor": 5, "porcentaje": 31.3, "icono": "fiber_new" },
    { "clave": "EN_PROCESO", "etiqueta": "En proceso", "valor": 6, "porcentaje": 37.5, "icono": "autorenew" },
    { "clave": "PENDIENTES", "etiqueta": "Pendientes", "valor": 1, "porcentaje": 6.3, "icono": "pending_actions" },
    { "clave": "RESUELTOS", "etiqueta": "Resueltos", "valor": 2, "porcentaje": 12.5, "icono": "check_circle" },
    { "clave": "RECHAZADOS", "etiqueta": "Rechazadas", "valor": 2, "porcentaje": 12.5, "icono": "cancel" }
  ],
  "estados": [
    { "estado": "REGISTRADA", "grupo": "NUEVOS", "total": 5, "porcentaje": 31.3 },
    { "estado": "EN INSPECCIÓN", "grupo": "EN_PROCESO", "total": 2, "porcentaje": 12.5 }
  ],
  "recientes": []
}
```

Agrupación de estados usada por el dashboard:

| Grupo | Estados incluidos | Color |
|---|---|---|
| `NUEVOS` | `REGISTRADA` | Amarillo |
| `EN_PROCESO` | `EN INSPECCIÓN`, `EN REPARACIÓN TÉCNICA`, `EN VALIDACIÓN DE REPARACIÓN` | Naranja |
| `PENDIENTES` | `PENDIENTE DE CIERRE` | Rojo |
| `RESUELTOS` | `SOLUCIONADA / CERRADA` | Verde |
| `RECHAZADOS` | `RECHAZADA` | Rojo oscuro |

La pantalla usa su propio `app-grafico-distribucion` (dona con `conic-gradient`) y los colores del código de estados. El mismo código de colores se aplica a las quejas recientes del dashboard, al catálogo de quejas y al detalle del reporte.

Exportaciones simples del dashboard:

| Formato | Contenido |
|---|---|
| PDF | Título, fecha, total, tabla de contadores, tabla por estado con color y quejas recientes |
| Excel | Hojas `Resumen`, `Estados` (con color por grupo) y `Recientes` |

El PDF/Excel del dashboard no reutiliza `ReporteExportService` ni incluye imágenes de gráficos: es una exportación rápida del estado actual. Si más adelante se necesita el gráfico incrustado, se puede adaptar el mismo patrón del módulo de reportes.

---

## 5. Fases de implementación

### Fase 0 — Verificación previa

**Tiempo:** 5 min. **Depende de:** nada. **Salida:** entorno verificado y alcance congelado.

- [x] Confirmar que el único reporte de muestra será **“Quejas por categoría”**, con agrupación configurable y sin implementar los reportes oficiales.
- [x] Verificar que backend y frontend compilan/arrancan (`./mvnw spring-boot:run` y `npm start`).
- [x] Verificar que existe al menos una queja en estados distintos para que los gráficos no salgan vacíos (`16` quejas en `6` estados).
- [x] Ejecutar la línea base: `./mvnw test` y `npm run build` (anotar si ya fallaban antes de tocar código).
- [x] Confirmar que no se harán commits ni push; trabajar en el árbol local.

**Verificación:** el proyecto compila antes de los cambios y hay datos de prueba.

**Línea base ejecutada (2026-11-04):**
- Backend: `./mvnw -q -DskipTests package` compila.
- Backend tests: `./mvnw test` ejecuta 17 pruebas; falla sólo `BackendApplicationTests.contextLoads` porque `${DB_HOST}`, `${DB_PORT}` y `${DB_NAME}` no están resueltos en el entorno de pruebas. Es un fallo preexistente, no del cambio de bitácora.
- Frontend: `npm run build` falla por presupuestos de CSS preexistentes (`detalle-queja-gestion`, `detalle-queja-especialista`, `detalle-queja-inspector`) y un warning de `GestionPersonalComponent`. No se corrigen en este plan.

---

### Fase 1 — Consulta de bitácora (implementada)

**Tiempo:** 0 min (ya hecha). **Depende de:** nada. **Salida:** bitácora consultable por API.

- [x] Crear `BitacoraRespuestaDTO` con: `registroId`, `usuarioAccion`, `direccionIp`, `fechaHora`, `tablaAfectada`, `idAfectado`, `campoAfectado`, `accion`, `registroAntiguo`, `registroNuevo`.
- [x] Agregar `BitacoraRepository.buscarConFiltros(...)` con filtros por usuario, tabla, acción y rango de fechas, paginado y ordenado por `fechaHora DESC`.
- [x] Agregar `BitacoraService.listar(...)` que normalice filtros y convierta entidades a `BitacoraRespuestaDTO`.
- [x] Agregar `GET /admin/bitacora` en `BitacoraController` con los filtros anteriores, `pagina` y `tamanoPagina`.
- [x] Agregar prueba unitaria de la consulta en `BitacoraServiceTest`.

**No se hace en esta fase:** índices, login fallido, `registrarSeguro`, auditoría de resultado éxito/fallo ni pantalla de bitácora.

**Verificación:**
- `./mvnw -q -Dtest=BitacoraServiceTest test` pasa.
- `GET /admin/bitacora?pagina=0&tamanoPagina=20` devuelve un `Page` con `content`.
- El endpoint queda bajo `/admin/**`; para la demo se usa el usuario administrador.

---

### Fase 2 — Verificación de cobertura de bitácora

**Tiempo:** 10 min. **Depende de:** Fase 1. **Salida:** lista de comprobaciones y hallazgos. **No se implementa código nuevo salvo que una verificación falle.**

La bitácora no es la fuente principal del reporte de quejas: el reporte lee de `quejas` e `informes_queja`. La bitácora se usa para **auditar acciones** y para responder “quién consultó/exportó el reporte”. Estas son las verificaciones que deben pasar antes de dar la bitácora por suficiente:

| # | Verificación | Acción de prueba | Resultado esperado |
|---|---|---|---|
| B1 | Login exitoso | Iniciar sesión como admin; `GET /admin/bitacora?accion=LOGIN` | Fila con usuario, IP y fecha |
| B2 | Registro de queja | Crear queja; `GET /admin/bitacora?tabla=quejas&accion=CREAR` | `idAfectado` = ID; `registroNuevo` con estado/correlativo |
| B3 | Cambio de estado | Asignar inspector o mover estado; consultar `tabla=quejas` y revisar `campoAfectado` en la respuesta | `registroAntiguo` y `registroNuevo` con estados |
| B4 | Asignación de inspector | Asignar inspector; consultar `tabla=quejas` y revisar `campoAfectado=inspector_id` en la respuesta | Inspector anterior y nuevo |
| B5 | Asignación de especialista/dependencia | Autorizar reparación; revisar en la respuesta los campos `especialista_id` y `dependencia_id` | Valores anteriores y nuevos |
| B6 | Informes | Crear inspección, solución y cierre; filtrar `tabla=informes_queja&accion=CREAR` | Una fila por informe |
| B7 | Rechazo/devolución | Rechazar o devolver; revisar el motivo en `registroNuevo` | Motivo `motivo_rechazo`/`motivo_devolucion` presente |
| B8 | Evidencias | Subir foto; filtrar `tabla=evidencias_digitales&accion=SUBIR` | Archivo y queja asociada |
| B9 | Datos sensibles | Buscar `password`, `hash`, `token` en `registroAntiguo`/`registroNuevo` | No aparecen |
| B10 | Filtros y paginación | Probar usuario, tabla, acción y fechas | Resultados correctos y `totalElements` |
| B11 | Auditoría de reporte | Consultar/exportar el reporte de muestra | Fila `tabla=reportes`, `accion=CONSULTAR`/`DESCARGAR` |

- [x] Ejecutar B1–B2 y anotar el resultado: login y creación de queja con usuario, IP, `idAfectado` y correlativo.
- [x] Ejecutar B3–B5 y anotar el resultado: `estado_actual`, `inspector_id`, `especialista_id` y `dependencia_id` con valores anterior/nuevo.
- [x] Ejecutar B6–B8 y anotar el resultado: informes de inspección/solución/cierre, motivo de devolución y evidencia con archivo y queja.
- [x] Ejecutar B9–B10 y anotar el resultado: sin datos sensibles; filtros y paginación de bitácora responden y respetan `totalElements`.
- [x] Ejecutar las consultas de cobertura y de filas incompletas de la sección 6.3: 2 filas históricas incompletas de restablecimiento; el código actual ya escribe todos los campos.
- [x] Verificar B11 con el reporte de muestra: `tabla=reportes`, `CONSULTAR` y `DESCARGAR` (Excel/PDF).
- [x] Registrar hallazgos en el Registro de ejecución y decidir si hace falta un ajuste puntual: se corrigió la consulta de bitácora con parámetros nulos.
- [x] Si falta rastrear una queja o un campo concreto, evaluar agregar los filtros `idAfectado` y `campoAfectado` a `GET /admin/bitacora`: no fue necesario; los filtros actuales cubren la verificación.

**Verificación:** cada fila B1–B10 tiene evidencia (fila de bitácora o consulta SQL) y no se encontraron datos sensibles.

**Ajustes permitidos si una verificación falla (sólo el punto exacto):**
- Agregar el filtro `idAfectado` y/o `campoAfectado` a `GET /admin/bitacora`.
- Agregar la llamada a `BitacoraService.registrar` en la acción que no esté cubierta.
- Corregir el `campoAfectado` o el `registroNuevo` para que incluya el dato faltante.
- No agregar login fallido, resultado éxito/fallo, logout ni índices.

> Si una verificación falla, se corrige sólo el punto exacto; no se amplía el alcance.

---

### Fase 3 — Contrato del reporte de muestra

**Tiempo:** 10 min. **Depende de:** Fase 2. **Salida:** DTOs y contrato estable para un solo reporte.

- [x] Crear `ReporteAgrupacion` con `CATEGORIA` como valor por defecto y `ESTADO`, `SUBCATEGORIA`, `PRIORIDAD`, `TIPO_REGISTRO`, `DEPENDENCIA`, `ESPECIALISTA`, `INSPECTOR`. No incluir `ZONA`.
- [x] Crear `ReporteFiltroDTO` con `fechaDesde`, `fechaHasta`, `estado`, `categoriaId`, `prioridad`, `agrupacion`.
- [x] Crear `ReporteKpiDTO`, `ReporteSerieDTO`, `ReporteFilaDTO`, `ReporteTablaDTO`, `ReporteDetalleDTO`, `ReporteOpcionesDTO`, `OpcionDTO` y `ReporteResponseDTO`.
- [x] Crear `ReporteExceptionHandler` con `@RestControllerAdvice(basePackages = "com.muni.backend.reportes.controller")` para devolver `400` en `IllegalArgumentException` y `500` genérico.
- [x] Confirmar que el JSON de ejemplo de la sección 4.1 coincide con los nombres de los DTOs.

**Verificación:** el backend compila y el contrato no incluye reportes adicionales; sólo el de muestra.

---

### Fase 4 — Consultas SQL y opciones

**Tiempo:** 15 min. **Depende de:** Fase 3. **Salida:** agregación, KPIs y catálogos para los filtros.

- [x] Crear `ReporteAgrupadoProjection` (`etiqueta`, `total`, `cerradas`, `enProceso`, `rechazadas`, `porcentaje`) y `ReporteKpiProjection` (`total`, `cerradas`, `rechazadas`, `enProceso`, `promedioDiasCierre`).
- [x] Crear `ReporteQuejaRepository` con la consulta de agrupación genérica (sección 6.1) usando `CASE CAST(:agrupacion AS text)` y `GROUP BY 1`.
- [x] Crear la consulta de KPIs (sección 6.2) con `FILTER` y promedio de días de cierre.
- [x] Crear `ReporteQuejaService.obtenerOpciones()` usando `CategoriaQuejaRepository`, `SubcategoriaQuejaRepository`, `DependenciaRepository`, `UsuarioRepository.findByRolActivo("ESPECIALISTA_TECNICO")` y los enums de estados, prioridades y agrupaciones; el método `generar(...)` se agrega en la Fase 5.
- [x] Probar ambas consultas directamente en PostgreSQL con un rango de fechas y verificar que los totales cuadran con `SELECT COUNT(*)`: 16 quejas; suma por categoría = 16; KPIs 2 cerradas, 11 en proceso, 3 rechazadas; promedio 26.6 días.

**Verificación:** para una agrupación `CATEGORIA`, la suma de `total` coincide con el KPI `total`; no hay etiquetas nulas; `obtenerOpciones()` devuelve catálogos no vacíos cuando hay datos (el endpoint se expone en Fase 5).

> No se crean índices en esta fase. Si con los datos reales una consulta se vuelve lenta, se mide primero y se agrega sólo el índice necesario.

---

### Fase 5 — Servicio, controller y exportaciones

**Tiempo:** 20 min. **Depende de:** Fase 4. **Salida:** API funcional del reporte de muestra y descargas.

- [x] Crear `ReporteQuejaService.generar(filtro, username, limiteDetalle)`:
  - Validar `fechaDesde <= fechaHasta`; si faltan fechas, usar primer día del mes actual y hoy.
  - Convertir `fechaDesde` a inicio del día y `fechaHasta` a fin del día (o usar `< fechaHasta + 1 día`) para no excluir el último día.
  - Normalizar `TODAS`/vacíos a `null`; validar `agrupacion`.
  - Ejecutar agregación y KPIs.
  - Construir KPIs formateados (`1,247`, `87.3%`, `4.2 días`).
  - Construir `series` y `tabla.filas`.
  - Cargar el detalle con `QuejaRepository.buscarCatalogoConFiltros(estado, categoriaId, prioridad, null, inicio, fin, PageRequest.of(0, limiteDetalle))`.
- [x] Crear `ReporteExportService.generarExcel(ReporteResponseDTO)` con hojas `Resumen`, `Agrupado`, `Detalle` y `Gráficos` (imágenes PNG incrustadas).
- [x] Crear `ReporteExportService.generarPdf(ReporteResponseDTO)` con título, periodo, filtros, KPIs, gráficos de barras/pastel, tabla agrupada y primeras 100 filas de detalle.
- [x] Crear `ReporteChartService` con Java2D para generar las imágenes de barras y pastel sin dependencias externas.
- [x] Crear `ReporteQuejaController`:
  - `POST /admin/reportes/quejas/consultar`
  - `GET /admin/reportes/opciones`
  - `POST /admin/reportes/quejas/exportar/excel`
  - `POST /admin/reportes/quejas/exportar/pdf`
- [x] Registrar auditoría con `BitacoraService.registrar`:
  - Consulta: `tablaAfectada = "reportes"`, `campoAfectado = "consulta"`, `accion = "CONSULTAR"`, `registroNuevo` con filtros.
  - Exportación: `campoAfectado = "exportacion_pdf"` o `"exportacion_excel"`, `accion = "DESCARGAR"`.
- [x] Agregar regla en `SecurityConfig` antes de `/admin/**`: `/admin/reportes/**` sólo `ADMINISTRADOR`/`ROLE_ADMINISTRADOR`.
- [x] Verificar que `/admin/quejas/**` sigue funcionando y que no se rompió el catálogo existente: `GET /admin/quejas/catalogo` respondió HTTP 200 con datos.

**Verificación:**
- `POST /admin/reportes/quejas/consultar` devuelve KPIs, series, tabla y detalle.
- `GET /admin/reportes/opciones` devuelve catálogos.
- Ambas exportaciones descargan archivos válidos e incluyen los gráficos de barras y pastel.
- Cada consulta/exportación crea una fila en `bitacora`.

---

### Fase 6 — Frontend: modelos, servicio y gráficos

**Tiempo:** 15 min. **Depende de:** Fase 5. **Salida:** base frontend reutilizable para el reporte de muestra.

- [x] Crear `core/models/reporte-queja.model.ts` con interfaces espejo del contrato JSON.
- [x] Crear `core/services/reporte-queja.service.ts` con:
  - `consultar(filtros): Observable<ReporteResponse>` usando `POST`.
  - `obtenerOpciones(): Observable<ReporteOpciones>` usando `GET`.
  - `exportarExcel(filtros): Observable<Blob>` usando `POST` y `responseType: 'blob'`.
  - `exportarPdf(filtros): Observable<Blob>` usando `POST` y `responseType: 'blob'`.
  - Descarga del blob con un helper simple.
- [x] Crear `GraficoBarrasComponent` con barras horizontales CSS, etiqueta, valor y porcentaje.
- [x] Crear `GraficoPastelComponent` con `conic-gradient`, leyenda y agregación de “Otros” cuando hay más de 6 categorías.
- [x] Crear `GraficoApiladoComponent` con la composición resueltas/en proceso/rechazadas por dimensión.
- [x] Verificar que los tres componentes muestran un estado vacío cuando no hay datos.

**Verificación:** los componentes se pueden importar en una página de prueba y renderizan datos simulados.

---

### Fase 7 — Página admin y pruebas

**Tiempo:** 15 min. **Depende de:** Fase 6. **Salida:** pantalla `/admin/reportes` funcional y pruebas del reporte de muestra.

- [x] Reemplazar el placeholder `AdminReportesComponent` existente; no crear una ruta nueva.
- [x] Usar `ReactiveFormsModule` y `signal()` para `reporte`, `opciones`, `cargando` y `error`.
- [x] Formulario de filtros: fecha desde, fecha hasta, categoría, estado, prioridad y agrupación; botones `Consultar`, `Limpiar`, `Exportar PDF` y `Exportar Excel`.
- [x] Al iniciar: cargar opciones y consultar con el rango por defecto.
- [x] Mostrar 6 tarjetas KPI, gráfico de barras, pastel, barras apiladas, tabla agrupada y tabla de detalle; incluir estado vacío y mensaje de error.
- [x] Mantener la ruta `/admin/reportes` y el menú actual; no tocar `/funcionario/**`.
- [x] Agregar `ReporteQuejaServiceTest` con: rango inválido, valores por defecto y resultado vacío.
- [x] Ejecutar `./mvnw -q -Dtest=ReporteQuejaServiceTest test` y `npm run build`; anotar si hay fallos preexistentes.
- [x] Prueba funcional equivalente: login admin, ruta `/admin/reportes` servida, consulta con varios filtros/agrupaciones, exportación Excel/PDF y fila B11 (`tabla=reportes`). No se automatizó un navegador; la UI se verificó por ruta y la API por HTTP.
- [x] Actualizar `Estado Actual` y agregar la fila final al Registro de ejecución.

**Verificación:**
- La pantalla carga sin errores nuevos en consola.
- Al cambiar filtros y consultar, cambian KPIs, gráficos y tablas.
- Los botones de exportación descargan archivos.
- La consulta/exportación deja la fila de auditoría B11.

---

### Fase 8 — Adaptación al tema real (cuando se conozca)

**Tiempo:** variable. **Depende de:** Fase 7. **Salida:** reporte definitivo sin rehacer la base.

- [x] **Fase 8.1 — Plantilla congelada:** reporte de quejas documentado como ejemplo canónico, con barras, pastel, apilado y PDF/Excel con gráficos.
- [x] **Fase 8.2 — Dashboard en tiempo real:** agregar `GET /admin/dashboard` con contadores por grupo (nuevos, en proceso, pendientes, resueltos y rechazados), distribución por estado y quejas recientes.
- [x] Separar el dashboard en su propio módulo backend (`com.muni.backend.dashboard`) y frontend (`modules/dashboard`), sin depender del módulo de reportes.
- [x] Agregar exportación simple a PDF y Excel del dashboard (`GET /admin/dashboard/exportar/pdf` y `/excel`), auditada en `bitacora`.
- [x] Crear el código de colores compartido (`estado-queja.util.ts`): amarillo para nuevos, naranja para en proceso, rojo para pendientes/rechazadas y verde para resueltos.
- [x] Crear la página `/admin/dashboard` con autoactualización cada 15 segundos, actualización manual, KPIs, dona de distribución y detalle por estado.
- [x] Aplicar el mismo código de colores al catálogo de quejas y al detalle del reporte.
- [x] Agregar pruebas unitarias del dashboard (`DashboardServiceTest` y `DashboardExportServiceTest`) y build de desarrollo del frontend.
- [ ] Reemplazar el título “Reporte de quejas por categoría” por el tema real solicitado.
- [ ] Elegir el dataset y las dimensiones: si sigue siendo quejas, cambiar el valor por defecto de `agrupacion`, el título y los filtros visibles; si es otro dataset, crear un método/repositorio nuevo y reutilizar DTOs, gráficos y pantalla.
- [ ] Ajustar `/admin/reportes/opciones` para que devuelva sólo los filtros que use el tema real.
- [ ] Actualizar el `registroNuevo` de auditoría con los filtros definitivos.
- [ ] Ejecutar las pruebas y la verificación B11 con el reporte definitivo.
- [ ] **Opcional:** pantalla de bitácora reutilizando `GET /admin/bitacora`.
- [ ] **Opcional:** índices mínimos sólo si una consulta se vuelve lenta; medir antes.
- [ ] **Opcional:** normalización de roles (`FUNCIONARIO_MUNICIPAL` → `FUNCIONARIO`) si se necesita login de funcionario.
- [ ] **Opcional:** localidades por coordenadas, en un módulo aparte, sólo si se pide; no usar `zona`.

> **Atajo para el día de la solicitud:** abrir la sección 7, seguir el checklist 7.2 y usar el contrato 7.3. Si el reporte es de otro dataset, copiar la consulta SQL de 6.1 y cambiar el `FROM`/`JOIN`/`WHERE`; no tocar la pantalla ni los exportadores.

> **No se aborda en la base:** materiales normalizados, inventario, auditoría de acciones fallidas, dictamen de conformidad, reportes programados ni zonas. Requieren cambios de alcance que no pertenecen al reporte de muestra.

---

## 6. Consultas SQL de referencia

### 6.1 Agrupación genérica

```sql
SELECT
    CASE CAST(:agrupacion AS text)
        WHEN 'ESTADO' THEN q.estado_actual
        WHEN 'CATEGORIA' THEN c.nombre_categoria
        WHEN 'SUBCATEGORIA' THEN COALESCE(s.nombre_subcategoria, 'Sin subcategoría')
        WHEN 'PRIORIDAD' THEN COALESCE(q.prioridad_confirmada, 'Sin prioridad')
        WHEN 'TIPO_REGISTRO' THEN COALESCE(q.tipo_registro, 'PRINCIPAL')
        WHEN 'DEPENDENCIA' THEN COALESCE(d.nombre_dependencia, 'Sin dependencia')
        WHEN 'ESPECIALISTA' THEN COALESCE(e.nombres || ' ' || e.apellidos, 'Sin especialista')
        WHEN 'INSPECTOR' THEN COALESCE(i.nombres || ' ' || i.apellidos, 'Sin inspector')
        ELSE q.estado_actual
    END AS "etiqueta",
    COUNT(*) AS "total",
    COUNT(*) FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA') AS "cerradas",
    COUNT(*) FILTER (WHERE q.estado_actual = 'RECHAZADA') AS "rechazadas",
    COUNT(*) FILTER (WHERE q.estado_actual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')) AS "enProceso",
    ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) AS "porcentaje"
FROM quejas q
JOIN categorias_queja c ON c.categoria_id = q.categoria_id
LEFT JOIN subcategorias_queja s ON s.subcategoria_id = q.subcategoria_id
LEFT JOIN dependencias d ON d.dependencia_id = q.dependencia_asignada_id
LEFT JOIN usuarios e ON e.usuario_id = q.especialista_id
LEFT JOIN usuarios i ON i.usuario_id = q.inspector_id
WHERE q.fecha_registro >= CAST(:fechaDesde AS date)
  AND q.fecha_registro < (CAST(:fechaHasta AS date) + INTERVAL '1 day')
  AND (CAST(:estado AS text) IS NULL OR q.estado_actual = CAST(:estado AS text))
  AND (CAST(:categoriaId AS integer) IS NULL OR q.categoria_id = CAST(:categoriaId AS integer))
  AND (CAST(:prioridad AS text) IS NULL OR q.prioridad_confirmada = CAST(:prioridad AS text))
GROUP BY 1
ORDER BY "total" DESC, "etiqueta" ASC;
```

### 6.2 KPIs

```sql
SELECT
    COUNT(*) AS "total",
    COUNT(*) FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA') AS "cerradas",
    COUNT(*) FILTER (WHERE q.estado_actual = 'RECHAZADA') AS "rechazadas",
    COUNT(*) FILTER (WHERE q.estado_actual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')) AS "enProceso",
    COALESCE(
        ROUND(
            AVG(EXTRACT(EPOCH FROM (q.fecha_cierre - q.fecha_registro)) / 86400.0)
                FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA'
                          AND q.fecha_cierre IS NOT NULL),
            1
        ),
        0
    ) AS "promedioDiasCierre"
FROM quejas q
WHERE q.fecha_registro >= CAST(:fechaDesde AS date)
  AND q.fecha_registro < (CAST(:fechaHasta AS date) + INTERVAL '1 day')
  AND (CAST(:estado AS text) IS NULL OR q.estado_actual = CAST(:estado AS text))
  AND (CAST(:categoriaId AS integer) IS NULL OR q.categoria_id = CAST(:categoriaId AS integer))
  AND (CAST(:prioridad AS text) IS NULL OR q.prioridad_confirmada = CAST(:prioridad AS text));
```

### 6.3 Verificación rápida

```sql
-- Totales por estado
SELECT estado_actual, COUNT(*) FROM quejas GROUP BY 1 ORDER BY 2 DESC;

-- Consulta de bitácora (sin filtro)
SELECT fecha_hora, usuario_accion, accion, tabla_afectada, campo_afectado, registro_nuevo
FROM bitacora
ORDER BY fecha_hora DESC
LIMIT 20;

-- Auditoría de reportes
SELECT fecha_hora, usuario_accion, accion, campo_afectado, registro_nuevo
FROM bitacora
WHERE tabla_afectada = 'reportes'
ORDER BY fecha_hora DESC
LIMIT 20;

-- Cobertura: qué tablas y acciones se están registrando
SELECT tabla_afectada, accion, COUNT(*) AS total
FROM bitacora
GROUP BY tabla_afectada, accion
ORDER BY total DESC;

-- Filas incompletas: no deberían existir para acciones auditables
SELECT registro_id, fecha_hora, usuario_accion, accion, tabla_afectada
FROM bitacora
WHERE fecha_hora IS NULL
   OR usuario_accion IS NULL
   OR accion IS NULL
   OR tabla_afectada IS NULL
ORDER BY fecha_hora DESC;
```

> No hay consulta de índices porque no se crean índices en esta etapa. La tabla `bitacora` y las tablas de quejas ya existen; no se modifica el esquema.

---

## 7. Guía para adaptar la plantilla a un reporte nuevo (10–15 min)

> **Regla de oro:** el reporte de quejas es la **plantilla viva**. No se crea otro módulo de reportes ni se duplica la pantalla. Se edita el dataset, los filtros y los textos; los DTOs, los gráficos, la auditoría y los exportadores se reutilizan tal cual.

### 7.1 Archivos que forman la plantilla

| Capa | Archivo | ¿Se toca al adaptar? |
|---|---|---|
| Contrato de respuesta | `reportes/dto/ReporteResponseDTO.java` y DTOs de KPIs/series/tabla/detalle | Sólo si el reporte necesita una columna nueva |
| Filtros | `reportes/dto/ReporteFiltroDTO.java` | Sí, si el reporte pide filtros distintos |
| Agrupaciones | `reportes/model/ReporteAgrupacion.java` | Sí, si el reporte pide dimensiones nuevas |
| Consulta SQL | `reportes/repository/ReporteQuejaRepository.java` | Sí: es el corazón del cambio de dataset |
| Servicio | `reportes/service/ReporteQuejaService.java` | Sí: mapear filas, KPIs, títulos y filtros |
| Gráficos de exportación | `reportes/service/ReporteChartService.java` | Casi nunca; ya genera barras y pastel |
| Exportadores | `reportes/service/ReporteExportService.java` | Casi nunca; ya genera PDF/Excel con gráficos |
| Controller | `reportes/controller/ReporteQuejaController.java` | Sólo si cambia la ruta o el método |
| Frontend: modelos | `core/models/reporte-queja.model.ts` | Sí, si cambian filtros/columnas |
| Frontend: servicio | `core/services/reporte-queja.service.ts` | Casi nunca; reutiliza el mismo contrato |
| Frontend: página | `modules/admin/pages/admin-reportes/*` | Sí: textos y campos de filtro |
| Frontend: gráficos | `modules/admin/components/grafico-barras`, `grafico-pastel`, `grafico-apilado` | No; se reutilizan |

### 7.2 Checklist de adaptación

1. **Elegir la dimensión.** Si el reporte sólo cambia la agrupación (“por estado”, “por categoría”, “por dependencia” o “por especialista”), no se toca el backend: sólo se cambia el valor por defecto de `agrupacion` y el título en el frontend.
2. **Agregar una dimensión nueva.** Agregar el valor al enum `ReporteAgrupacion` y un `WHEN` en la consulta SQL. No usar concatenación dinámica de SQL. No agregar `ZONA`: si se pide localidad, se resuelve con coordenadas en un módulo aparte.
3. **Agregar un KPI.** Calcularlo en `ReporteQuejaService` a partir de las proyecciones o de una consulta adicional; agregarlo a la lista `kpis` con icono y tono.
4. **Agregar un filtro.** Agregar el campo al `ReporteFiltroDTO`, al `WHERE` de las consultas, al formulario Angular y al cuerpo JSON del `POST`. Mantener el filtro opcional con `CAST(:param AS tipo) IS NULL`.
5. **Cambiar el dataset.** Si el reporte no es de quejas (por ejemplo, usuarios), crear un repositorio y un método nuevo que devuelva las mismas proyecciones (`etiqueta`, `total`, `porcentaje`, etc.) y reutilizar `ReporteResponseDTO`, los gráficos y la pantalla. No duplicar el exportador.
6. **Exportaciones.** No hace falta tocar `ReporteExportService` si el nuevo reporte devuelve el mismo `ReporteResponseDTO`: PDF y Excel ya incluyen KPIs, filtros, tabla, detalle y gráficos.
7. **Auditoría.** El controller debe seguir registrando `CONSULTAR`/`DESCARGAR` con el nombre del reporte en `registroNuevo`.
8. **Rendimiento.** No agregar índices preventivamente; probar con datos reales y medir antes de crear uno.

### 7.3 Contrato mínimo que debe devolver cualquier reporte

```json
{
  "titulo": "Reporte de ...",
  "subtitulo": "Periodo: ...",
  "fechaEmision": "2026-11-07T10:00:00",
  "filtrosAplicados": { "Fecha desde": "01/11/2026", "Agrupación": "Categoría" },
  "kpis": [
    { "etiqueta": "Total", "valor": "120", "icono": "description", "tono": "primary" }
  ],
  "series": [
    { "etiqueta": "Categoría A", "valor": 70, "porcentaje": 58.3 }
  ],
  "tabla": {
    "titulo": "Resumen por categoría",
    "columnas": ["Dimensión", "Total", "Resueltas", "En proceso", "Rechazadas", "%"],
    "filas": [
      { "etiqueta": "Categoría A", "total": 70, "cerradas": 40, "enProceso": 20, "rechazadas": 10, "porcentaje": 58.3 }
    ]
  },
  "detalle": [],
  "totalDetalle": 0,
  "limiteDetalle": 100
}
```

Si el reporte nuevo no tiene “cerradas / en proceso / rechazadas”, se dejan en `0` y se adaptan los encabezados de la tabla y del gráfico apilado. Los gráficos de barras y pastel sólo necesitan `series`.

### 7.4 Recetas de gráficos (para copiar sin internet)

| Gráfico | Componente/servicio | Cuándo usarlo |
|---|---|---|
| Barras horizontales | `app-grafico-barras` + `ReporteChartService.generarBarras` | Comparar totales entre categorías |
| Pastel | `app-grafico-pastel` + `ReporteChartService.generarPastel` | Ver participación porcentual |
| Barras apiladas | `app-grafico-apilado` | Ver composición por estado dentro de cada categoría |
| Línea / tendencia | Receta de la sección 7.5 | Evolución por mes/día; requiere serie temporal |

**Regla para gráficos de línea:** el contrato actual (`series` con etiqueta/valor/porcentaje) sirve si las etiquetas son meses o días. No hace falta cambiar el backend; se agrega un componente `app-grafico-linea` que reciba `ReporteSerie[]` y dibuje un SVG con los puntos normalizados. Si el eje necesita fechas reales, agregar `fecha` a `ReporteSerieDTO` y ajustar el componente.

### 7.5 Receta de gráfico de línea (copiable)

```ts
// frontend/src/app/modules/admin/components/grafico-linea/grafico-linea.ts
import { Component, Input } from '@angular/core';
import { ReporteSerie } from '../../../../core/models/reporte-queja.model';

@Component({
  selector: 'app-grafico-linea',
  standalone: true,
  templateUrl: './grafico-linea.html',
  styleUrl: './grafico-linea.css',
})
export class GraficoLineaComponent {
  @Input() series: ReporteSerie[] = [];

  get puntos(): string {
    if (this.series.length === 0) return '';
    const max = Math.max(...this.series.map((s) => s.valor), 1);
    return this.series
      .map((s, i) => {
        const x = this.series.length === 1 ? 50 : (i / (this.series.length - 1)) * 100;
        const y = 100 - (s.valor / max) * 100;
        return `${x},${y}`;
      })
      .join(' ');
  }
}
```

```html
<!-- grafico-linea.html -->
@if (series.length === 0) {
  <div class="grafico-vacio">Sin datos para graficar</div>
} @else {
  <svg viewBox="0 0 100 100" preserveAspectRatio="none" class="linea">
    <polyline [attr.points]="puntos" fill="none" stroke="var(--primary-500)" stroke-width="2" />
  </svg>
  <div class="leyenda">
    @for (serie of series; track serie.etiqueta) {
      <span>{{ serie.etiqueta }}: {{ serie.valor }}</span>
    }
  </div>
}
```

> Esta receta queda documentada, no implementada, para no agregar un cuarto gráfico que el reporte de muestra no necesita. Si el reporte solicitado es temporal, se crea el componente y se usa igual que los otros tres.

### 7.6 Ejemplo: reporte de usuarios registrados

- Fuente: `usuarios` + `credenciales` + `rol_user` + `dependencias`.
- Agrupación: rol, estado o dependencia.
- Filtros: rango de fechas de `usuarios.fecha_creacion`, rol, estado.
- KPIs: total, activos, inactivos, nuevos del periodo.
- Gráficos: barras por rol, pastel por estado, apilado por rol/estado.
- Limitación: no hay localidad normalizada; si se pide, usar coordenadas en un módulo aparte. No se agrega `zona` al MVP.

### 7.7 Respuestas rápidas para la demo

| Pregunta probable | Dónde está la respuesta |
|---|---|
| ¿Cuántos registros hay en el periodo? | KPI “Total” |
| ¿Cuántos están resueltos / en proceso / rechazados? | KPIs y gráfico apilado |
| ¿Qué categoría concentra más casos? | Primera fila de la tabla y barra más larga |
| ¿Qué porcentaje representa cada categoría? | Pastel y columna “%” |
| ¿Cuál es la tasa de resolución? | KPI “Tasa de resolución” |
| ¿Cuánto tarda el cierre? | KPI “Días promedio de cierre” |
| ¿Quién consultó o exportó el reporte? | `GET /admin/bitacora?tabla=reportes` |

---

## 8. Matriz de pruebas

| # | Caso | Resultado esperado |
|---|---|---|
| 1 | Rango de fechas válido con datos | KPIs, gráficos y tablas con datos |
| 2 | `fechaDesde > fechaHasta` | HTTP 400 con mensaje claro |
| 3 | Rango sin datos | HTTP 200, listas vacías, estado vacío en UI |
| 4 | Categoría específica | Totales sólo de esa categoría |
| 5 | Estado específico | Totales sólo de ese estado |
| 6 | Agrupación `DEPENDENCIA` | Una fila por dependencia, “Sin dependencia” si aplica |
| 7 | Exportar Excel | Archivo `.xlsx` con 4 hojas (`Resumen`, `Agrupado`, `Detalle`, `Gráficos`) y datos |
| 8 | Exportar PDF | Archivo `.pdf` con título, KPIs y tabla |
| 9 | Consulta/exportación | Fila en `bitacora` con `tabla_afectada='reportes'` |
| 10 | `GET /admin/bitacora` | Página con filtros y `totalElements` correcto |
| 11 | Sin token | HTTP 401/403, no 500 |
| 12 | Pantalla responsive | KPIs y gráficos se apilan en móvil |
| 13 | Consulta por `POST` | Los filtros no aparecen en la URL; HTTP 200 |
| 14 | Opciones de filtros | `GET /admin/reportes/opciones` devuelve catálogos |
| 15 | Exportar PDF con gráficos | El `.pdf` incluye barras y pastel además de KPIs/tabla |
| 16 | Exportar Excel con gráficos | El `.xlsx` incluye la hoja “Gráficos” con las dos imágenes |
| 17 | Gráfico apilado | La pantalla muestra la composición resueltas/en proceso/rechazadas |
| 18 | Reporte sin series | PDF/Excel generan una imagen “Sin datos” sin fallar |
| 19 | `GET /admin/dashboard` | Responde total, contadores, estados y recientes sin auditar |
| 20 | Dashboard con datos | Los 5 contadores cuadran con la suma de estados |
| 21 | Autoactualización | La pantalla vuelve a consultar cada 15 s y actualiza la hora |
| 22 | Código de colores | Nuevos amarillo, en proceso naranja, pendientes rojo y resueltos verde en dashboard, catálogo y reporte |
| 23 | Exportar dashboard PDF | El `.pdf` contiene total, contadores, estados y quejas recientes |
| 24 | Exportar dashboard Excel | El `.xlsx` contiene las hojas `Resumen`, `Estados` y `Recientes` |
| 25 | Auditoría del dashboard | Cada exportación deja fila `tabla=dashboard`, `accion=DESCARGAR` |

---

## 9. Riesgos y mitigaciones

| Riesgo | Probabilidad | Mitigación |
|---|---|---|
| El tiempo de 90 min se agota | Alta | Seguir la ruta rápida; el PDF es el primer recorte. La consulta y Excel siguen funcionando |
| La consulta nativa falla por tipos null | Media | Usar `CAST(:param AS tipo)` y probar en PostgreSQL |
| El rol de funcionario no inicia sesión | Alta | Demo con administrador; normalización en Fase 8 |
| `@PreAuthorize` no está habilitado | Alta | No confiar en él; usar reglas de URL en `SecurityConfig` |
| PDF con acentos dañados | Media | Usar OpenPDF con Helvetica/WinAnsi; probar “Categoría” y “Días” |
| Reporte lento con muchos datos | Baja | Proyecto local; no crear índices preventivos. Medir y agregar sólo si hace falta; limitar exportación a 10,000 filas y evitar N+1 |
| El detalle no coincide con los filtros de agrupación | Media | En MVP los filtros son fecha/estado/categoría/prioridad; no filtrar por zona ni por una agrupación que el detalle no use |
| El componente no se actualiza en modo zoneless | Media | Usar `signal()` para estado y `ReactiveFormsModule` para filtros |
| Se promete auditoría de acciones fallidas | Baja | No incluirla en la demo; está fuera de alcance |

---

## 10. Definition of Done (MVP)

- [x] `GET /admin/bitacora` responde con filtros y paginación.
- [x] El backend compila con `./mvnw -q -DskipTests package` y `ReporteQuejaServiceTest` pasa.
- [x] El frontend no introduce errores nuevos; `npm run build` sigue fallando por los presupuestos CSS preexistentes, deuda ajena al reporte.
- [x] `POST /admin/reportes/quejas/consultar` responde con el contrato de la sección 4: KPIs, series, tabla y detalle verificados con 8 agrupaciones y filtros de estado/categoría/prioridad.
- [x] La pantalla `/admin/reportes` muestra KPIs, barras, pastel, tabla y detalle.
- [x] Los filtros de fecha, estado, categoría, prioridad y agrupación funcionan.
- [x] Las exportaciones PDF y Excel descargan archivos válidos: `.xlsx` con 4 hojas y `.pdf` de 2 páginas con KPIs, gráficos, tabla y detalle.
- [x] Las exportaciones incluyen los gráficos de barras y pastel; Excel agrega la hoja “Gráficos”.
- [x] La pantalla incluye un tercer gráfico de barras apiladas por estado.
- [x] La plantilla está documentada en la sección 7 con contrato, checklist y recetas de gráficos.
- [x] Cada consulta/exportación queda en `bitacora`: B11 verificado con `tabla=reportes`, `CONSULTAR` y `DESCARGAR`.
- [x] No hay arreglos hardcodeados de métricas en el componente; todo viene de la API.
- [x] No se agregaron índices, migraciones, zonas, cuadrillas ni normalización de materiales.
- [x] `Estado Actual` refleja la plantilla congelada (`87.5%` global) y el Registro de ejecución tiene las filas de la Fase 8.1; la adaptación al tema real queda pendiente.
- [x] El plan está guardado en `PLAN_REPORTERIA.md` en la raíz del proyecto.

---

## 11. Registro de ejecución

| Fecha | Fase | Tarea | Resultado | Nota |
|---|---|---|---|---|
| 2026-11-04 | 0 | Plan creado | Completado | Documento inicial |
| 2026-11-04 | 0 | Línea base backend/frontend | Fallos preexistentes | Backend compila; `./mvnw test` falla sólo por variables de BD; `npm run build` falla por presupuestos CSS |
| 2026-11-04 | 1 | `BitacoraRespuestaDTO`, repositorio, servicio, GET y test | Completado | `./mvnw -q -Dtest=BitacoraServiceTest test` OK |
| 2026-11-04 | — | Plan afinado: sin índices, zonas, cuadrillas, materiales ni auditoría de fallos | Completado | Alcance reducido según indicaciones |
| 2026-11-04 | — | Plan afinado: un solo reporte de muestra, POST para filtros y arquitectura detallada | Completado | Se agrega verificación B1–B11 de cobertura de bitácora |
| 2026-11-04 | 3–7 | Implementación backend/frontend del reporte de muestra | Completado | DTOs, SQL, servicio, exportadores, seguridad, modelos, servicio Angular, gráficos y pantalla admin |
| 2026-11-04 | 7 | `./mvnw -q -DskipTests package`, `ReporteQuejaServiceTest` y `npm run build` | Completado con deuda previa | Backend OK; frontend compila pero mantiene errores de presupuesto CSS preexistentes |
| 2026-11-04 | 0 | Arranque real y datos de prueba | Completado | PostgreSQL disponible; backend y frontend arrancan; 16 quejas en 6 estados distintos |
| 2026-11-04 | 2 | Verificación B1–B11 de bitácora | Completado | B1=12, B2=2, B3=6, B4=1, B5=2, B6=3, B7=1, B8=1, B9=0 sensibles, B11=14 filas; B10 validado por API |
| 2026-11-04 | 2 | Corrección de consulta de bitácora | Completado | Los parámetros nulos de texto/fecha se inferían como `bytea`; se usan `CAST` para texto y fechas centinela para el rango |
| 2026-11-04 | 4 | Consultas SQL directas | Completado | Total 16; agrupación por categoría suma 16; KPIs 2/11/3; promedio de cierre 26.6 días |
| 2026-11-04 | 5, 7 | Reporte y exportaciones reales | Completado | 8 agrupaciones y filtros; `.xlsx` válido con 3 hojas; `.pdf` válido de 2 páginas; auditoría B11 verificada |
| 2026-11-04 | 7 | Arranque frontend y ruta `/admin/reportes` | Completado | `npm start` responde HTTP 200; ruta servida; no se automatizó navegador |
| 2026-11-04 | 2 | Hallazgo histórico | Sin cambios | 2 filas antiguas de restablecimiento sin usuario/tabla y 8 informes previos sin auditoría; las acciones nuevas sí quedan completas |
| 2026-11-07 | 8.1 | Exportaciones con gráficos | Completado | `ReporteChartService` genera PNG de barras/pastel sin dependencias nuevas; PDF y Excel los incrustan |
| 2026-11-07 | 8.1 | Tercer gráfico de ejemplo | Completado | `app-grafico-apilado` reutiliza las filas de la tabla |
| 2026-11-07 | 8.1 | Plantilla documentada | Completado | Sección 7 ampliada con archivos, checklist, contrato, recetas y respuestas de demo |
| 2026-11-07 | 8.1 | Pruebas de exportación | Completado | `ReporteChartServiceTest` y `ReporteExportServiceTest` pasan |
| 2026-11-07 | 8.2 | Dashboard en tiempo real | Completado | Módulo propio `dashboard` (backend/frontend), `GET /admin/dashboard`, exportación PDF/Excel auditada, página `/admin/dashboard`, autoactualización cada 15 s y código de colores compartido |
| 2026-11-07 | 8.2 | Verificación | Completado | `ReporteQuejaServiceTest` (3 pruebas) y `npm run build -- --configuration development` sin errores nuevos |
|  |  |  |  |  |

---

## Anexo A — Catálogo de acciones de bitácora

| Acción | Uso |
|---|---|
| `CREAR` | Alta de entidad |
| `ACTUALIZAR` | Modificación de campo |
| `ELIMINAR` | Baja lógica o física |
| `LOGIN` | Inicio de sesión exitoso |
| `CERRAR_SESION` | Cierre de sesión desde `POST /auth/logout` |
| `SUBIR` | Carga de evidencia |
| `DESCARGAR` | Exportación o constancia |
| `CONSULTAR` | Consulta de reporte |

> Sí se implementa `CERRAR_SESION`. No se implementan `LOGIN_FALLIDO` ni un campo `resultado` de éxito/fallo; la bitácora actual registra acciones completadas.

## Anexo B — Fuera de alcance explícito

| Tema | Motivo | Decisión |
|---|---|---|
| Reportes oficiales / múltiples reportes | Todavía no se conoce el tema real; se necesita una base para gráficos | Sólo un reporte de muestra; se adapta en Fase 8 |
| Auditoría de intentos fallidos y resultado (`EXITO`/`FALLO`) | Se considera parte de ciberseguridad, no del alcance de reportes | No se implementa |
| Índices adicionales | Proyecto local de alcance corto | Se crean sólo si una consulta se vuelve lenta y se mide primero |
| Zonas / localidades | No se usan; `zona` quedó en desuso | Si se pide, módulo aparte con coordenadas |
| Entidad “cuadrilla” | No se modeló; se usó texto/dependencia | Reportar por dependencia o especialista |
| Materiales normalizados / inventario | Requiere un módulo completo | No se implementa |
| Dictamen de conformidad | El flujo actual no lo registra | Sólo si se solicita y se amplía el flujo |
| Duración de sesión | El logout se registra, pero no se calcula tiempo conectado | No se implementa |

---

## Anexo C — Chuleta de adaptación (1 página)

**Cuando te pidan el reporte real, haz esto en orden:**

1. **Define la salida:** título, periodo, 4–6 KPIs, una dimensión principal y si necesita detalle.
2. **Elige el dataset:** si es `quejas`, reutiliza `ReporteQuejaRepository`; si es otro, crea un método nuevo con las mismas proyecciones.
3. **Ajusta el SQL:** cambia `FROM`, `JOIN`, `WHERE` y el `CASE` de agrupación. Mantén los filtros opcionales con `CAST(:param AS tipo) IS NULL`.
4. **Ajusta el servicio:** títulos, etiquetas de KPIs, `filtrosAplicados` y mapeo de filas/detalle.
5. **Ajusta el frontend:** labels de filtros, opciones y textos. No dupliques componentes ni exportadores.
6. **Prueba:** consulta, PDF, Excel, sin datos y auditoría en `bitacora`.
7. **Actualiza este plan:** marca la Fase 8 y anota el reporte implementado.

**Archivos que casi nunca se tocan:**

- `ReporteResponseDTO` y DTOs de series/tabla (si el contrato alcanza).
- `ReporteChartService` (barras y pastel ya resueltos).
- `ReporteExportService` (PDF/Excel ya resueltos).
- `grafico-barras`, `grafico-pastel`, `grafico-apilado`.
- `reporte-queja.service.ts` (misma API).

**Comandos útiles:**

```bash
# Backend: compilar y probar sólo reportes
cd backend
./mvnw -q -DskipTests package
./mvnw -q -Dtest=ReporteQuejaServiceTest,ReporteChartServiceTest,ReporteExportServiceTest test

# Frontend: compilar
cd frontend
npm run build
```

**Criterio de “listo”:** el reporte responde con datos reales, los tres gráficos se ven en pantalla, PDF y Excel descargan con gráficos, y queda una fila `CONSULTAR`/`DESCARGAR` en `bitacora` con `tabla_afectada = 'reportes'`.

---

## Anexo D — Pantalla de bitácora (receta, no implementada)

Si más adelante piden “ver la bitácora”, **no hace falta backend nuevo**: ya existe y es inmutable.

**Endpoints disponibles:**

| Uso | Endpoint |
|---|---|
| Consulta paginada | `GET /admin/bitacora?usuario=&tabla=&accion=&idAfectado=&campoAfectado=&fechaInicio=&fechaFin=&pagina=0&tamanoPagina=20` |
| Exportar PDF | `GET /admin/bitacora/exportar/pdf` con los mismos filtros |
| Exportar Excel | `GET /admin/bitacora/exportar/excel` con los mismos filtros |

**Pantalla sugerida:**

1. Ruta nueva `/admin/bitacora` en `app.routes.ts` y opción “Bitácora” en el menú de administrador.
2. Filtros: usuario, tabla, acción, rango de fechas y página.
3. Tabla con columnas: fecha/hora, usuario, IP, tabla, acción, ID afectado, campo y descripción.
4. Botones PDF/Excel que llamen a los endpoints de exportación.
5. No agregar botones de editar ni eliminar: la bitácora es de sólo lectura y la BD bloquea `UPDATE`/`DELETE`/`TRUNCATE`.

> Esta pantalla queda como receta para no agregar una opción de menú que todavía no se pidió.

> **Fin del plan.** Si se retoma después de una interrupción: leer `Estado Actual`, buscar la primera tarea sin marcar y continuar desde ahí. No rehacer fases ya verificadas.
