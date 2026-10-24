package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeDetalleDTO {
    private Integer informeId;
    private String tipoInforme;
    private String autorNombre;
    private String autorRol;
    private Boolean problemaVerificado;
    private String gravedad;
    private String recursosSugeridos;
    private String instruccionesCuadrilla;
    private String materialesUtilizados;
    private BigDecimal horasTrabajadas;
    private LocalDateTime fechaFinTrabajo;
    private String dictamenCalidad;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private java.util.List<EvidenciaDTO> evidencias;
    private java.util.List<String> fotos;
}
