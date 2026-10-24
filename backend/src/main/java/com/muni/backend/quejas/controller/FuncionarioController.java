package com.muni.backend.quejas.controller;

import com.muni.backend.quejas.dto.funcionario.AsignarInspectorDTO;
import com.muni.backend.quejas.dto.funcionario.AutorizarReparacionDTO;
import com.muni.backend.quejas.dto.funcionario.CierreAdministrativoDTO;
import com.muni.backend.quejas.dto.funcionario.RechazoDevolucionDTO;
import com.muni.backend.quejas.dto.funcionario.TareaAsignadaDTO;
import com.muni.backend.quejas.dto.shared.MensajeResponse;
import com.muni.backend.quejas.dto.shared.QuejaDetalleCompletoDTO;
import com.muni.backend.quejas.dto.shared.QuejaResumenDTO;
import com.muni.backend.quejas.model.Dependencia;
import com.muni.backend.quejas.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funcionario")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // Lista las dependencias activas para los selects del funcionario.
    @GetMapping("/dependencias")
    public List<Dependencia> listarDependencias() {
        return funcionarioService.listarDependenciasActivas();
    }

    // Bandeja de quejas asignadas al funcionario.
    @GetMapping("/mis-quejas")
    public List<QuejaResumenDTO> obtenerMisQuejas(Authentication authentication) {
        return funcionarioService.obtenerMisQuejas(authentication.getName());
    }

    // Detalle completo de una queja.
    @GetMapping("/{quejaId}")
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(
            @PathVariable Integer quejaId,
            Authentication authentication) {
        return funcionarioService.obtenerDetalleQueja(quejaId, authentication.getName());
    }

    // Endpoint antiguo de asignación por demanda; se conserva por compatibilidad.
    @PostMapping("/tareas/siguiente")
    public TareaAsignadaDTO asignarSiguienteTarea(Authentication authentication) {
        return funcionarioService.asignarSiguienteTarea(authentication.getName());
    }

    // Asigna automáticamente el inspector con menor carga.
    @PostMapping("/{quejaId}/asignar-inspector")
    public MensajeResponse asignarInspector(
            @PathVariable Integer quejaId,
            @RequestBody(required = false) AsignarInspectorDTO dto,
            Authentication authentication) {
        return funcionarioService.asignarInspector(quejaId, dto, authentication.getName());
    }

    // Autoriza la reparación y asigna automáticamente un especialista.
    @PostMapping("/{quejaId}/autorizar-reparacion")
    public MensajeResponse autorizarReparacion(
            @PathVariable Integer quejaId,
            @Valid @RequestBody AutorizarReparacionDTO dto,
            Authentication authentication) {
        return funcionarioService.autorizarReparacion(quejaId, dto, authentication.getName());
    }

    // Cierra administrativamente la queja.
    @PostMapping("/{quejaId}/cierre")
    public MensajeResponse cierreAdministrativo(
            @PathVariable Integer quejaId,
            @Valid @RequestBody CierreAdministrativoDTO dto,
            Authentication authentication) {
        return funcionarioService.cierreAdministrativo(quejaId, dto, authentication.getName());
    }

    // Rechaza o devuelve la queja al estado anterior.
    @PostMapping("/{quejaId}/rechazar-devolver")
    public MensajeResponse rechazarODevolver(
            @PathVariable Integer quejaId,
            @Valid @RequestBody RechazoDevolucionDTO dto,
            Authentication authentication) {
        return funcionarioService.rechazarODevolver(quejaId, dto, authentication.getName());
    }
}
