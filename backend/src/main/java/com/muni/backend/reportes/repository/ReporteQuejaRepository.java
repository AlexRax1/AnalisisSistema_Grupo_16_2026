package com.muni.backend.reportes.repository;

import com.muni.backend.quejas.model.Queja;
import com.muni.backend.reportes.repository.proyeccion.DashboardEstadoProjection;
import com.muni.backend.reportes.repository.proyeccion.ReporteAgrupadoProjection;
import com.muni.backend.reportes.repository.proyeccion.ReporteKpiProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReporteQuejaRepository extends JpaRepository<Queja, Integer> {

    @Query(value = """
            SELECT
                CASE CAST(:agrupacion AS text)
                    WHEN 'ESTADO' THEN q.estado_actual
                    WHEN 'CATEGORIA' THEN c.nombre_categoria
                    WHEN 'SUBCATEGORIA' THEN COALESCE(s.nombre_subcategoria, 'Sin subcategoría')
                    WHEN 'PRIORIDAD' THEN COALESCE(q.prioridad_confirmada, 'Sin prioridad')
                    WHEN 'TIPO_REGISTRO' THEN COALESCE(q.tipo_registro, 'PRINCIPAL')
                    WHEN 'DEPENDENCIA' THEN COALESCE(d.nombre_dependencia, 'Sin dependencia')
                    WHEN 'ESPECIALISTA' THEN COALESCE(e.nombres || ' ' || e.apellidos, 'Sin especialista')
                    WHEN 'INSPECTOR' THEN COALESCE(i.nombres || ' ' || i.apellidos, 'Sin inspector')
                    ELSE q.estado_actual
                END AS "etiqueta",
                COUNT(*) AS "total",
                COUNT(*) FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA') AS "cerradas",
                COUNT(*) FILTER (WHERE q.estado_actual = 'RECHAZADA') AS "rechazadas",
                COUNT(*) FILTER (WHERE q.estado_actual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')) AS "enProceso",
                ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) AS "porcentaje"
            FROM quejas q
            JOIN categorias_queja c ON c.categoria_id = q.categoria_id
            LEFT JOIN subcategorias_queja s ON s.subcategoria_id = q.subcategoria_id
            LEFT JOIN dependencias d ON d.dependencia_id = q.dependencia_asignada_id
            LEFT JOIN usuarios e ON e.usuario_id = q.especialista_id
            LEFT JOIN usuarios i ON i.usuario_id = q.inspector_id
            WHERE q.fecha_registro >= CAST(:fechaDesde AS date)
              AND q.fecha_registro < (CAST(:fechaHasta AS date) + INTERVAL '1 day')
              AND (CAST(:estado AS text) IS NULL OR q.estado_actual = CAST(:estado AS text))
              AND (CAST(:categoriaId AS integer) IS NULL OR q.categoria_id = CAST(:categoriaId AS integer))
              AND (CAST(:prioridad AS text) IS NULL OR q.prioridad_confirmada = CAST(:prioridad AS text))
            GROUP BY 1
            ORDER BY "total" DESC, "etiqueta" ASC
            """, nativeQuery = true)
    List<ReporteAgrupadoProjection> agrupar(
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("estado") String estado,
            @Param("categoriaId") Integer categoriaId,
            @Param("prioridad") String prioridad,
            @Param("agrupacion") String agrupacion
    );

    @Query(value = """
            SELECT
                COUNT(*) AS "total",
                COUNT(*) FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA') AS "cerradas",
                COUNT(*) FILTER (WHERE q.estado_actual = 'RECHAZADA') AS "rechazadas",
                COUNT(*) FILTER (WHERE q.estado_actual NOT IN ('SOLUCIONADA / CERRADA', 'RECHAZADA')) AS "enProceso",
                COALESCE(
                    ROUND(
                        AVG(EXTRACT(EPOCH FROM (q.fecha_cierre - q.fecha_registro)) / 86400.0)
                            FILTER (WHERE q.estado_actual = 'SOLUCIONADA / CERRADA'
                                      AND q.fecha_cierre IS NOT NULL),
                        1
                    ),
                    0
                ) AS "promedioDiasCierre"
            FROM quejas q
            WHERE q.fecha_registro >= CAST(:fechaDesde AS date)
              AND q.fecha_registro < (CAST(:fechaHasta AS date) + INTERVAL '1 day')
              AND (CAST(:estado AS text) IS NULL OR q.estado_actual = CAST(:estado AS text))
              AND (CAST(:categoriaId AS integer) IS NULL OR q.categoria_id = CAST(:categoriaId AS integer))
              AND (CAST(:prioridad AS text) IS NULL OR q.prioridad_confirmada = CAST(:prioridad AS text))
            """, nativeQuery = true)
    ReporteKpiProjection obtenerKpis(
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("estado") String estado,
            @Param("categoriaId") Integer categoriaId,
            @Param("prioridad") String prioridad
    );

    @Query(value = """
            SELECT
                q.estado_actual AS "estado",
                COUNT(*) AS "total"
            FROM quejas q
            GROUP BY q.estado_actual
            """, nativeQuery = true)
    List<DashboardEstadoProjection> contarPorEstado();
}
