package com.muni.backend.usuarios.service;

import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.model.RolUser;
import com.muni.backend.security.repository.CredencialRepository;
import com.muni.backend.security.repository.RolUserRepository;
import com.muni.backend.security.service.AuthService;
import com.muni.backend.usuarios.dto.ActualizarPerfilDTO;
import com.muni.backend.usuarios.dto.PerfilUsuarioDTO;
import com.muni.backend.usuarios.dto.UsuarioRegistroDTO;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registrarCiudadano(UsuarioRegistroDTO dto) {


        // --- campos nulos ---
        if (dto.getDpi() == null || dto.getNombres() == null || dto.getApellidos() == null ||
                dto.getCorreo() == null || dto.getTelefono() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }


        // Coincidencia de contraseñas
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas ingresadas no coinciden.");
        }


         /* descomentar despues
        // Complejidad de contraseña (mínimo 6 caracteres, al menos 1 mayúscula, 1 número y 1 carácter especial)
        if (!dto.getPassword().matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos 6 caracteres, una letra mayúscula, un número y un carácter especial.");
        }*/
        // Teléfono de 8 dígitos
        if (!dto.getTelefono().matches("^[0-9]{8}$")) {
            throw new IllegalArgumentException("El número telefónico debe contener exactamente 8 dígitos numéricos.");
        }
        // DPI de 13 dígitos
        if (!dto.getDpi().matches("^[0-9]{13}$")) {
            throw new IllegalArgumentException("El DPI debe contener exactamente 13 dígitos numéricos.");
        }




        // --- 2. VALIDACIÓN DE DUPLICADOS (FA02, FA03) ---
        if (usuarioRepository.existsByDpi(dto.getDpi())) {
            throw new IllegalArgumentException("El número de DPI ingresado ya se encuentra registrado en el sistema.");
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ingresado ya está asociado a otra cuenta.");
        }






        // --- creacion de credenciales comunicando con modulo "Security" ---
        // se asigna rol Ciudadano por defecto al registrar un usuario
        // --- 4. DELEGACIÓN A AUTH (Crea credencial, valida username y hashea contraseña) ---
        Credencial credencialGuardada = authService.crearCredencialCiudadano(dto.getCorreo(), dto.getPassword());

        // --- 5. CREACIÓN DE USUARIO (Módulo Usuarios) ---
        Usuario usuario = new Usuario();
        usuario.setCredencial(credencialGuardada);
        usuario.setDpi(dto.getDpi());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        usuario.setUsuarioCreacion("REGISTRO_PUBLICO");

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioDTO obtenerPerfilCiudadano(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        return PerfilUsuarioDTO.builder()
                .dpi(usuario.getDpi())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .telefono(usuario.getTelefono())
                .direccion(usuario.getDireccion())
                .correo(usuario.getCorreo())
                .build();
    }

    @Transactional
    public void actualizarPerfilCiudadano(String correoUsuarioActual, ActualizarPerfilDTO dto) {

        if (dto.getTelefono() == null || dto.getTelefono().trim().isEmpty() ||
                dto.getDireccion() == null || dto.getDireccion().trim().isEmpty() ||
                dto.getCorreo() == null || dto.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar los campos obligatorios.");
        }

        String telefonoLimpio = dto.getTelefono().trim();
        if (!telefonoLimpio.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El número de teléfono debe constar de 8 dígitos.");
        }

        String correoNuevo = dto.getCorreo().trim().toLowerCase();
        if (!correoNuevo.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Debe ingresar un formato de correo electrónico válido.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuarioActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!usuario.getCorreo().equalsIgnoreCase(correoNuevo)) {
            if (usuarioRepository.existsByCorreo(correoNuevo)) {
                throw new IllegalArgumentException("El correo electrónico ingresado ya se encuentra registrado por otro usuario.");
            }
            usuario.setCorreo(correoNuevo);
        }

        usuario.setTelefono(telefonoLimpio);
        usuario.setDireccion(dto.getDireccion().trim());

        // 4. Lógica Opcional de Cambio de Contraseña (FA03, FA04)
        if (dto.getNuevaPassword() != null && !dto.getNuevaPassword().trim().isEmpty()) {

            Credencial credencial = usuario.getCredencial();
            if (credencial == null) {
                throw new IllegalArgumentException("No se encontró una credencial asociada a este usuario.");
            }

            // FA04: Validar contraseña actual con el hash almacenado en la credencial
            String hashPasswordActual = credencial.getPassword(); // O credencial.getClave() / credencial.getContrasenia() según tu entidad Credencial

            if (dto.getPasswordActual() == null || dto.getPasswordActual().isEmpty() ||
                    !passwordEncoder.matches(dto.getPasswordActual(), hashPasswordActual)) {
                throw new IllegalArgumentException("La contraseña actual ingresada es incorrecta.");
            }

            // Validar coincidencia de nueva contraseña
            if (!dto.getNuevaPassword().equals(dto.getConfirmarNuevaPassword())) {
                throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
            }

            // RN05: Formato de Contraseña (mínimo 6 caracteres, mayúscula, número y símbolo) (FA03)
            String regexPassword = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$";
            if (!dto.getNuevaPassword().matches(regexPassword)) {
                throw new IllegalArgumentException("La nueva contraseña no cumple con los requisitos de seguridad.");
            }

            // Actualizar contraseña encriptada en la Credencial
            credencial.setPassword(passwordEncoder.encode(dto.getNuevaPassword()));
        }

        // Auditoría de modificación
        usuario.setFechaModificacion(LocalDateTime.now());
        usuario.setUsuarioModificacion(correoUsuarioActual);

        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorDpi(String dpi) {
        return usuarioRepository.findByDpi(dpi).orElse(null);
    }
}