package com.muni.backend.quejas.dto.funcionario;

import lombok.Data;

@Data
public class AsignarInspectorDTO {
    private Integer quejaId;
    private Integer inspectorId;
    private String instrucciones;
}
