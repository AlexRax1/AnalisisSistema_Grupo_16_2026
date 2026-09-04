# Resumen Ejecutivo de Casos de Uso - Sistema MuniQuejas

Este documento consolida el resumen de los **17 Casos de Uso (CU)** que conforman la especificación del Sistema de Gestión de Quejas Municipales (**MuniQuejas**).

---

## 📌 Reglas Clave de Negocio y Arquitectura

* **Sin División por Zonas**: Cobertura municipal completa (287 km²) para todo el personal operativo.
* **4 Categorías Técnicas**: 
  1. *Alumbrado Público*
  2. *Drenajes y Alcantarillado*
  3. *Vialidad y Espacios Públicos*
  4. *Limpieza y Áreas Verdes*
* **Priorización Bajo Demanda**: El sistema agrupa reportes geográficamente cercanos sobre la misma problemática para priorizar casos con mayor acumulación ciudadana.
* **Quejas Hijas**:
  * *Agravamiento*: Reporte de empeoramiento sobre una queja en estado activo.
  * *Reincidencia / Inconformidad*: Reporte de falla post-cierre sobre una queja terminada (se enlaza al expediente original).

---

## 🔑 1. Módulo de Autenticación y Perfil de Usuario

### **CU - Inicio de Sesión**
* **Actor Principal**: Todos los roles (*Ciudadano, Funcionario Municipal, Inspector de Campo, Especialista Técnico, Administrador del Sistema*).
* **Propósito**: Validar las credenciales de acceso (correo electrónico y contraseña con hash BCrypt) y generar un token JWT de sesión con los permisos del rol asignado.

### **CU - Registrarse en el Sistema**
* **Actor Principal**: Ciudadano (Vecino).
* **Propósito**: Permitir el autoregistro público de vecinos completando su DPI (13 dígitos inmutables), nombres, apellidos, teléfono, correo y contraseña. Congela la identidad del usuario para respaldo legal de las quejas.

### **CU - Actualizar Datos de Perfil**
* **Actor Principal**: Ciudadano (Vecino).
* **Propósito**: Permitir al usuario autenticado modificar su número de teléfono, dirección de residencia o correo electrónico, y cambiar su contraseña de acceso. Mantiene inmutables el DPI y nombres legales.

---

## 📝 2. Módulo Ciudadano: Registro y Trazabilidad

### **CU - Registrar Queja**
* **Actor Principal**: Ciudadano (Vecino).
* **Propósito**: Permite radicar un reporte seleccionando categoría/subcategoría, indicando la ubicación física en un mapa interactivo (latitud/longitud), redactando la descripción (20 a 500 caracteres) y adjuntando de 1 a 3 fotografías. Asigna correlativo único `QUE-2026-XXXXXX` y estado `REGISTRADA`.

### **CU - Consultar Estado e Historial de Quejas**
* **Actor Principal**: Ciudadano (Vecino).
* **Propósito**: Proporcionar la línea de tiempo en vivo y trazabilidad de los reportes del vecino. Habilita el registro de **Agravamientos** (en quejas activas) o **Reincidencias / Inconformidades** (en quejas cerradas), y permite la descarga de la Constancia Oficial de Cierre en PDF.

---

## 🔍 3. Módulo Operativo: Triaje, Inspección y Despacho

### **CU - Validar, Priorizar y Asignar Queja a Inspector**
* **Actor Principal**: Funcionario Municipal.
* **Propósito**: Evaluar quejas en estado `REGISTRADA`. Permite rechazar fundadamente solicitudes no procedentes (`RECHAZADA`) o confirmar la prioridad por demanda comunitaria acumulada y despachar el caso a un Inspector de Campo disponible (`EN INSPECCIÓN`).

### **CU - Consultar Quejas Asignadas**
* **Actor Principal**: Inspector de Campo.
* **Propósito**: Desplegar la bandeja de trabajo asignada al inspector, clasificando expedientes pendientes de inspección inicial (`EN INSPECCIÓN`) o auditoría de calidad (`EN VALIDACIÓN DE REPARACIÓN`), facilitando ruta geográfica y antecedentes.

### **CU - Registrar Informe de Inspección Inicial y Asignar a Especialista**
* **Actor Principal**: Inspector de Campo.
* **Propósito**: Documentar la primera visita presencial in situ. Registra el diagnóstico del daño, clasifica la gravedad (*Leve, Moderada, Grave, Crítica*), descarta falsas alarmas, adjunta fotografías de constatación y deriva la orden a la cuadrilla técnica competente (`EN REPARACIÓN TÉCNICA`).

---

## 🛠️ 4. Módulo Operativo: Solución Técnica y Control de Calidad

### **CU - Consultar Órdenes de Trabajo Asignadas**
* **Actor Principal**: Especialista Técnico (Jefe de Cuadrilla).
* **Propósito**: Mostrar la cola de órdenes de trabajo asignadas a la cuadrilla técnica, priorizadas por severidad y demanda, para la planificación y salida del personal a campo.

