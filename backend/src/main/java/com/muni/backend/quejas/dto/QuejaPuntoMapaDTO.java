package com.muni.backend.quejas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuejaPuntoMapaDTO {
    private Integer quejaId;
    private String correlativo;
    private String categoria;
    private Integer zona;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estadoActual;
    private String prioridadConfirmada;
}