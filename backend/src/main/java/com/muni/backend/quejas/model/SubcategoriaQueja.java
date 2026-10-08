package com.muni.backend.quejas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subcategorias_queja")
@Data
public class SubcategoriaQueja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategoria_id")
    private Integer subcategoriaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaQueja categoria;

    @Column(name = "nombre_subcategoria", length = 100, nullable = false)
    private String nombreSubcategoria;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";
}
