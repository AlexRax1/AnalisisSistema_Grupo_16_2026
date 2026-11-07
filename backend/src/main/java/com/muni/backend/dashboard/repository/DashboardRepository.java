package com.muni.backend.dashboard.repository;

import com.muni.backend.dashboard.repository.proyeccion.DashboardEstadoProjection;
import com.muni.backend.quejas.model.Queja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Consultas propias del dashboard. No reutiliza el repositorio de reportes
 * para mantener el módulo aislado.
 */
@Repository
public interface DashboardRepository extends JpaRepository<Queja, Integer> {

    @Query(value = """
            SELECT
                q.estado_actual AS "estado",
                COUNT(*) AS "total"
            FROM quejas q
            GROUP BY q.estado_actual
            """, nativeQuery = true)
    List<DashboardEstadoProjection> contarPorEstado();
}
