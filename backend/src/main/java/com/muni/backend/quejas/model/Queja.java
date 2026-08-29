package com.muni.backend.quejas.model;

import com.muni.backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quejas")
@Data
public class Queja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queja_id")
    private Integer quejaId;

    @Column(name = "correlativo", unique = true, nullable = false, length = 30)
    private String correlativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Usuario ciudadano;

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @Column(name = "subcategoria_id")
    private Integer subcategoriaId;

    @Column(name = "zona", nullable = false)
    private Integer zona;

    @Column(name = "direccion_exacta", nullable = false, columnDefinition = "TEXT")
    private String direccionExacta;

    @Column(name = "punto_referencia", columnDefinition = "TEXT")
    private String puntoReferencia;

    @Column(name = "latitud", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitud;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado_actual", length = 40, nullable = false)
    private String estadoActual = "REGISTRADA";

    @Column(name = "prioridad_sugerida", length = 20)
    private String prioridadSugerida;

    @Column(name = "prioridad_confirmada", length = 20)
    private String prioridadConfirmada = "MEDIA";

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}