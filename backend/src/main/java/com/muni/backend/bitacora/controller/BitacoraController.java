package com.muni.backend.bitacora.controller;

import com.muni.backend.bitacora.dto.BitacoraFiltroDTO;
import com.muni.backend.bitacora.dto.BitacoraRespuestaDTO;
import com.muni.backend.bitacora.service.BitacoraExportService;
import com.muni.backend.bitacora.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Consulta y exportación de la bitácora. No existe endpoint para escribir manualmente:
// los eventos sólo los registra el backend desde los servicios de negocio.
@RestController
@RequestMapping("/admin/bitacora")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BitacoraController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final BitacoraService bitacoraService;
    private final BitacoraExportService exportService;

    // GET /admin/bitacora?usuario=admin&tabla=quejas&accion=LOGIN&idAfectado=13
    @GetMapping
    public ResponseEntity<Page<BitacoraRespuestaDTO>> listar(
            @ModelAttribute BitacoraFiltroDTO filtro,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanoPagina) {

        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamanoPagina, 1), 100);
        Pageable pageable = PageRequest.of(paginaSegura, tamanoSeguro);

        return ResponseEntity.ok(bitacoraService.listar(filtro, pageable));
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @ModelAttribute BitacoraFiltroDTO filtro,
            Authentication authentication) {

        List<BitacoraRespuestaDTO> filas = bitacoraService.listarParaExportar(filtro);
        byte[] archivo = exportService.generarPdf(filas, filtro);
        registrarExportacion(filtro, "PDF", filas.size(), authentication);
        return archivo(archivo, "Bitacora", "pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @ModelAttribute BitacoraFiltroDTO filtro,
            Authentication authentication) {

        List<BitacoraRespuestaDTO> filas = bitacoraService.listarParaExportar(filtro);
        byte[] archivo = exportService.generarExcel(filas, filtro);
        registrarExportacion(filtro, "EXCEL", filas.size(), authentication);
        return archivo(archivo, "Bitacora", "xlsx", XLSX);
    }

    private void registrarExportacion(BitacoraFiltroDTO filtro,
                                      String formato,
                                      int filas,
                                      Authentication authentication) {
        String usuario = authentication != null ? authentication.getName() : null;
        bitacoraService.registrar(
                usuario,
                "bitacora",
                "reporte_auditoria",
                "reporte",
                "DESCARGAR",
                null,
                "Reporte de auditoría " + formato + " | " + filtro.resumen()
                        + " | " + filas + " filas"
        );
    }

    private ResponseEntity<byte[]> archivo(byte[] contenido,
                                           String nombre,
                                           String extension,
                                           MediaType tipo) {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(nombre + "_" + fecha + "." + extension, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(tipo)
                .body(contenido);
    }
}
