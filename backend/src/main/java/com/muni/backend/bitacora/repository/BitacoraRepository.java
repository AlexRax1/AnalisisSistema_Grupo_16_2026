package com.muni.backend.bitacora.repository;

import com.muni.backend.bitacora.model.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Integer> {

    // Consulta paginada para revisar la bitácora con filtros opcionales.
    @Query("""
        SELECT b FROM Bitacora b
        WHERE (CAST(:usuario AS string) IS NULL
               OR LOWER(b.usuarioAccion) LIKE LOWER(CONCAT('%', CAST(:usuario AS string), '%')))
          AND (CAST(:tabla AS string) IS NULL OR b.tablaAfectada = CAST(:tabla AS string))
          AND (CAST(:accion AS string) IS NULL OR b.accion = CAST(:accion AS string))
          AND (CAST(:idAfectado AS string) IS NULL OR b.idAfectado = CAST(:idAfectado AS string))
          AND (CAST(:campoAfectado AS string) IS NULL OR b.campoAfectado = CAST(:campoAfectado AS string))
          AND b.fechaHora >= :fechaInicio
          AND b.fechaHora <= :fechaFin
        ORDER BY b.fechaHora DESC
        """)
    Page<Bitacora> buscarConFiltros(
            @Param("usuario") String usuario,
            @Param("tabla") String tabla,
            @Param("accion") String accion,
            @Param("idAfectado") String idAfectado,
            @Param("campoAfectado") String campoAfectado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}
