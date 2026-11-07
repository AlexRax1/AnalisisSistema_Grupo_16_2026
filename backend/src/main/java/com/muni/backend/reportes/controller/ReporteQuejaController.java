package com.muni.backend.reportes.controller;

import com.muni.backend.reportes.dto.ReporteFiltroDTO;
import com.muni.backend.reportes.dto.ReporteOpcionesDTO;
import com.muni.backend.reportes.dto.ReporteResponseDTO;
import com.muni.backend.reportes.dto.DashboardResponseDTO;
import com.muni.backend.reportes.service.ReporteExportService;
import com.muni.backend.reportes.service.ReporteQuejaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
public class ReporteQuejaController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final ReporteQuejaService reporteService;
    private final ReporteExportService exportService;

    @PostMapping("/quejas/consultar")
    public ReporteResponseDTO consultar(
            @RequestBody(required = false) ReporteFiltroDTO filtro,
            Authentication authentication
    ) {
        return reporteService.generar(filtro, usuario(authentication), 100);
    }

    @GetMapping("/opciones")
    public ReporteOpcionesDTO opciones() {
        return reporteService.obtenerOpciones();
    }

    @GetMapping("/dashboard")
    public DashboardResponseDTO dashboard(
            @RequestParam(defaultValue = "10") int limiteRecientes
    ) {
        return reporteService.obtenerDashboard(limiteRecientes);
    }

    @PostMapping("/quejas/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestBody(required = false) ReporteFiltroDTO filtro,
            Authentication authentication
    ) {
        String username = usuario(authentication);
        ReporteResponseDTO reporte = reporteService.generar(filtro, username, 10000, false);
        byte[] archivo = exportService.generarExcel(reporte);
        reporteService.registrarExportacion(filtro, username, "excel");
        return archivo(archivo, "Reporte_Quejas", "xlsx", XLSX);
    }

    @PostMapping("/quejas/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestBody(required = false) ReporteFiltroDTO filtro,
            Authentication authentication
    ) {
        String username = usuario(authentication);
        ReporteResponseDTO reporte = reporteService.generar(filtro, username, 10000, false);
        byte[] archivo = exportService.generarPdf(reporte);
        reporteService.registrarExportacion(filtro, username, "pdf");
        return archivo(archivo, "Reporte_Quejas", "pdf", MediaType.APPLICATION_PDF);
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
