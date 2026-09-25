package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.QuejaDerivadaDTO;
import com.muni.backend.quejas.dto.QuejaDetalleDTO;
import com.muni.backend.quejas.dto.QuejaRegistroDTO;
import com.muni.backend.quejas.dto.QuejaRegistroResponse;
import com.muni.backend.quejas.service.PdfService;
import com.muni.backend.quejas.service.QuejaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/quejas")
@RequiredArgsConstructor
public class QuejaController {

    private final QuejaService quejaService;
    private final PdfService pdfService;

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarQueja(
            @RequestPart("datos") QuejaRegistroDTO dto,
            @RequestPart("fotos") List<MultipartFile> fotos,
            Authentication authentication
    ) {
        try {
            String correoUsuario = authentication.getName();
            QuejaRegistroResponse response = quejaService.registrarQueja(dto, fotos, correoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el registro: " + e.getMessage());
        }
    }

    @GetMapping("/mis-quejas")
    public ResponseEntity<?> obtenerMisQuejas(Authentication authentication) {
        try {
            String correoUsuario = authentication.getName();
            List<QuejaDetalleDTO> historial = quejaService.obtenerMisQuejas(correoUsuario);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el historial de quejas: " + e.getMessage());
        }
    }

    @GetMapping("/detalle/{correlativo}")
    public ResponseEntity<?> obtenerDetallePorCorrelativo(@PathVariable String correlativo) {
        try {
            QuejaDetalleDTO detalle = quejaService.obtenerPorCorrelativo(correlativo);
            return ResponseEntity.ok(detalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el detalle de la queja: " + e.getMessage());
        }
    }

    @GetMapping("/{correlativo}/constancia-pdf")
    public ResponseEntity<byte[]> descargarConstanciaPdf(@PathVariable String correlativo) {
        QuejaDetalleDTO detalle = quejaService.obtenerPorCorrelativo(correlativo);
        byte[] pdfBytes = pdfService.generarConstanciaPdf(detalle);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Constancia_" + correlativo + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/reportar-derivada", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarQuejaDerivada(
            @RequestPart("datos") QuejaDerivadaDTO dto,
            @RequestPart("fotos") List<MultipartFile> fotos,
            Authentication authentication
    ) {
        try {
            String correoUsuario = authentication.getName();
            QuejaRegistroResponse response = quejaService.registrarQuejaDerivada(dto, fotos, correoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el reporte derivado: " + e.getMessage());
        }
    }
}