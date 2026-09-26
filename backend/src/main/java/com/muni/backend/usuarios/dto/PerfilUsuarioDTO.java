package com.muni.backend.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioDTO {
    private String dpi;         // Solo lectura (RN06)
    private String nombres;     // Solo lectura (RN06)
    private String apellidos;   // Solo lectura (RN06)
    private String telefono;    // Editable (RN04)
    private String direccion;   // Editable
    private String correo;      // Editable (RN03)
}
