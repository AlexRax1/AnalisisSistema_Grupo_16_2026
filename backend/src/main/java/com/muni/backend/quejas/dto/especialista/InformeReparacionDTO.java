package com.muni.backend.quejas.dto.especialista;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InformeReparacionDTO {

    @NotNull(message = "La descripción del trabajo realizado es obligatoria.")
    @Size(min = 20, max = 500, message = "La descripción debe tener entre 20 y 500 caracteres.")
    private String descripcion;

    @NotBlank(message = "Debe detallar los materiales y repuestos utilizados.")
    private String materialesUtilizados;

    @NotNull(message = "Debe indicar las horas trabajadas.")
    @DecimalMin(value = "0.5", message = "Las horas trabajadas deben ser al menos 0.5.")
    @DecimalMax(value = "24.0", message = "Las horas trabajadas no pueden superar 24.")
    private BigDecimal horasTrabajadas;

    private String instruccionesCuadrilla;
}
