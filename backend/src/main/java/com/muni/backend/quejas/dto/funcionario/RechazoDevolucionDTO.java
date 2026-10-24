package com.muni.backend.quejas.dto.funcionario;

import lombok.Data;

@Data
public class RechazoDevolucionDTO {
    private Integer quejaId;
    private String motivoRechazo;
    private String descripcionDetallada;
    private Boolean esRechazoDefinitivo;
}
