package com.muni.backend.reportes.repository.proyeccion;

import java.math.BigDecimal;

public interface ReporteKpiProjection {
    Long getTotal();

    Long getCerradas();

    Long getRechazadas();

    Long getEnProceso();

    BigDecimal getPromedioDiasCierre();
}
