package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoriaResponseDTO {
    private Integer subcategoriaId;
    private String nombreSubcategoria;
}
