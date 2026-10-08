package com.muni.backend.quejas.dto.especialista;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InformeReparacionDTO {

    @NotNull(message = "La descripción del trabajo realizado es obligatoria.")
    @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres.")
    private String descripcion;

    private String materialesUtilizados;

    private BigDecimal horasTrabajadas;

    private String instruccionesCuadrilla;
}
