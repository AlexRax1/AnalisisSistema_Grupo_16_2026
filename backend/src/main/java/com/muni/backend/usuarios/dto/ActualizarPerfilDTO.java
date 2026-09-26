package com.muni.backend.usuarios.dto;

import lombok.Data;

@Data
public class ActualizarPerfilDTO {
    private String telefono;
    private String direccion;
    private String correo;

    // Opcionales para cambio de contraseña
    private String passwordActual;
    private String nuevaPassword;
    private String confirmarNuevaPassword;
}
