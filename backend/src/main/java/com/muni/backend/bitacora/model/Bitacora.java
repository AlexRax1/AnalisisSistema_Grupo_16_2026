package com.muni.backend.bitacora.model;

import com.muni.backend.security.model.Credencial;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// Registro de auditoría de las acciones del sistema.
@Data
@Entity
@Table(name = "bitacora", indexes = {
        @Index(name = "idx_bitacora_fecha_hora", columnList = "fecha_hora"),
        @Index(name = "idx_bitacora_tabla_afectada", columnList = "tabla_afectada")
})
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Integer registroId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Credencial usuario;

    @Column(name = "usuario_accion", length = 200)
    private String usuarioAccion;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "tabla_afectada", length = 100)
    private String tablaAfectada;

    @Column(name = "id_afectado", length = 150)
    private String idAfectado;

    @Column(name = "campo_afectado", length = 100)
    private String campoAfectado;

    @Column(name = "accion", length = 50)
    private String accion;

    @Column(name = "registro_antiguo", columnDefinition = "TEXT")
    private String registroAntiguo;

    @Column(name = "registro_nuevo", columnDefinition = "TEXT")
    private String registroNuevo;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }
}
