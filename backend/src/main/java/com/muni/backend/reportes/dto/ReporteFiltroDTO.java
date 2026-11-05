package com.muni.backend.reportes.dto;

import java.time.LocalDate;

public record ReporteFiltroDTO(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String estado,
        Integer categoriaId,
        String prioridad,
        String agrupacion
) {
}
