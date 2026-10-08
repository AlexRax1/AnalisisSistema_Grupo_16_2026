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

    /**
     * Busca los IDs de funcionarios municipales activos ordenados por menor cantidad de quejas activas.
     */
    @Query("""
        SELECT u.usuarioId FROM Usuario u
        JOIN u.credencial c
        JOIN c.rolUser r
        LEFT JOIN com.muni.backend.quejas.model.Queja q
            ON q.funcionario = u AND q.estadoActual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')
        WHERE r.nombreRol = 'FUNCIONARIO_MUNICIPAL'
          AND u.estado = 'ACTIVO'
          AND c.estado = 'ACTIVO'
        GROUP BY u.usuarioId
        ORDER BY COUNT(q) ASC, u.usuarioId ASC
        """)
    List<Integer> buscarIdFuncionarioMenorCarga(org.springframework.data.domain.Pageable pageable);

    /**
     * Busca los IDs de inspectores de campo activos ordenados por menor cantidad de quejas en inspección.
     */
    @Query("""
        SELECT u.usuarioId FROM Usuario u
        JOIN u.credencial c
        JOIN c.rolUser r
        LEFT JOIN com.muni.backend.quejas.model.Queja q
            ON q.inspector = u AND q.estadoActual = 'EN INSPECCIÓN'
        WHERE r.nombreRol = 'INSPECTOR_CAMPO'
          AND u.estado = 'ACTIVO'
          AND c.estado = 'ACTIVO'
        GROUP BY u.usuarioId
        ORDER BY COUNT(q) ASC, u.usuarioId ASC
        """)
    List<Integer> buscarIdInspectorMenorCarga(org.springframework.data.domain.Pageable pageable);

    /*
       Busca los IDs de especialistas técnicos activos de una dependencia específica,
       ordenados por menor cantidad de quejas en reparación técnica.
     */
    @Query("""
        SELECT u.usuarioId FROM Usuario u
        JOIN u.credencial c
        JOIN c.rolUser r
        LEFT JOIN com.muni.backend.quejas.model.Queja q
            ON q.especialista = u AND q.estadoActual = 'EN REPARACIÓN TÉCNICA'
        WHERE r.nombreRol = 'ESPECIALISTA_TECNICO'
          AND u.estado = 'ACTIVO'
          AND c.estado = 'ACTIVO'
          AND u.dependencia.dependenciaId = :dependenciaId
        GROUP BY u.usuarioId
        ORDER BY COUNT(q) ASC, u.usuarioId ASC
        """)
    List<Integer> buscarIdEspecialistaDependenciaMenorCarga(
            @Param("dependenciaId") Integer dependenciaId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Busca los IDs de especialistas técnicos activos a nivel global,
     * ordenados por menor cantidad de quejas en reparación técnica.
     */
    @Query("""
        SELECT u.usuarioId FROM Usuario u
        JOIN u.credencial c
        JOIN c.rolUser r
        LEFT JOIN com.muni.backend.quejas.model.Queja q
            ON q.especialista = u AND q.estadoActual = 'EN REPARACIÓN TÉCNICA'
        WHERE r.nombreRol = 'ESPECIALISTA_TECNICO'
          AND u.estado = 'ACTIVO'
          AND c.estado = 'ACTIVO'
        GROUP BY u.usuarioId
        ORDER BY COUNT(q) ASC, u.usuarioId ASC
        """)
    List<Integer> buscarIdEspecialistaGlobalMenorCarga(org.springframework.data.domain.Pageable pageable);
}