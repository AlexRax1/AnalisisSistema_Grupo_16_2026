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
public class TareaAsignadaDTO {
    private Integer quejaId;
    private String correlativo;
    private String estadoActual;
    private String prioridadConfirmada;
    private Integer categoriaId;
    private String categoria;
    private Integer subcategoriaId;
    private String subcategoria;
    private Integer zona;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private String faseRequerida;
    private String faseAdministrativa;
    private String ciudadanoNombre;
    private List<String> fotos;
    private List<EvidenciaDTO> evidencias;
    private String mensaje;
}
