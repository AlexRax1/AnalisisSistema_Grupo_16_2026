package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.*;
import com.muni.backend.quejas.exception.*;
import com.muni.backend.quejas.service.GestionMunicipalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/gestion-municipal", "/quejas"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GestionMunicipalController {

    private final GestionMunicipalService gestionService;

    /**
     * Endpoint 1: Pull por demanda - Asignar siguiente tarea
     */
    @PostMapping({"/siguiente-tarea", "/tareas/asignar-siguiente"})
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> asignarSiguienteTarea(Authentication authentication) {
        try {
            String username = authentication.getName();
            TareaAsignadaDTO tarea = gestionService.asignarSiguienteTarea(username);
            return ResponseEntity.ok(tarea);
        } catch (SinTareasDisponiblesException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("mensaje", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al asignar tarea: " + e.getMessage()));
        }
    }

    /**
     * Endpoint 2: Fase 1 - Asignar inspector de campo
     */
    @PostMapping({"/asignar-inspector", "/{quejaId}/fase-inicial/asignar-inspector"})
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> asignarInspector(
            @PathVariable(required = false) Integer quejaId,
            @Valid @RequestBody AsignarInspectorDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Integer idDefinitivo = quejaId != null ? quejaId : dto.getQuejaId();
            MensajeResponse response = gestionService.asignarInspector(idDefinitivo, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al asignar inspector: " + e.getMessage()));
        }
    }

    /**
     * Endpoint 3: Fase 2 - Autorizar reparación
     */
    @PostMapping({"/autorizar-reparacion", "/{quejaId}/fase-aprobacion/autorizar-reparacion"})
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> autorizarReparacion(
            @PathVariable(required = false) Integer quejaId,
            @Valid @RequestBody AutorizarReparacionDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Integer idDefinitivo = quejaId != null ? quejaId : dto.getQuejaId();
            MensajeResponse response = gestionService.autorizarReparacion(idDefinitivo, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al autorizar reparación: " + e.getMessage()));
        }
    }

    /**
     * Endpoint 4: Fase 3 - Cierre administrativo
     */
    @PostMapping({"/cierre-administrativo", "/{quejaId}/fase-cierre/aprobar"})
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> cierreAdministrativo(
            @PathVariable(required = false) Integer quejaId,
            @Valid @RequestBody CierreAdministrativoDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Integer idDefinitivo = quejaId != null ? quejaId : dto.getQuejaId();
            MensajeResponse response = gestionService.cierreAdministrativo(idDefinitivo, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en cierre administrativo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint 5: Rechazar o Devolver queja
     */
    @PostMapping({"/rechazar-devolver", "/{quejaId}/rechazar-devolver"})
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> rechazarODevolver(
            @PathVariable(required = false) Integer quejaId,
            @Valid @RequestBody RechazoDevolucionDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Integer idDefinitivo = quejaId != null ? quejaId : dto.getQuejaId();
            MensajeResponse response = gestionService.rechazarODevolver(idDefinitivo, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al rechazar/devolver: " + e.getMessage()));
        }
    }
}
