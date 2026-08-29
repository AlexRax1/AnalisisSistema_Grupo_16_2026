package com.muni.backend.usuarios.dto;

import lombok.Data;

@Data
public class UsuarioRegistroDTO {

    private String correo;
    private String password;
    private String confirmPassword;

    private String dpi;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String direccion;
}