package com.muni.backend.security.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "credenciales")
@Data
public class Credencial implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", unique = true, nullable = false, length = 200)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", referencedColumnName = "rol_user_id", nullable = false)
    private RolUser rolUser;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    // --- CAMPOS DE AUDITORIA ---
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "usuario_creacion", length = 150)
    private String usuarioCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificacion", length = 150)
    private String usuarioModificacion;

    // --- MÉTODOS DE USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.rolUser == null || this.rolUser.getNombreRol() == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority(this.rolUser.getNombreRol()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}