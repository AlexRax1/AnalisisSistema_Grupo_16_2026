package com.muni.backend.reportes.dto;

import java.time.LocalDateTime;

public record ReporteDetalleDTO(
        Integer quejaId,
        String correlativo,
        LocalDateTime fechaRegistro,
        String ciudadano,
        String dpi,
        String categoria,
        String subcategoria,
        String estadoActual,
        String prioridad,
        String inspector,
        String especialista,
        String dependencia,
        LocalDateTime fechaCierre,
        String direccionExacta
) {
}
