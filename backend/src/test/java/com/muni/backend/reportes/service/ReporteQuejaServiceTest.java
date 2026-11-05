package com.muni.backend.reportes.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.repository.DependenciaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.repository.SubcategoriaQuejaRepository;
import com.muni.backend.reportes.dto.ReporteFiltroDTO;
import com.muni.backend.reportes.repository.ReporteQuejaRepository;
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
}
