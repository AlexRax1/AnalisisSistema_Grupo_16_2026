package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.Queja;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuejaRepository extends JpaRepository<Queja, Integer> {

    Optional<Queja> findByCorrelativo(String correlativo);

    List<Queja> findByCiudadano_UsuarioIdOrderByQuejaIdDesc(Integer usuarioId);

    @Query(value = """
        SELECT COUNT(*)
        FROM quejas
        WHERE categoria_id = :categoriaId
          AND zona = :zona
          AND estado_actual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')
          AND fecha_registro >= NOW() - INTERVAL '7 DAYS'
          AND (
            6371000 * acos(
              LEAST(1.0, GREATEST(-1.0,
                cos(radians(CAST(:lat AS double precision))) * cos(radians(CAST(latitud AS double precision))) *
                cos(radians(CAST(longitud AS double precision)) - radians(CAST(:lon AS double precision))) +
                sin(radians(CAST(:lat AS double precision))) * sin(radians(CAST(latitud AS double precision)))
              ))
            )
          ) <= :radioMetros
    """, nativeQuery = true)
    int contarQuejasCercanasActivas(
            @Param("categoriaId") Integer categoriaId,
            @Param("zona") Integer zona,
            @Param("lat") BigDecimal latitud,
            @Param("lon") BigDecimal longitud,
            @Param("radioMetros") double radioMetros
    );

    /**
     * Cola de Tareas Unificada con Bloqueo Pesimista (SKIP LOCKED).
     * Busca la queja más prioritaria y antigua en los 3 estados operativos
     * que no esté asignada a otro funcionario.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "-2")})
    @Query("""
        SELECT q FROM Queja q
        WHERE q.estadoActual IN ('REGISTRADA', 'EN VALIDACIÓN DE REPARACIÓN', 'PENDIENTE DE CIERRE')
          AND (q.funcionario IS NULL OR q.funcionario.usuarioId = :funcionarioId)
        ORDER BY
            CASE WHEN q.funcionario.usuarioId = :funcionarioId THEN 0 ELSE 1 END ASC,
            CASE q.prioridadConfirmada
                WHEN 'URGENTE' THEN 1
                WHEN 'ALTA' THEN 2
                WHEN 'MEDIA' THEN 3
                WHEN 'BAJA' THEN 4
                ELSE 5
            END ASC,
            q.fechaRegistro ASC
        """)
    List<Queja> buscarSiguienteTarea(@Param("funcionarioId") Integer funcionarioId, Pageable pageable);
}