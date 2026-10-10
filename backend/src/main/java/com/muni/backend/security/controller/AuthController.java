package com.muni.backend.security.controller;

import com.muni.backend.security.dto.*;
import com.muni.backend.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/recuperar/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Contraseña restablecida exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> rollbackCredencial(@PathVariable Integer userId) {
        try {
            authService.deleteCredencial(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/bitacora/registrar")
    public ResponseEntity<?> registrarBitacora(@RequestBody BitacoraDTO dto) {
        authService.registrarBitacora(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperar/solicitar-codigo")
    public ResponseEntity<?> solicitarCodigo(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe ingresar el correo electrónico."));
        }

        try {
            authService.solicitarCodigoRecuperacion(correo);
            return ResponseEntity.ok(Map.of("mensaje", "Código de verificación enviado al correo."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Ocurrió un error al procesar la solicitud."));
        }
    }

    @PostMapping("/recuperar/validar-codigo")
    public ResponseEntity<?> validarCodigo(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        String codigo = request.get("codigo");

        if (correo == null || correo.isBlank() || codigo == null || codigo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe ingresar los campos obligatorios."));
        }

        boolean esValido = authService.validarCodigoRecuperacion(correo, codigo);
        if (!esValido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "El código ingresado es inválido o ha expirado."));
        }

        return ResponseEntity.ok(Map.of("mensaje", "Código verificado con éxito."));
    }
}