package com.muni.backend.dashboard.dto;

public record DashboardEstadoDTO(
        String estado,
        String grupo,
        long total,
        double porcentaje
) {
}
