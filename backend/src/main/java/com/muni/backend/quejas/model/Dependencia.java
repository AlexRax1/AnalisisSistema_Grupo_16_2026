package com.muni.backend.quejas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dependencias")
@Data
public class Dependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependencia_id")
    private Integer dependenciaId;

    @Column(name = "nombre_dependencia", length = 100, unique = true, nullable = false)
    private String nombreDependencia;

    @Column(name = "codigo_dependencia", length = 20, unique = true, nullable = false)
    private String codigoDependencia;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;
}
