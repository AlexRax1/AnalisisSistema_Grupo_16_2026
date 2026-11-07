package com.muni.backend.dashboard.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.dashboard.repository.DashboardRepository;
import com.muni.backend.dashboard.repository.proyeccion.DashboardEstadoProjection;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.QuejaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private QuejaRepository quejaRepository;

    @Mock
    private BitacoraService bitacoraService;

    @InjectMocks
    private DashboardService service;

    @Test
    void agrupaEstadosYCalculaPorcentajes() {
        List<DashboardEstadoProjection> conteos = List.of(
                estado("REGISTRADA", 5),
                estado("EN INSPECCIÓN", 2),
                estado("PENDIENTE DE CIERRE", 1),
                estado("SOLUCIONADA / CERRADA", 2),
                estado("RECHAZADA", 1)
        );
        when(dashboardRepository.contarPorEstado()).thenReturn(conteos);
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

    @Test
    void registraLaExportacionEnBitacora() {
        service.registrarExportacion("admin", "pdf", 11);

        verify(bitacoraService).registrar(
                "admin",
                "dashboard",
                "quejas",
                "exportacion_pdf",
                "DESCARGAR",
                null,
                "{\"total\":11}"
        );
    }

    private DashboardEstadoProjection estado(String nombre, long total) {
        DashboardEstadoProjection proyeccion = mock(DashboardEstadoProjection.class);
        when(proyeccion.getEstado()).thenReturn(nombre);
        when(proyeccion.getTotal()).thenReturn(total);
        return proyeccion;
    }
}
