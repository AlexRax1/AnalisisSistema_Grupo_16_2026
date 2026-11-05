package com.muni.backend.reportes.dto;

import java.util.List;

public record ReporteOpcionesDTO(
        List<OpcionDTO> categorias,
        List<OpcionDTO> subcategorias,
        List<OpcionDTO> dependencias,
        List<OpcionDTO> especialistas,
        List<String> estados,
        List<String> prioridades,
        List<String> agrupaciones
) {
}