### **CU - Registrar Informe de Solución Técnica**
* **Actor Principal**: Especialista Técnico (Jefe de Cuadrilla).
* **Propósito**: Registrar la finalización de los trabajos físicos en terreno. Detalla la labor realizada, materiales/insumos consumidos, horas hombre invertidas y carga fotos obligatorias del resultado. Transiciona el estado a `EN VALIDACIÓN DE REPARACIÓN`.

### **CU - Validar Reparación y Emitir Dictamen de Conformidad**
* **Actor Principal**: Inspector de Campo.
* **Propósito**: Realizar la segunda visita en terreno para auditar la calidad de la obra. Si no cumple estándares, devuelve el expediente a la cuadrilla con observaciones (`EN REPARACIÓN TÉCNICA`). Si es conforme, emite el visto bueno con fotografías de auditoría y avanza a `PENDIENTE DE CIERRE`.

### **CU - Aprobar Resolución y Cerrar Queja**
* **Actor Principal**: Funcionario Municipal.
* **Propósito**: Realizar la revisión administrativa final del expediente consolidado (diagnóstico, solución, vistos buenos y fotos antes/después). Al aprobar la resolución, la queja cambia a `SOLUCIONADA / CERRADA`, notifica al ciudadano y cierra en cascada las quejas hijas vinculadas.

---

## 📊 5. Módulo de Administración, Reportería y Catálogo General

### **CU - Consultar Catálogo e Historial de Quejas**
* **Actor Principal**: Funcionario Municipal / Administrador.
* **Propósito**: Permitir la búsqueda y filtrado de todas las quejas del municipio por correlativo, fecha, ciudadano, categoría o estado para consulta institucional.

### **CU - Gestionar Personal y Roles del Sistema**
* **Actor Principal**: Administrador del Sistema.
* **Propósito**: Centralizar el alta, actualización o desactivación de cuentas del personal operativo (Funcionarios, Inspectores y Especialistas), asignando sus roles y dependencias técnicas.

### **CU - Reporte de Quejas por Rango de Fechas y Estado**
* **Actor Principal**: Funcionario Municipal / Administrador.
* **Propósito**: Generar estadísticas y tablas sobre el volumen de quejas ingresadas, resueltas, rechazadas y en proceso en un periodo dado para medir tiempos de respuesta institucionales.

### **CU - Reporte de Quejas por Categoría y Ubicación**
* **Actor Principal**: Funcionario Municipal / Administrador.
* **Propósito**: Identificar el volumen de incidencias por cada una de las 4 categorías técnicas y desplegar un mapa de calor de concentración geográfica para la toma de decisiones estratégicas.

### **CU - Reporte de Rendimiento y Resoluciones por Cuadrilla**
* **Actor Principal**: Funcionario Municipal / Administrador.
* **Propósito**: Evaluar la productividad y eficiencia de las cuadrillas técnicas, midiendo tiempos promedios de reparación, re-trabajos por rechazo y consumo de materiales.

---

## 📑 Tabla Resumen de Casos de Uso

| Código / Nombre del Caso de Uso | Actor Principal | Estado Inicial | Estado Final |
|---|---|---|---|
| **CU - Inicio de Sesión** | Todos | N/A | Sesión Iniciada (Token JWT) |
| **CU - Registrarse en el Sistema** | Ciudadano | N/A | Cuenta Creada |
| **CU - Actualizar Datos de Perfil** | Ciudadano | Autenticado | Perfil Actualizado |
| **CU - Registrar Queja** | Ciudadano | N/A | `REGISTRADA` |
| **CU - Consultar Estado e Historial** | Ciudadano | Autenticado | Trazabilidad / Queja Hija |
| **CU - Validar Priorizar y Asignar** | Funcionario | `REGISTRADA` | `EN INSPECCIÓN` o `RECHAZADA` |
| **CU - Consultar Quejas Asignadas** | Inspector | Autenticado | Consulta de Ruta |
| **CU - Registrar Inspección Inicial** | Inspector | `EN INSPECCIÓN` | `EN REPARACIÓN TÉCNICA` |
| **CU - Consultar Órdenes Asignadas** | Especialista | Autenticado | Consulta de Cola |
| **CU - Registrar Solución Técnica** | Especialista | `EN REPARACIÓN TÉCNICA` | `EN VALIDACIÓN DE REPARACIÓN` |
| **CU - Validar Reparación y Dictamen** | Inspector | `EN VALIDACIÓN DE REPARACIÓN` | `PENDIENTE DE CIERRE` |
| **CU - Aprobar Resolución y Cerrar** | Funcionario | `PENDIENTE DE CIERRE` | `SOLUCIONADA / CERRADA` |
| **CU - Consultar Catálogo General** | Funcionario/Admin | N/A | Consulta / Filtros |
| **CU - Gestionar Personal y Roles** | Administrador | N/A | Cuentas Internas Gestionadas |
| **CU - Reporte Fechas y Estado** | Funcionario/Admin | N/A | Reporte Generado (PDF/XLS) |
| **CU - Reporte Categoría y Ubicación** | Funcionario/Admin | N/A | Mapa de Calor / Reporte |
| **CU - Reporte Rendimiento Cuadrilla**| Funcionario/Admin | N/A | Métricas de Cuadrilla |
