package com.muni.backend.quejas.dto.ciudadano;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuejaRegistroResponse {
    private String correlativo;
    private String mensaje;
}
