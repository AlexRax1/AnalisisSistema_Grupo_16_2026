package com.muni.backend.reportes.dto;

import java.util.List;

public record ReporteTablaDTO(
        String titulo,
        List<String> columnas,
        List<ReporteFilaDTO> filas
) {
}
