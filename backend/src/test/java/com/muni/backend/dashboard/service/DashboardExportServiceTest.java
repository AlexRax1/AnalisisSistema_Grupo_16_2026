package com.muni.backend.dashboard.service;

import com.muni.backend.dashboard.dto.DashboardContadorDTO;
import com.muni.backend.dashboard.dto.DashboardEstadoDTO;
import com.muni.backend.dashboard.dto.DashboardQuejaDTO;
import com.muni.backend.dashboard.dto.DashboardResponseDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardExportServiceTest {

    private final DashboardExportService service = new DashboardExportService();

    @Test
    void generaUnPdfValido() {
        byte[] pdf = service.generarPdf(dashboard());

        assertTrue(pdf.length > 500);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void generaUnExcelConLasTresHojas() throws Exception {
        byte[] excel = service.generarExcel(dashboard());

        assertTrue(excel.length > 0);
        assertEquals('P', excel[0]);
        assertEquals('K', excel[1]);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertNotNull(workbook.getSheet("Resumen"));
            assertNotNull(workbook.getSheet("Estados"));
            assertNotNull(workbook.getSheet("Recientes"));
        }
    }

    private DashboardResponseDTO dashboard() {
        return new DashboardResponseDTO(
                LocalDateTime.now(),
                11,
                List.of(
                        new DashboardContadorDTO("NUEVOS", "Nuevos", 5, 45.5, "fiber_new"),
                        new DashboardContadorDTO("EN_PROCESO", "En proceso", 2, 18.2, "autorenew"),
                        new DashboardContadorDTO("PENDIENTES", "Pendientes", 1, 9.1, "pending_actions"),
                        new DashboardContadorDTO("RESUELTOS", "Resueltos", 2, 18.2, "check_circle"),
                        new DashboardContadorDTO("RECHAZADOS", "Rechazadas", 1, 9.1, "cancel")
                ),
                List.of(
                        new DashboardEstadoDTO("REGISTRADA", "NUEVOS", 5, 45.5),
                        new DashboardEstadoDTO("SOLUCIONADA / CERRADA", "RESUELTOS", 2, 18.2)
                ),
                List.of(
                        new DashboardQuejaDTO(
                                1,
                                "QUE-2026-000001",
                                LocalDateTime.now(),
                                "Ciudadano de prueba",
                                "Alumbrado Público",
                                "REGISTRADA",
                                "MEDIA",
                                "Servicios Públicos"
                        )
                )
        );
    }
}
