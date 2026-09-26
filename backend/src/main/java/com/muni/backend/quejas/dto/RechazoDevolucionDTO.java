package com.muni.backend.quejas.dto;

import lombok.Data;

@Data
public class RechazoDevolucionDTO {
    private Integer quejaId;
    private String motivoRechazo;
    private String descripcionDetallada;
    private Boolean esRechazoDefinitivo;
}
