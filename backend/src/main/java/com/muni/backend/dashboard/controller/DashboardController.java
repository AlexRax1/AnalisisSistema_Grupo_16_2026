package com.muni.backend.dashboard.controller;

import com.muni.backend.dashboard.dto.DashboardResponseDTO;
import com.muni.backend.dashboard.service.DashboardExportService;
import com.muni.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final DashboardService dashboardService;
    private final DashboardExportService exportService;

    @GetMapping
    public DashboardResponseDTO obtener(
            @RequestParam(defaultValue = "10") int limiteRecientes
    ) {
        return dashboardService.obtenerDashboard(limiteRecientes);
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(Authentication authentication) {
        String username = usuario(authentication);
        DashboardResponseDTO dashboard = dashboardService.obtenerDashboard(25);
        byte[] archivo = exportService.generarPdf(dashboard);
        dashboardService.registrarExportacion(username, "pdf", dashboard.total());
        return archivo(archivo, "Dashboard_Quejas", "pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(Authentication authentication) {
        String username = usuario(authentication);
        DashboardResponseDTO dashboard = dashboardService.obtenerDashboard(25);
        byte[] archivo = exportService.generarExcel(dashboard);
        dashboardService.registrarExportacion(username, "excel", dashboard.total());
        return archivo(archivo, "Dashboard_Quejas", "xlsx", XLSX);
    }

    private ResponseEntity<byte[]> archivo(byte[] contenido, String nombre, String extension, MediaType tipo) {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombre + "_" + fecha + "." + extension + "\"")
                .contentType(tipo)
                .body(contenido);
    }

    private String usuario(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
