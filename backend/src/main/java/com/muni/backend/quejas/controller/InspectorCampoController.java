package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.MensajeResponse;
import com.muni.backend.quejas.dto.QuejaInspectorBandejaDTO;
import com.muni.backend.quejas.dto.RegistroInspeccionDTO;
import com.muni.backend.quejas.exception.AccesoDenegadoException;
import com.muni.backend.quejas.exception.EstadoInvalidoException;
import com.muni.backend.quejas.exception.QuejaNoEncontradaException;
import com.muni.backend.quejas.service.InspectorCampoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspector")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InspectorCampoController {

    private final InspectorCampoService inspectorCampoService;

    @GetMapping("/asignaciones")
    @PreAuthorize("hasAuthority('INSPECTOR_CAMPO')")
    public ResponseEntity<?> listarMisAsignaciones(Authentication auth) {
        try {
            List<QuejaInspectorBandejaDTO> asignaciones = inspectorCampoService.listarMisAsignaciones(auth.getName());
            return ResponseEntity.ok(asignaciones);
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar asignaciones: " + e.getMessage()));
        }
    }

    @PostMapping("/quejas/{quejaId}/informe-inspeccion")
    @PreAuthorize("hasAuthority('INSPECTOR_CAMPO')")
    public ResponseEntity<?> registrarInspeccionInicial(
            @PathVariable Integer quejaId,
            @Valid @RequestBody RegistroInspeccionDTO dto,
            Authentication auth) {
        try {
            MensajeResponse response = inspectorCampoService.registrarInspeccionInicial(quejaId, dto, auth.getName());
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar informe de inspección: " + e.getMessage()));
        }
    }
}
