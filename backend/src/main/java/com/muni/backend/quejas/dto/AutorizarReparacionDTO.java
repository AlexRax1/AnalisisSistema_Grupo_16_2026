package com.muni.backend.quejas.dto;

import lombok.Data;

@Data
public class AutorizarReparacionDTO {
    private Integer quejaId;
    private Integer especialistaId;
    private Integer dependenciaId;
    private Integer dependenciaAsignadaId;
    private String instrucciones;

    public Integer getDependenciaEfectivaId() {
        return dependenciaId != null ? dependenciaId : dependenciaAsignadaId;
    }
}
