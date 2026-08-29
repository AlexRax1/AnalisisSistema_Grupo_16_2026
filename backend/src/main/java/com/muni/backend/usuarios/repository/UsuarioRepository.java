package com.muni.backend.usuarios.repository;

import com.muni.backend.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByDpi(String dpi);
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByDpi(String dpi);
    Optional<Usuario> findByCorreo(String correo);
}