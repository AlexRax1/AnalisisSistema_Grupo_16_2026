package com.muni.backend.usuarios.controller;

import com.muni.backend.usuarios.dto.UsuarioRegistroDTO;
import com.muni.backend.usuarios.dto.UsuarioResumenDTO;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/usuarios", "/usuarios"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    
    @PostMapping("/registro-ciudadano")
    public ResponseEntity<?> registrarCiudadano(@RequestBody UsuarioRegistroDTO dto) {
        try {
            usuarioService.registrarCiudadano(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Su cuenta ha sido creada exitosamente. Ya puede iniciar sesión."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el registro: " + e.getMessage()));
        }
    }

    @GetMapping("/dpi/{dpi}")
    public ResponseEntity<Usuario> obtenerPorDpi(@PathVariable String dpi) {
        Usuario usuario = usuarioService.buscarPorDpi(dpi);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/inspectores")
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> listarInspectores() {
        try {
            List<UsuarioResumenDTO> inspectores = usuarioService.listarPorRol("INSPECTOR_CAMPO");
            return ResponseEntity.ok(inspectores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener inspectores: " + e.getMessage()));
        }
    }

    @GetMapping("/especialistas")
    @PreAuthorize("hasAuthority('FUNCIONARIO_MUNICIPAL')")
    public ResponseEntity<?> listarEspecialistas() {
        try {
            List<UsuarioResumenDTO> especialistas = usuarioService.listarPorRol("ESPECIALISTA_TECNICO");
            return ResponseEntity.ok(especialistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener especialistas: " + e.getMessage()));
        }
    }
}