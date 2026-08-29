package com.muni.backend.usuarios.controller;

import com.muni.backend.usuarios.dto.UsuarioRegistroDTO;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro-ciudadano")
    public ResponseEntity<String> registrarCiudadano(@RequestBody UsuarioRegistroDTO dto) {
        try {
            usuarioService.registrarCiudadano(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Su cuenta ha sido creada exitosamente. Ya puede iniciar sesión.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el registro: " + e.getMessage());
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
}