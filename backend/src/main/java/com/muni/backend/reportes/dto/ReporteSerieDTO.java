package com.muni.backend.reportes.dto;

public record ReporteSerieDTO(
        String etiqueta,
        long valor,
        double porcentaje
) {
}
