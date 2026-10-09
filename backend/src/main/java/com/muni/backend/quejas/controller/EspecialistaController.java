package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.especialista.InformeReparacionDTO;
import com.muni.backend.quejas.dto.shared.MensajeResponse;
import com.muni.backend.quejas.dto.shared.QuejaDetalleCompletoDTO;
import com.muni.backend.quejas.dto.shared.QuejaResumenDTO;
import com.muni.backend.quejas.exception.*;
import com.muni.backend.quejas.service.EspecialistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/especialista")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ESPECIALISTA_TECNICO')")
public class EspecialistaController {

    private final EspecialistaService especialistaService;

    /**
     * Bandeja de quejas asignadas al especialista (resumen simple para lista).
     */
    @GetMapping("/mis-quejas")
    public ResponseEntity<?> obtenerMisQuejas(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<QuejaResumenDTO> quejas = especialistaService.obtenerMisQuejas(username);
            return ResponseEntity.ok(quejas);



        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener quejas: " + e.getMessage()));
        }
    }

    /**
     * Detalle completo de una queja seleccionada (con evidencias, informes previos, historial y acciones disponibles).
     */
    @GetMapping("/{quejaId}")
    public ResponseEntity<?> obtenerDetalleQueja(
            @PathVariable Integer quejaId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            QuejaDetalleCompletoDTO detalle = especialistaService.obtenerDetalleQueja(quejaId, username);
            return ResponseEntity.ok(detalle);


        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener detalle de la queja: " + e.getMessage()));
        }
    }

    /**
     * Registrar informe de solución técnica sobre una queja asignada (con soporte multipart para evidencias).
     */
    @PostMapping(value = "/{quejaId}/informe-reparacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarInformeReparacion(
            @PathVariable Integer quejaId,
            @RequestPart("datos") @Valid InformeReparacionDTO dto,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = especialistaService.registrarInformeReparacion(quejaId, dto, fotos, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar informe de reparación: " + e.getMessage()));
        }
    }

    /**
     * Alternativa JSON directo para registrar informe de solución técnica sin archivos adjuntos.
     */
    @PostMapping(value = "/{quejaId}/informe-reparacion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registrarInformeReparacionJson(
            @PathVariable Integer quejaId,
            @Valid @RequestBody InformeReparacionDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = especialistaService.registrarInformeReparacion(quejaId, dto, null, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar informe de reparación: " + e.getMessage()));
        }
    }
}
