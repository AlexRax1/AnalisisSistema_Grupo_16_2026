package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuejaResumenDTO {
    private Integer quejaId;
    private String correlativo;
    private String tipoRegistro;
    private String categoria;
    private String subcategoria;
    private Integer zona;
    private String direccionExacta;
    private String estadoActual;
    private String prioridadConfirmada;
    private LocalDateTime fechaRegistro;
    private String ciudadanoNombre;
}
