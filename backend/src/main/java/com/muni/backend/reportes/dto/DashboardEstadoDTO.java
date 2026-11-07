package com.muni.backend.reportes.dto;

public record DashboardEstadoDTO(
        String estado,
        String grupo,
        long total,
        double porcentaje
) {
}
