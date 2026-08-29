package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.Queja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface QuejaRepository extends JpaRepository<Queja, Integer> {

    Optional<Queja> findByCorrelativo(String correlativo);

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
}