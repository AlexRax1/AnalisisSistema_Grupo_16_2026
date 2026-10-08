package com.muni.backend.quejas.dto.funcionario;

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
