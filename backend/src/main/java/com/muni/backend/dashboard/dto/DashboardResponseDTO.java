package com.muni.backend.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponseDTO(
        LocalDateTime fechaActualizacion,
        long total,
        List<DashboardContadorDTO> contadores,
        List<DashboardEstadoDTO> estados,
        List<DashboardQuejaDTO> recientes
) {
}
