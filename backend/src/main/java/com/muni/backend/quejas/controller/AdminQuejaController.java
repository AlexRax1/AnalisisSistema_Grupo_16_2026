package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.ciudadano.QuejaDetalleDTO;
import com.muni.backend.quejas.model.CategoriaQueja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.service.QuejaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/admin/quejas")
@CrossOrigin(origins = "*")
public class AdminQuejaController {

    @Autowired
    private QuejaService quejaService;
    @Autowired
    private CategoriaQuejaRepository categoriaQuejaRepository;

    // GET http://localhost:8080/admin/quejas/catalogo?pagina=0&tamanoPagina=20
    @GetMapping("/catalogo")
    public ResponseEntity<Page<QuejaDetalleDTO>> obtenerCatalogo(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanoPagina,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        String termino = (buscar != null && !buscar.trim().isEmpty()) ? buscar : busqueda;
        Pageable pageable = PageRequest.of(pagina, tamanoPagina);

        Page<QuejaDetalleDTO> catalogo = quejaService.obtenerCatalogoConFiltros(
                estado, categoriaId, prioridad, termino, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(catalogo);
    }

    // GET http://localhost:8080/admin/quejas/exportar/pdf
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf() {
        byte[] pdfBytes = quejaService.generarPdfCatalogo();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Catalogo_Quejas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel() {
        byte[] excelBytes = quejaService.generarExcelCatalogo();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Catalogo_Quejas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaQueja>> obtenerCategorias() {
        // Retorna todas las categorías registradas en la base de datos
        List<CategoriaQueja> categorias = categoriaQuejaRepository.findAll();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/test-catalogo")
    public ResponseEntity<Page<QuejaDetalleDTO>> probarCatalogo(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanoPagina,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String prioridad
    ) {
        Pageable pageable = PageRequest.of(pagina, tamanoPagina);

        Page<QuejaDetalleDTO> resultado = quejaService.obtenerCatalogoConFiltros(
                estado, categoriaId, prioridad, buscar, null, null, pageable
        );

        return ResponseEntity.ok(resultado);
    }
}
