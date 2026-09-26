package com.muni.backend.quejas.model;

import com.muni.backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados_queja")
@Data
public class HistorialEstadoQueja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historial_id")
    private Integer historialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queja_id", nullable = false)
    private Queja queja;

    @Column(name = "estado_anterior", length = 40)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 40, nullable = false)
    private String estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cambiado_por_id", nullable = false)
    private Usuario cambiadoPor;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio = LocalDateTime.now();
}
