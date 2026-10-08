package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.funcionario.*;
import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.exception.*;
import com.muni.backend.quejas.service.FuncionarioService;
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
@RequestMapping("/api/funcionario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    /**
     * Lista de dependencias municipales activas para dropdowns de selección.
     */
    @GetMapping("/dependencias")
    public ResponseEntity<List<com.muni.backend.quejas.model.Dependencia>> listarDependencias() {
        return ResponseEntity.ok(funcionarioService.listarDependenciasActivas());
    }

    /**
     * Bandeja de quejas asignadas al funcionario (resumen para lista).
     */
    @GetMapping("/mis-quejas")
    public ResponseEntity<?> obtenerMisQuejas(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<QuejaResumenDTO> quejas = funcionarioService.obtenerMisQuejas(username);
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
            QuejaDetalleCompletoDTO detalle = funcionarioService.obtenerDetalleQueja(quejaId, username);
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

    /*
     * funocion descontinuada, borrar despues
     */
    @PostMapping("/tareas/siguiente")
    public ResponseEntity<?> asignarSiguienteTarea(Authentication authentication) {
        try {
            String username = authentication.getName();
            TareaAsignadaDTO tarea = funcionarioService.asignarSiguienteTarea(username);
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
     * Asignar inspector de campo de forma automática por menor carga activa.
     */
    @PostMapping("/{quejaId}/asignar-inspector")
    public ResponseEntity<?> asignarInspector(
            @PathVariable Integer quejaId,
            @RequestBody(required = false) AsignarInspectorDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = funcionarioService.asignarInspector(quejaId, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al asignar inspector: " + e.getMessage()));
        }
    }

    /**
     * Autorizar reparación con asignación automática de especialista por menor carga en la dependencia.
     */
    @PostMapping("/{quejaId}/autorizar-reparacion")
    public ResponseEntity<?> autorizarReparacion(
            @PathVariable Integer quejaId,
            @Valid @RequestBody AutorizarReparacionDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = funcionarioService.autorizarReparacion(quejaId, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al autorizar reparación: " + e.getMessage()));
        }
    }

    /**
     * Cierre administrativo definitivo de la queja.
     */
    @PostMapping("/{quejaId}/cierre")
    public ResponseEntity<?> cierreAdministrativo(
            @PathVariable Integer quejaId,
            @Valid @RequestBody CierreAdministrativoDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = funcionarioService.cierreAdministrativo(quejaId, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en cierre administrativo: " + e.getMessage()));
        }
    }

    /**
     * Rechazo definitivo o devolución de la queja.
     */
    @PostMapping("/{quejaId}/rechazar-devolver")
    public ResponseEntity<?> rechazarODevolver(
            @PathVariable Integer quejaId,
            @Valid @RequestBody RechazoDevolucionDTO dto,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            MensajeResponse response = funcionarioService.rechazarODevolver(quejaId, dto, username);
            return ResponseEntity.ok(response);
        } catch (QuejaNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (EstadoInvalidoException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al rechazar/devolver: " + e.getMessage()));
        }
    }
}
