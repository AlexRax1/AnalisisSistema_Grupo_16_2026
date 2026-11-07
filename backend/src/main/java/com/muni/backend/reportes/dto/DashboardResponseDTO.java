package com.muni.backend.reportes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponseDTO(
        LocalDateTime fechaActualizacion,
        long total,
        List<DashboardContadorDTO> contadores,
        List<DashboardEstadoDTO> estados,
        List<ReporteDetalleDTO> recientes
) {
}
