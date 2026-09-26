package com.muni.backend.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenDTO {
    private Integer usuarioId;
    private String nombres;
    private String apellidos;
    private String correo;
    private String dependencia;
}
