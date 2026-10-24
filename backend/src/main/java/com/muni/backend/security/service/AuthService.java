package com.muni.backend.security.service;

import com.muni.backend.security.dto.*;
import com.muni.backend.security.model.Bitacora;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.model.RecuperacionPassword;
import com.muni.backend.security.model.RolUser;
import com.muni.backend.security.repository.BitacoraRepository;
import com.muni.backend.security.repository.CredencialRepository;
import com.muni.backend.security.repository.RecuperacionPasswordRepository;
import com.muni.backend.security.repository.RolUserRepository;
import com.muni.backend.shared.service.EmailService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CredencialRepository credencialRepository;
    private final RolUserRepository rolUserRepository;
    private final BitacoraRepository bitacoraRepository;
    private final RecuperacionPasswordRepository recuperacionPasswordRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        Credencial usuario = credencialRepository.findByUsername(username)
                .or(() -> usuarioRepository.findByCorreoIgnoreCase(username).map(Usuario::getCredencial))
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String token = jwtService.generarToken(usuario);
        String rol = usuario.getRolUser() != null ? usuario.getRolUser().getNombreRol() : "CIUDADANO";
        return new AuthResponse(token, rol, usuario.getUsername());
    }

    // Para otros roles (funcionario, inspector, etc.)
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (credencialRepository.findByUsername(request.getUsername()).isPresent()) {
            throw excitingIllegalArgument("El username ya existe");
        }
        RolUser rolUsuario = rolUserRepository.findById(2)
                .orElseThrow(() -> new IllegalArgumentException("Rol base no encontrado"));

        Credencial credencial = new Credencial();
        credencial.setUsername(request.getUsername());
        credencial.setPassword(passwordEncoder.encode(request.getPassword()));
        credencial.setRolUser(rolUsuario);

        Credencial saved = credencialRepository.save(credencial);
        return new RegisterResponse(saved.getUserId());
    }

    /**
     * Busca la credencial asociada al correo proporcionado.
     * Revisa tanto en Usuario (por correo) como en Credencial (por username).
     */
    @Transactional(readOnly = true)
    public Credencial obtenerCredencialPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo ingresado no es válido.");
        }
        String correoLimpio = correo.trim();

        // 1. Buscar en entidad Usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoIgnoreCase(correoLimpio);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getCredencial() != null) {
            return usuarioOpt.get().getCredencial();
        }

        // 2. Buscar en Credencial por username (para ciudadanos registrados con username=correo)
        return credencialRepository.findByUsernameIgnoreCase(correoLimpio)
                .orElseThrow(() -> new IllegalArgumentException("El correo ingresado no se encuentra registrado."));
    }

    /**
     * Valida si el correo o usuario existe en el sistema.
     */
    @Transactional(readOnly = true)
    public boolean existeUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        String correoLimpio = correo.trim();
        return usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)
                || credencialRepository.findByUsernameIgnoreCase(correoLimpio).isPresent();
    }

    /**
     * Paso 1: Solicitar código OTP para recuperación de contraseña.
     * Genera código de 6 dígitos, guarda en recuperacion_password y lo envía por correo.
     */
    @Transactional
    public void solicitarCodigoRecuperacion(String correo) {
        Credencial credencial = obtenerCredencialPorCorreo(correo);

        // Invalidar solicitudes pendientes previas para este usuario
        List<RecuperacionPassword> anteriores = recuperacionPasswordRepository.findByCredencial_UserIdAndUtilizadoFalse(credencial.getUserId());
        if (!anteriores.isEmpty()) {
            anteriores.forEach(r -> r.setUtilizado(true));
            recuperacionPasswordRepository.saveAll(anteriores);
        }

        // Generar código numérico de 6 dígitos seguro
        String codigo = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // Persistir en tabla recuperacion_password con vigencia de 15 minutos
        RecuperacionPassword recuperacion = RecuperacionPassword.builder()
                .credencial(credencial)
                .codigoVerificacion(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                .utilizado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
        recuperacionPasswordRepository.save(recuperacion);

        log.info("[RECUPERACION_PASSWORD] Código OTP generado para {} (user_id {}): {}", correo, credencial.getUserId(), codigo);

        // Enviar notificación por correo electrónico
        emailService.enviarCodigoRecuperacion(correo.trim(), codigo);
    }

    /**
     * Paso 2: Validar código OTP recibido por el usuario.
     */
    @Transactional(readOnly = true)
    public boolean validarCodigoRecuperacion(String correo, String codigo) {
        if (correo == null || correo.isBlank() || codigo == null || codigo.isBlank()) {
            return false;
        }

        try {
            Credencial credencial = obtenerCredencialPorCorreo(correo);
            return recuperacionPasswordRepository
                    .findTopByCredencial_UserIdAndCodigoVerificacionAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                            credencial.getUserId(), codigo.trim(), LocalDateTime.now()
                    )
                    .isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Paso 3: Restablecer contraseña con el nuevo valor.
     * Valida que exista una solicitud activa, actualiza la contraseña y marca el código como utilizado.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.getCorreo() == null || request.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria.");
        }
        if (request.getNewPassword().trim().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        Credencial credencial = obtenerCredencialPorCorreo(request.getCorreo());

        // Verificar que exista una solicitud válida y vigente en recuperacion_password
        RecuperacionPassword recuperacion = recuperacionPasswordRepository
                .findTopByCredencial_UserIdAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        credencial.getUserId(), LocalDateTime.now()
                )
                .orElseThrow(() -> new IllegalArgumentException("El código ingresado es inválido o ha expirado."));

        // Actualizar contraseña hasheada
        credencial.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        credencial.setFechaModificacion(LocalDateTime.now());
        credencial.setUsuarioModificacion(request.getCorreo().trim());
        credencialRepository.save(credencial);

        // Marcar el registro de recuperación como utilizado
        recuperacion.setUtilizado(true);
        recuperacionPasswordRepository.save(recuperacion);

        // Invalidar cualquier otro registro pendiente
        List<RecuperacionPassword> pendientes = recuperacionPasswordRepository.findByCredencial_UserIdAndUtilizadoFalse(credencial.getUserId());
        if (!pendientes.isEmpty()) {
            pendientes.forEach(r -> r.setUtilizado(true));
            recuperacionPasswordRepository.saveAll(pendientes);
        }

        // Registrar acción en bitácora
        BitacoraDTO bitacora = new BitacoraDTO();
        bitacora.setUserId(credencial.getUserId());
        bitacora.setMicroservicioAfectado("Seguridad");
        bitacora.setEndpoint("/auth/recuperar/reset-password");
        bitacora.setAccion("RESTABLECER_CONTRASENA");
        bitacora.setIdAfectado(String.valueOf(credencial.getUserId()));
        registrarBitacora(bitacora);
    }

    @Transactional
    public void deleteCredencial(Integer userId) {
        if (!credencialRepository.existsById(userId)) {
            throw new UsernameNotFoundException("Credencial no encontrada con ID: " + userId);
        }
        credencialRepository.deleteById(userId);
    }

    @Transactional
    public void registrarBitacora(BitacoraDTO dto) {
        Bitacora bitacora = new Bitacora();

        if (dto.getUserId() != null) {
            credencialRepository.findById(dto.getUserId()).ifPresent(bitacora::setUsuario);
        }

        bitacora.setMicroservicioAfectado(dto.getMicroservicioAfectado());
        bitacora.setEndpoint(dto.getEndpoint());
        bitacora.setAccion(dto.getAccion());
        bitacora.setIdAfectado(dto.getIdAfectado());
        bitacora.setFechaHora(LocalDateTime.now());

        bitacoraRepository.save(bitacora);
    }

    private IllegalArgumentException excitingIllegalArgument(String msg) {
        return new IllegalArgumentException(msg);
    }

    @Transactional
    public Credencial crearCredencialCiudadano(String username, String rawPassword) {
        if (credencialRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ingresado ya está asociado a otra cuenta.");
        }

        // Rol CIUDADANO (ID 1)
        RolUser rolCiudadano = rolUserRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Rol CIUDADANO no configurado en el sistema."));

        Credencial credencial = new Credencial();
        credencial.setUsername(username);
        credencial.setPassword(passwordEncoder.encode(rawPassword));
        credencial.setRolUser(rolCiudadano);
        credencial.setUsuarioCreacion("REGISTRO_PUBLICO");

        return credencialRepository.save(credencial);
    }
}