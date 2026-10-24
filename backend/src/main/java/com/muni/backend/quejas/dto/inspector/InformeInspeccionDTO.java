package com.muni.backend.quejas.dto.inspector;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InformeInspeccionDTO {

    @NotNull(message = "Debe indicar si el problema fue verificado.")
    private Boolean problemaVerificado;

    @NotNull(message = "Debe indicar la gravedad del problema.")
    private String gravedad; // LEVE, MODERADA, GRAVE, CRITICA

    private String recursosSugeridos;

    @NotNull(message = "La descripción del hallazgo es obligatoria.")
    @Size(min = 20, max = 500, message = "La descripción debe tener entre 20 y 500 caracteres.")
    private String descripcion;
}
