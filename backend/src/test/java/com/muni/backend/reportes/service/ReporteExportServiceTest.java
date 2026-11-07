package com.muni.backend.reportes.service;

import com.muni.backend.reportes.dto.ReporteDetalleDTO;
import com.muni.backend.reportes.dto.ReporteFilaDTO;
import com.muni.backend.reportes.dto.ReporteKpiDTO;
import com.muni.backend.reportes.dto.ReporteResponseDTO;
import com.muni.backend.reportes.dto.ReporteSerieDTO;
import com.muni.backend.reportes.dto.ReporteTablaDTO;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteExportServiceTest {

    private final ReporteExportService service =
            new ReporteExportService(new ReporteChartService());

    @Test
    void generaUnPdfConGraficos() {
        byte[] pdf = service.generarPdf(reporte());

        assertTrue(pdf.length > 0);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));

        String contenido = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(contenido.contains("/Subtype/Image") || contenido.contains("/Subtype /Image"));
    }

    @Test
    void generaUnExcelConHojaDeGraficos() throws Exception {
        byte[] excel = service.generarExcel(reporte());

        assertTrue(excel.length > 0);
        assertEquals('P', excel[0]);
        assertEquals('K', excel[1]);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            XSSFSheet graficos = workbook.getSheet("Gráficos");
            assertNotNull(graficos);
            assertNotNull(graficos.getDrawingPatriarch());
            assertEquals(2, graficos.getDrawingPatriarch().getShapes().size());
        }
    }

    private ReporteResponseDTO reporte() {
        List<ReporteSerieDTO> series = List.of(
                new ReporteSerieDTO("Categoría A", 10, 50),
                new ReporteSerieDTO("Categoría B", 6, 30),
                new ReporteSerieDTO("Categoría C", 4, 20)
        );

        List<ReporteFilaDTO> filas = List.of(
                new ReporteFilaDTO("Categoría A", 10, 6, 3, 1, 50),
                new ReporteFilaDTO("Categoría B", 6, 3, 2, 1, 30),
                new ReporteFilaDTO("Categoría C", 4, 2, 1, 1, 20)
        );

        List<ReporteDetalleDTO> detalle = List.of(
                new ReporteDetalleDTO(
                        1,
                        "Q-001",
                        LocalDateTime.now(),
                        "Ciudadano de prueba",
                        "1234567890123",
                        "Categoría A",
                        "Subcategoría",
                        "REGISTRADA",
                        "MEDIA",
                        "Inspector",
                        "Especialista",
                        "Dependencia",
                        null,
                        "Dirección de prueba"
                )
        );

        return new ReporteResponseDTO(
                "Reporte de prueba",
                "Periodo de prueba",
                LocalDateTime.now(),
                Map.of("Agrupación", "Categoría"),
                List.of(
                        new ReporteKpiDTO("Total", "20", "description", "primary"),
                        new ReporteKpiDTO("Resueltas", "11", "check_circle", "success")
                ),
                series,
                new ReporteTablaDTO(
                        "Resumen por categoría",
                        List.of("Dimensión", "Total", "Resueltas", "En proceso", "Rechazadas", "%"),
                        filas
                ),
                detalle,
                1,
                100
        );
    }
}
