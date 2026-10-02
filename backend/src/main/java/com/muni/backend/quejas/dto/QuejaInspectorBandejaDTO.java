package com.muni.backend.quejas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuejaInspectorBandejaDTO {
    private Integer quejaId;
    private String correlativo;
    private String categoria;
    private String subcategoria;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String prioridadConfirmada;
    private LocalDateTime fechaRegistro;
    private String tipoRegistro;
    private Integer quejaOrigenId;
    private List<String> fotosCiudadano;
}
