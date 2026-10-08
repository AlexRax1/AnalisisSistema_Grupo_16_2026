package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDetalleDTO {
    private Integer historialId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String cambiadoPorNombre;
    private String comentario;
    private LocalDateTime fechaCambio;
}
