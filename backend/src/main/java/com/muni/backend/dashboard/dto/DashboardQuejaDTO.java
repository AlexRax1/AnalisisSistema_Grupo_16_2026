package com.muni.backend.dashboard.dto;

import java.time.LocalDateTime;

public record DashboardQuejaDTO(
        Integer quejaId,
        String correlativo,
        LocalDateTime fechaRegistro,
        String ciudadano,
        String categoria,
        String estadoActual,
        String prioridad,
        String dependencia
) {
}
