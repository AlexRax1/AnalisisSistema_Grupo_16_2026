package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.especialista.InformeReparacionDTO;
import com.muni.backend.quejas.dto.shared.MensajeResponse;
import com.muni.backend.quejas.dto.shared.QuejaDetalleCompletoDTO;
import com.muni.backend.quejas.dto.shared.QuejaResumenDTO;
import com.muni.backend.quejas.service.EspecialistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/especialista")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ESPECIALISTA_TECNICO')")
public class EspecialistaController {

    private final EspecialistaService especialistaService;

    // Bandeja de quejas asignadas al especialista.
    @GetMapping("/mis-quejas")
    public List<QuejaResumenDTO> obtenerMisQuejas(Authentication authentication) {
        return especialistaService.obtenerMisQuejas(authentication.getName());
    }

    // Detalle completo de una queja.
    @GetMapping("/{quejaId}")
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(
            @PathVariable Integer quejaId,
            Authentication authentication) {
        return especialistaService.obtenerDetalleQueja(quejaId, authentication.getName());
    }

    // Registra el informe de reparación con fotografías.
    @PostMapping(value = "/{quejaId}/informe-reparacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MensajeResponse registrarInformeReparacion(
            @PathVariable Integer quejaId,
            @RequestPart("datos") @Valid InformeReparacionDTO dto,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            Authentication authentication) {
        return especialistaService.registrarInformeReparacion(quejaId, dto, fotos, authentication.getName());
    }

    // Registra el informe de reparación sin fotografías (JSON).
    @PostMapping(value = "/{quejaId}/informe-reparacion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MensajeResponse registrarInformeReparacionJson(
            @PathVariable Integer quejaId,
            @Valid @RequestBody InformeReparacionDTO dto,
            Authentication authentication) {
        return especialistaService.registrarInformeReparacion(quejaId, dto, null, authentication.getName());
    }
}
