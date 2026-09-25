package com.muni.backend.quejas.dto;

import lombok.Data;

@Data
public class QuejaDerivadaDTO {
    private String correlativoPadre; // Correlativo de la queja original
    private String tipoDerivacion;    // 'AGRAVAMIENTO' o 'REINCIDENCIA'
    private String descripcion;       // Entre 20 y 500 caracteres
}
