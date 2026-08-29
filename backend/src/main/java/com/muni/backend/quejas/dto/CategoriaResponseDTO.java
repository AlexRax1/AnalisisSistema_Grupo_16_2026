package com.muni.backend.quejas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Integer categoriaId;
    private String nombreCategoria;
    private String descripcion;
    private List<SubcategoriaResponseDTO> subcategorias;
}