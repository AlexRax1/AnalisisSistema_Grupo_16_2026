package com.muni.backend.quejas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuejaRegistroDTO {
    private Integer categoriaId;
    private Integer subcategoriaId;
    private Integer zona;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String descripcion;
}