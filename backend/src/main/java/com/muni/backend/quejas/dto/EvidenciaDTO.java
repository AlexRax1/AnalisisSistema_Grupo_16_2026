package com.muni.backend.quejas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaDTO {
    private Integer evidenciaId;
    private String urlArchivo;
    private String nombreArchivo;
    private String formato;
    private LocalDateTime fechaSubida;
}