package com.muni.backend.quejas.dto;

import lombok.Data;

@Data
public class AsignarInspectorDTO {
    private Integer quejaId;
    private Integer inspectorId;
    private String instrucciones;
}
