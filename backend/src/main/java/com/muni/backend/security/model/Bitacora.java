package com.muni.backend.security.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bitacora")
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Integer registroId;

    // Cambiado de com.aeropuerto.auth.model.Credencial a Credencial directa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Credencial usuario;

    @Column(name = "microservicio_afectado", length = 50)
    private String microservicioAfectado;

    @Column(length = 150)
    private String endpoint;

    @Column(length = 200)
    private String accion;

    @Column(name = "id_afectado", length = 150)
    private String idAfectado;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }
}