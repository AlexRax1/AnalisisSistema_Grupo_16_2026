package com.muni.backend.dashboard.dto;

public record DashboardContadorDTO(
        String clave,
        String etiqueta,
        long valor,
        double porcentaje,
        String icono
) {
}
