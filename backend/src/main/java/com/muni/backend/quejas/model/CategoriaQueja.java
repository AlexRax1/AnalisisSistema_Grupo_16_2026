package com.muni.backend.quejas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categorias_queja")
@Data
public class CategoriaQueja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "nombre_categoria", length = 100, unique = true, nullable = false)
    private String nombreCategoria;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";
}
