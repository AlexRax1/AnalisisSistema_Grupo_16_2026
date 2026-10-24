package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {
    private Long id;
    private String dpi;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private String rol;
    private String dependencia;
    private Boolean activo;
}
