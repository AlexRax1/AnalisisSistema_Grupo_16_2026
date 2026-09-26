package com.muni.backend.quejas.model;

import com.muni.backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "informes_queja")
@Data
public class InformeQueja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "informe_id")
    private Integer informeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queja_id", nullable = false)
    private Queja queja;

    @Column(name = "tipo_informe", length = 30, nullable = false)
    private String tipoInforme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "problema_verificado")
    private Boolean problemaVerificado;

    @Column(name = "gravedad", length = 20)
    private String gravedad;

    @Column(name = "recursos_sugeridos", columnDefinition = "TEXT")
    private String recursosSugeridos;

    @Column(name = "instrucciones_cuadrilla", columnDefinition = "TEXT")
    private String instruccionesCuadrilla;

    @Column(name = "materiales_utilizados", columnDefinition = "TEXT")
    private String materialesUtilizados;

    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(name = "fecha_fin_trabajo")
    private LocalDateTime fechaFinTrabajo;

    @Column(name = "dictamen_calidad", length = 20)
    private String dictamenCalidad;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
