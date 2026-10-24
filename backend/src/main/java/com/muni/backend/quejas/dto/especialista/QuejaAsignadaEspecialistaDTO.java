package com.muni.backend.quejas.dto.especialista;

import com.muni.backend.quejas.dto.shared.EvidenciaDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuejaAsignadaEspecialistaDTO {
    private Integer quejaId;
    private String correlativo;
    private String estadoActual;
    private String prioridadConfirmada;
    private String categoria;
    private String subcategoria;
    private Integer zona;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private String dependenciaNombre;
    private String instruccionesFuncionario;
    private List<EvidenciaDTO> evidencias;
}
