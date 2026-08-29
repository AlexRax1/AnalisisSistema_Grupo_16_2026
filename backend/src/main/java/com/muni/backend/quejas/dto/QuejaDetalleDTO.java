package com.muni.backend.quejas.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuejaDetalleDTO {
    private Integer quejaId;
    private String correlativo;
    private String categoria;
    private String subcategoria;
    private Integer zona;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String descripcion;
    private String estadoActual;
    private String prioridadConfirmada;
    private LocalDateTime fechaRegistro;
    private List<EvidenciaDTO> evidencias;
}