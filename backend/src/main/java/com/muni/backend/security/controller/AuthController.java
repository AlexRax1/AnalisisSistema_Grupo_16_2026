package com.muni.backend.security.controller;

import com.muni.backend.security.dto.*;
import com.muni.backend.security.service.AuthService;
import com.muni.backend.shared.service.EmailService;
import com.muni.backend.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

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
            usuarioService.eliminarCodigoVerificacion(request.getCorreo());
            return ResponseEntity.ok("Contraseña restablecida exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

        // 1. Validar que el correo exista (FA05)[cite: 10]
        if (!usuarioService.existePorCorreo(correo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El correo ingresado no se encuentra registrado."); // FA05[cite: 10]
        }

        // 2. Generar código aleatorio y guardarlo en BD o caché con expiración
        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuarioService.guardarCodigoVerificacion(correo, codigo);

        // 3. Enviar el correo real
        emailService.enviarCodigoRecuperacion(correo, codigo);

        return ResponseEntity.ok(Map.of("mensaje", "Código de verificación enviado al correo."));
    }

    @PostMapping("/recuperar/validar-codigo")
    public ResponseEntity<?> validarCodigo(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        String codigo = request.get("codigo");

        boolean esValido = usuarioService.validarCodigo(correo, codigo);
        if (!esValido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El código ingresado es inválido o ha expirado."); // FA06[cite: 10]
        }

        return ResponseEntity.ok(Map.of("mensaje", "Código verificado con éxito."));
    }
}