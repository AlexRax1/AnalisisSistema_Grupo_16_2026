package com.muni.backend.usuarios.repository;

import com.muni.backend.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByDpi(String dpi);
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByDpi(String dpi);
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Busca usuarios activos por nombre de rol.
     */
    @Query("""
        SELECT u FROM Usuario u
        JOIN u.credencial c
        JOIN c.rolUser r
        WHERE r.nombreRol = :nombreRol
          AND u.estado = 'ACTIVO'
        ORDER BY u.apellidos ASC, u.nombres ASC
        """)
    List<Usuario> findByRolActivo(@Param("nombreRol") String nombreRol);

    /**
     * Busca un usuario por su credencial username.
     */
    Optional<Usuario> findByCredencial_Username(String username);

    /**
     * Busca un usuario por su credencial user_id.
     */
    Optional<Usuario> findByCredencial_UserId(Integer userId);
}