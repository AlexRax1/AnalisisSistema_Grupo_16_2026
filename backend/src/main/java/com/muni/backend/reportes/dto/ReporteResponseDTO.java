package com.muni.backend.reportes.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteResponseDTO(
        String titulo,
        String subtitulo,
        LocalDateTime fechaEmision,
        Map<String, String> filtrosAplicados,
        List<ReporteKpiDTO> kpis,
        List<ReporteSerieDTO> series,
        ReporteTablaDTO tabla,
        List<ReporteDetalleDTO> detalle,
        long totalDetalle,
        int limiteDetalle
) {
}
