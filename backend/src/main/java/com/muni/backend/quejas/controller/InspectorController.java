package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.inspector.InformeInspeccionDTO;
import com.muni.backend.quejas.dto.shared.MensajeResponse;
import com.muni.backend.quejas.dto.shared.QuejaDetalleCompletoDTO;
import com.muni.backend.quejas.dto.shared.QuejaResumenDTO;
import com.muni.backend.quejas.service.InspectorService;
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
@RequestMapping("/api/inspector")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INSPECTOR_CAMPO')")
public class InspectorController {

    private final InspectorService inspectorService;

    // Bandeja de quejas asignadas al inspector.
    @GetMapping("/mis-quejas")
    public List<QuejaResumenDTO> obtenerMisQuejas(Authentication authentication) {
        return inspectorService.obtenerMisQuejas(authentication.getName());
    }

    // Detalle completo de una queja.
    @GetMapping("/{quejaId}")
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(
            @PathVariable Integer quejaId,
            Authentication authentication) {
        return inspectorService.obtenerDetalleQueja(quejaId, authentication.getName());
    }

    // Registra el informe de inspección con fotografías.
    @PostMapping(value = "/{quejaId}/informe-inspeccion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MensajeResponse registrarInformeInspeccion(
            @PathVariable Integer quejaId,
            @RequestPart("datos") @Valid InformeInspeccionDTO dto,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            Authentication authentication) {
        return inspectorService.registrarInformeInspeccion(quejaId, dto, fotos, authentication.getName());
    }

    // Registra el informe de inspección sin fotografías (JSON).
    @PostMapping(value = "/{quejaId}/informe-inspeccion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MensajeResponse registrarInformeInspeccionJson(
            @PathVariable Integer quejaId,
            @Valid @RequestBody InformeInspeccionDTO dto,
            Authentication authentication) {
        return inspectorService.registrarInformeInspeccion(quejaId, dto, null, authentication.getName());
    }
}
