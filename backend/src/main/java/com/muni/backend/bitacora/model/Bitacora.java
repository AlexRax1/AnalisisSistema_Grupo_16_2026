package com.muni.backend.bitacora.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// Registro de auditoría de las acciones del sistema.
// Es inmutable: sólo se inserta, nunca se actualiza ni se elimina.
@Data
@Entity
@Table(name = "bitacora", indexes = {
        @Index(name = "idx_bitacora_fecha_hora", columnList = "fecha_hora"),
        @Index(name = "idx_bitacora_tabla_afectada", columnList = "tabla_afectada"),
        @Index(name = "idx_bitacora_direccion_ip", columnList = "direccion_ip"),
        @Index(name = "idx_bitacora_id_afectado", columnList = "id_afectado"),
        @Index(name = "idx_bitacora_campo_afectado", columnList = "campo_afectado")
})
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id", updatable = false)
    private Integer registroId;

    // Se guarda como número, sin FK: borrar una credencial no debe alterar el histórico.
    @Column(name = "user_id", updatable = false)
    private Integer userId;

    @Column(name = "usuario_accion", length = 200, updatable = false)
    private String usuarioAccion;

    @Column(name = "direccion_ip", length = 45, updatable = false)
    private String direccionIp;

    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tabla_afectada", length = 100, updatable = false)
    private String tablaAfectada;

    @Column(name = "id_afectado", length = 150, updatable = false)
    private String idAfectado;

    @Column(name = "campo_afectado", length = 100, updatable = false)
    private String campoAfectado;

    @Column(name = "accion", length = 50, updatable = false)
    private String accion;

    @Column(name = "descripcion", length = 500, updatable = false)
    private String descripcion;

    @Column(name = "registro_antiguo", columnDefinition = "TEXT", updatable = false)
    private String registroAntiguo;

    @Column(name = "registro_nuevo", columnDefinition = "TEXT", updatable = false)
    private String registroNuevo;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }
}
