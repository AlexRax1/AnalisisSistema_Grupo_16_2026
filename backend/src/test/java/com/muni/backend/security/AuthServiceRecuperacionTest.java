package com.muni.backend.security;

import com.muni.backend.security.dto.ResetPasswordRequest;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.model.RecuperacionPassword;
import com.muni.backend.security.model.RolUser;
import com.muni.backend.security.repository.BitacoraRepository;
import com.muni.backend.security.repository.CredencialRepository;
import com.muni.backend.security.repository.RecuperacionPasswordRepository;
import com.muni.backend.security.repository.RolUserRepository;
import com.muni.backend.security.service.AuthService;
import com.muni.backend.security.service.JwtService;
import com.muni.backend.shared.service.EmailService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRecuperacionTest {

    @Mock
    private CredencialRepository credencialRepository;
    @Mock
    private RolUserRepository rolUserRepository;
    @Mock
    private BitacoraRepository bitacoraRepository;
    @Mock
    private RecuperacionPasswordRepository recuperacionPasswordRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Credencial mockCredencial;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        mockCredencial = new Credencial();
        mockCredencial.setUserId(10);
        mockCredencial.setUsername("ciudadano@test.com");
        mockCredencial.setPassword("encodedOldPass");

        mockUsuario = new Usuario();
        mockUsuario.setUsuarioId(1);
        mockUsuario.setCorreo("ciudadano@test.com");
        mockUsuario.setCredencial(mockCredencial);
    }

    @Test
    void testSolicitarCodigoRecuperacion_Exitoso() {
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(mockUsuario));
        when(recuperacionPasswordRepository.findByCredencial_UserIdAndUtilizadoFalse(10)).thenReturn(Collections.emptyList());

        authService.solicitarCodigoRecuperacion("ciudadano@test.com");

        ArgumentCaptor<RecuperacionPassword> captor = ArgumentCaptor.forClass(RecuperacionPassword.class);
        verify(recuperacionPasswordRepository).save(captor.capture());
        RecuperacionPassword saved = captor.getValue();

        assertNotNull(saved.getCodigoVerificacion());
        assertEquals(6, saved.getCodigoVerificacion().length());
        assertFalse(saved.getUtilizado());
        assertTrue(saved.getFechaExpiracion().isAfter(LocalDateTime.now()));

        verify(emailService).enviarCodigoRecuperacion(eq("ciudadano@test.com"), eq(saved.getCodigoVerificacion()));
    }

    @Test
    void testSolicitarCodigoRecuperacion_CorreoNoExiste() {
        when(usuarioRepository.findByCorreoIgnoreCase("noexiste@test.com")).thenReturn(Optional.empty());
        when(credencialRepository.findByUsernameIgnoreCase("noexiste@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.solicitarCodigoRecuperacion("noexiste@test.com")
        );

        assertEquals("El correo ingresado no se encuentra registrado.", ex.getMessage());
        verify(recuperacionPasswordRepository, never()).save(any());
        verify(emailService, never()).enviarCodigoRecuperacion(anyString(), anyString());
    }

    @Test
    void testValidarCodigoRecuperacion_Valido() {
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(mockUsuario));
        RecuperacionPassword rec = RecuperacionPassword.builder()
                .recuperacionId(1)
                .credencial(mockCredencial)
                .codigoVerificacion("123456")
                .utilizado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        when(recuperacionPasswordRepository.findTopByCredencial_UserIdAndCodigoVerificacionAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(10), eq("123456"), any(LocalDateTime.class)
        )).thenReturn(Optional.of(rec));

        boolean esValido = authService.validarCodigoRecuperacion("ciudadano@test.com", "123456");

        assertTrue(esValido);
    }

    @Test
    void testValidarCodigoRecuperacion_Invalido() {
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(mockUsuario));
        when(recuperacionPasswordRepository.findTopByCredencial_UserIdAndCodigoVerificacionAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(10), eq("999999"), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        boolean esValido = authService.validarCodigoRecuperacion("ciudadano@test.com", "999999");

        assertFalse(esValido);
    }

    @Test
    void testResetPassword_Exitoso() {
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(mockUsuario));
        RecuperacionPassword rec = RecuperacionPassword.builder()
                .recuperacionId(1)
                .credencial(mockCredencial)
                .codigoVerificacion("123456")
                .utilizado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .build();

        when(recuperacionPasswordRepository.findTopByCredencial_UserIdAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(10), any(LocalDateTime.class)
        )).thenReturn(Optional.of(rec));
        when(passwordEncoder.encode("NuevaPassword123*")).thenReturn("encodedNewPass");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setCorreo("ciudadano@test.com");
        req.setNewPassword("NuevaPassword123*");

        authService.resetPassword(req);

        // Verifica que la contraseña fue actualizada
        assertEquals("encodedNewPass", mockCredencial.getPassword());
        verify(credencialRepository).save(mockCredencial);

        // Verifica que el código fue marcado como utilizado
        assertTrue(rec.getUtilizado());
        verify(recuperacionPasswordRepository).save(rec);

        // Verifica auditoria en bitacora
        verify(bitacoraRepository).save(any());
    }

    @Test
    void testResetPassword_SinSolicitudActiva() {
        when(usuarioRepository.findByCorreoIgnoreCase("ciudadano@test.com")).thenReturn(Optional.of(mockUsuario));
        when(recuperacionPasswordRepository.findTopByCredencial_UserIdAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                eq(10), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setCorreo("ciudadano@test.com");
        req.setNewPassword("NuevaPassword123*");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.resetPassword(req)
        );

        assertEquals("El código ingresado es inválido o ha expirado.", ex.getMessage());
        verify(credencialRepository, never()).save(any());
    }
}
