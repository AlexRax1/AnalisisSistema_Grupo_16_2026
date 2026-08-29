package com.muni.backend.usuarios.model;

import com.muni.backend.security.model.Credencial;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    // Relación directa 1 a 1 con la credencial de autenticación
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Credencial credencial;

    @Column(name = "dpi", length = 13, unique = true, nullable = false)
    private String dpi;

    @Column(name = "nombres", length = 100, nullable = false)
    private String nombres;

    @Column(name = "apellidos", length = 100, nullable = false)
    private String apellidos;

    @Column(name = "correo", length = 150, unique = true, nullable = false)
    private String correo;

    @Column(name = "telefono", length = 8, nullable = false)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "usuario_creacion", length = 150)
    private String usuarioCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificacion", length = 150)
    private String usuarioModificacion;
}