package com.muni.backend.reportes.service;

import com.muni.backend.reportes.dto.ReporteSerieDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteChartServiceTest {

    private final ReporteChartService service = new ReporteChartService();

    @Test
    void generaUnaImagenPngDeBarras() {
        byte[] imagen = service.generarBarras(List.of(
                new ReporteSerieDTO("Categoría A", 10, 50),
                new ReporteSerieDTO("Categoría B", 6, 30),
                new ReporteSerieDTO("Categoría C", 4, 20)
        ));

        assertTrue(imagen.length > 0);
        assertEquals(0x89, imagen[0] & 0xFF);
        assertEquals('P', imagen[1]);
        assertEquals('N', imagen[2]);
        assertEquals('G', imagen[3]);
    }

    @Test
    void generaUnaImagenPngDePastel() {
        byte[] imagen = service.generarPastel(List.of(
                new ReporteSerieDTO("Categoría A", 10, 50),
                new ReporteSerieDTO("Categoría B", 6, 30),
                new ReporteSerieDTO("Categoría C", 4, 20)
        ));

        assertTrue(imagen.length > 0);
        assertEquals(0x89, imagen[0] & 0xFF);
    }

    @Test
    void generaUnaImagenValidaCuandoNoHayDatos() {
        byte[] imagen = service.generarBarras(List.of());

        assertTrue(imagen.length > 0);
        assertEquals(0x89, imagen[0] & 0xFF);
    }
}
