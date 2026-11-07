package com.muni.backend.reportes.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.repository.DependenciaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.repository.SubcategoriaQuejaRepository;
import com.muni.backend.reportes.dto.ReporteFiltroDTO;
import com.muni.backend.reportes.repository.ReporteQuejaRepository;
import com.muni.backend.reportes.repository.proyeccion.DashboardEstadoProjection;
import com.muni.backend.reportes.repository.proyeccion.ReporteKpiProjection;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteQuejaServiceTest {

    @Mock
    private ReporteQuejaRepository reporteRepository;

    @Mock
    private QuejaRepository quejaRepository;

    @Mock
    private CategoriaQuejaRepository categoriaRepository;

    @Mock
    private SubcategoriaQuejaRepository subcategoriaRepository;

    @Mock
    private DependenciaRepository dependenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BitacoraService bitacoraService;

    @Mock
    private ReporteKpiProjection kpi;

    @InjectMocks
    private ReporteQuejaService service;

    @Test
    void rechazaRangoDeFechasInvertido() {
        ReporteFiltroDTO filtro = new ReporteFiltroDTO(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1),
                null,
                null,
                null,
                "CATEGORIA"
        );

        assertThrows(IllegalArgumentException.class, () -> service.generar(filtro, "admin", 100));
    }

    @Test
    void generaReporteVacioConValoresPorDefecto() {
        when(reporteRepository.agrupar(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(reporteRepository.obtenerKpis(any(), any(), any(), any(), any())).thenReturn(kpi);
        when(kpi.getTotal()).thenReturn(0L);
        when(kpi.getCerradas()).thenReturn(0L);
        when(kpi.getRechazadas()).thenReturn(0L);
        when(kpi.getEnProceso()).thenReturn(0L);
        when(kpi.getPromedioDiasCierre()).thenReturn(BigDecimal.ZERO);
        when(quejaRepository.buscarCatalogoConFiltros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.<Queja>empty());

        var respuesta = service.generar(null, "admin", 100, false);

        assertTrue(respuesta.titulo().contains("categoría"));
        assertEquals(6, respuesta.kpis().size());
        assertTrue(respuesta.series().isEmpty());
        assertTrue(respuesta.detalle().isEmpty());
    }

    @Test
    void dashboardAgrupaEstadosYCalculaPorcentajes() {
        List<DashboardEstadoProjection> conteos = List.of(
                estado("REGISTRADA", 5),
                estado("EN INSPECCIÓN", 2),
                estado("PENDIENTE DE CIERRE", 1),
                estado("SOLUCIONADA / CERRADA", 2),
                estado("RECHAZADA", 1)
        );
        when(reporteRepository.contarPorEstado()).thenReturn(conteos);
        when(quejaRepository.buscarCatalogoConFiltros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.<Queja>empty());

        var respuesta = service.obtenerDashboard(10);

        assertEquals(11, respuesta.total());
        assertEquals(5, respuesta.contadores().size());
        assertEquals(7, respuesta.estados().size());
        assertEquals(5, respuesta.contadores().get(0).valor());
        assertEquals("NUEVOS", respuesta.contadores().get(0).clave());
        assertEquals(2, respuesta.contadores().get(1).valor());
        assertEquals(1, respuesta.contadores().get(2).valor());
        assertEquals(2, respuesta.contadores().get(3).valor());
        assertEquals(1, respuesta.contadores().get(4).valor());
        assertEquals(45.5, respuesta.contadores().get(0).porcentaje());
        assertTrue(respuesta.recientes().isEmpty());
    }

    private DashboardEstadoProjection estado(String nombre, long total) {
        DashboardEstadoProjection proyeccion = mock(DashboardEstadoProjection.class);
        when(proyeccion.getEstado()).thenReturn(nombre);
        when(proyeccion.getTotal()).thenReturn(total);
        return proyeccion;
    }
}
