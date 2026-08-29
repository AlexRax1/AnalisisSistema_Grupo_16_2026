package com.muni.backend.security.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rol_user")
@Data
public class RolUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_user_id")
    private Integer rolUserId;

    @Column(name = "nombre_rol", length = 30)
    private String nombreRol;
}
