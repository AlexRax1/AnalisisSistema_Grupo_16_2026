package com.muni.backend.reportes.dto;

public record ReporteFilaDTO(
        String etiqueta,
        long total,
        long cerradas,
        long enProceso,
        long rechazadas,
        double porcentaje
) {
}
