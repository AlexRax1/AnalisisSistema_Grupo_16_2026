package com.muni.backend.reportes.dto;

public record DashboardContadorDTO(
        String clave,
        String etiqueta,
        long valor,
        double porcentaje,
        String icono
) {
}
