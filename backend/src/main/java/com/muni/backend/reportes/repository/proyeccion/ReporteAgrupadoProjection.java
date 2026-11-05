package com.muni.backend.reportes.repository.proyeccion;

import java.math.BigDecimal;

public interface ReporteAgrupadoProjection {
    String getEtiqueta();

    Long getTotal();

    Long getCerradas();

    Long getEnProceso();

    Long getRechazadas();

    BigDecimal getPorcentaje();
}
