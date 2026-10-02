package com.muni.backend.quejas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroInspeccionDTO {

    @NotNull(message = "El campo problemaVerificado es obligatorio.")
    private Boolean problemaVerificado;

    private String gravedad;

    @NotBlank(message = "El diagnóstico es obligatorio.")
    @Size(min = 20, message = "El diagnóstico debe tener al menos 20 caracteres.")
    private String diagnostico;

    private String recursosSugeridos;
    private String instruccionesCuadrilla;

    private List<String> fotos;
}
