package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.ciudadano.QuejaDerivadaDTO;
import com.muni.backend.quejas.dto.ciudadano.QuejaDetalleDTO;
import com.muni.backend.quejas.dto.ciudadano.QuejaRegistroDTO;
import com.muni.backend.quejas.dto.ciudadano.QuejaRegistroResponse;
import com.muni.backend.quejas.service.PdfService;
import com.muni.backend.quejas.service.QuejaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/quejas")
@RequiredArgsConstructor
public class QuejaController {

    private final QuejaService quejaService;
    private final PdfService pdfService;

    // Registra una queja principal con sus fotografías.
    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public QuejaRegistroResponse registrarQueja(
            @RequestPart("datos") QuejaRegistroDTO dto,
            @RequestPart("fotos") List<MultipartFile> fotos,
            Authentication authentication) {
        return quejaService.registrarQueja(dto, fotos, authentication.getName());
    }

    // Historial de quejas del ciudadano autenticado.
    @GetMapping("/mis-quejas")
    public List<QuejaDetalleDTO> obtenerMisQuejas(Authentication authentication) {
        return quejaService.obtenerMisQuejas(authentication.getName());
    }

    // Detalle de una queja por su correlativo.
    @GetMapping("/detalle/{correlativo}")
    public QuejaDetalleDTO obtenerDetallePorCorrelativo(@PathVariable String correlativo) {
        return quejaService.obtenerPorCorrelativo(correlativo);
    }

    // Descarga la constancia de la queja en PDF.
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

    // Registra un agravamiento o reincidencia.
    @PostMapping(value = "/reportar-derivada", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public QuejaRegistroResponse registrarQuejaDerivada(
            @RequestPart("datos") QuejaDerivadaDTO dto,
            @RequestPart("fotos") List<MultipartFile> fotos,
            Authentication authentication) {
        return quejaService.registrarQuejaDerivada(dto, fotos, authentication.getName());
    }
}
