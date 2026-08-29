package com.muni.backend.security.service;

import com.muni.backend.security.dto.*;
import com.muni.backend.security.model.Bitacora;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.model.RolUser;
import com.muni.backend.security.repository.BitacoraRepository;
import com.muni.backend.security.repository.CredencialRepository;
import com.muni.backend.security.repository.RolUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredencialRepository credencialRepository;
    private final RolUserRepository rolUserRepository;
    private final BitacoraRepository bitacoraRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Credencial usuario = credencialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (credencialRepository.findByUsername(request.getUsername()).isPresent()) {
            throw excitingIllegalArgument("El username ya existe");
        }

        // Por defecto rol Ciudadano/Base (ID 2 o según parametrización)
        RolUser rolUsuario = rolUserRepository.findById(2)
                .orElseThrow(() -> new IllegalArgumentException("Rol base no encontrado"));

        Credencial credencial = new Credencial();
        credencial.setUsername(request.getUsername());
        credencial.setPassword(passwordEncoder.encode(request.getPassword()));
        credencial.setRolUser(rolUsuario);

        Credencial saved = credencialRepository.save(credencial);
        return new RegisterResponse(saved.getUserId());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Credencial usuario = credencialRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credencialRepository.save(usuario);
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