package com.muni.backend.usuarios.service;

import com.muni.backend.quejas.dto.shared.EmpleadoDTO;
import com.muni.backend.quejas.dto.shared.GuardarEmpleadoDTO;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.model.RolUser;
import com.muni.backend.security.repository.CredencialRepository;
import com.muni.backend.security.repository.RolUserRepository;
import com.muni.backend.security.service.AuthService;
import com.muni.backend.usuarios.dto.ActualizarPerfilDTO;
import com.muni.backend.usuarios.dto.PerfilUsuarioDTO;
import com.muni.backend.usuarios.dto.UsuarioRegistroDTO;
import com.muni.backend.usuarios.dto.UsuarioResumenDTO;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RolUserRepository rolUserRepository;
    @Autowired
    private CredencialRepository credencialRepository;

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
        // Complejidad de contraseña (mínimo 6 caracteres, al menos 1 mayúscula, 1 número y 1 carácter especial)
        if (!dto.getPassword().matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos 6 caracteres, una letra mayúscula, un número y un carácter especial.");
        }
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
    public PerfilUsuarioDTO obtenerPerfilCiudadano(String identificador) {
        Usuario usuario = usuarioRepository.findByCredencial_Username(identificador)
                .or(() -> usuarioRepository.findByCorreoIgnoreCase(identificador))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en la base de datos."));

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
    public void actualizarPerfilCiudadano(String identificadorActual, ActualizarPerfilDTO dto) {
        // 1. Validar campos obligatorios
        if (dto.getTelefono() == null || dto.getTelefono().trim().isEmpty() ||
                dto.getDireccion() == null || dto.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el teléfono y la dirección.");
        }

        String telefonoLimpio = dto.getTelefono().trim();
        if (!telefonoLimpio.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El número de teléfono debe constar de 8 dígitos.");
        }

        // 2. Buscar usuario por credencial o correo en base de datos
        Usuario usuario = usuarioRepository.findByCredencial_Username(identificadorActual)
                .or(() -> usuarioRepository.findByCorreoIgnoreCase(identificadorActual))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en la base de datos."));

        // 3. Actualizar telefono y direccion (DPI, nombres y correo son inmutables)
        usuario.setTelefono(telefonoLimpio);
        usuario.setDireccion(dto.getDireccion().trim());

        // 4. Cambio opcional de contrasena
        if (dto.getNuevaPassword() != null && !dto.getNuevaPassword().trim().isEmpty()) {
            Credencial credencial = usuario.getCredencial();
            if (credencial == null) {
                throw new IllegalArgumentException("No se encontró una credencial asociada a este usuario.");
            }

            // Validar que la contrasena actual sea correcta
            String hashPasswordActual = credencial.getPassword();
            if (dto.getPasswordActual() == null || dto.getPasswordActual().isEmpty() ||
                    !passwordEncoder.matches(dto.getPasswordActual(), hashPasswordActual)) {
                throw new IllegalArgumentException("La contraseña actual ingresada es incorrecta.");
            }

            // Validar coincidencia de nueva contrasena
            if (!dto.getNuevaPassword().equals(dto.getConfirmarNuevaPassword())) {
                throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
            }

            // Validar longitud minima
            if (dto.getNuevaPassword().trim().length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
            }

            // Encriptar y actualizar contrasena
            credencial.setPassword(passwordEncoder.encode(dto.getNuevaPassword().trim()));
        }

        // 5. Guardar cambios en base de datos
        usuario.setFechaModificacion(LocalDateTime.now());
        usuario.setUsuarioModificacion(identificadorActual);
        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorDpi(String dpi) {
        return usuarioRepository.findByDpi(dpi).orElse(null);
    }

    public List<UsuarioResumenDTO> listarPorRol(String nombreRol) {
        List<Usuario> usuarios = usuarioRepository.findByRolActivo(nombreRol);
        return usuarios.stream().map(u -> new UsuarioResumenDTO(
                u.getUsuarioId(),
                u.getNombres(),
                u.getApellidos(),
                u.getCorreo(),
                u.getDependencia() != null ? u.getDependencia().getNombreDependencia() : null
        )).collect(Collectors.toList());
    }

    // Verifica si un correo electronico esta registrado
    public boolean existePorCorreo(String correo) {
        return correo != null && usuarioRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    public List<EmpleadoDTO> listarUsuariosInternos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAEmpleadoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registrarEmpleado(GuardarEmpleadoDTO dto) {
        String nombreRolBD = mapearNombreRolBD(dto.getRol());
        RolUser rol = rolUserRepository.findByNombreRol(nombreRolBD)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: " + nombreRolBD));

        // 1. Primero se crea y guarda la Credencial (genera el user_id serial)
        Credencial credencial = new Credencial();
        credencial.setUsername(dto.getCorreoInstitucional());
        credencial.setPassword(passwordEncoder.encode("Muni123456"));
        credencial.setEstado("ACTIVO");
        credencial.setRolUser(rol);

        Credencial credencialGuardada = credencialRepository.save(credencial);

        // 2. Luego se crea y guarda el Usuario asociando la Credencial persistida
        Usuario usuario = new Usuario();
        usuario.setDpi(dto.getDpi());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreoInstitucional());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEstado("ACTIVO");
        usuario.setCredencial(credencialGuardada);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void actualizarEmpleado(Long id, GuardarEmpleadoDTO dto) {
        // 1. Buscar el usuario
        Usuario usuario = usuarioRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));

        // 2. Actualizar datos personales
        usuario.setDpi(dto.getDpi());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreoInstitucional());
        usuario.setTelefono(dto.getTelefono());

        // 3. Mapear el nombre que viene del formulario de Angular al nombre exacto en la BD
        String nombreRolBD = mapearNombreRolBD(dto.getRol());

        // 4. Buscar la entidad RolUser correspondiente
        RolUser nuevoRol = rolUserRepository.findByNombreRol(nombreRolBD)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en BD: " + nombreRolBD));

        // 5. Asignar el nuevo rol a la credencial del usuario
        if (usuario.getCredencial() != null) {
            usuario.getCredencial().setRolUser(nuevoRol);
        }

        // 6. Guardar cambios
        usuarioRepository.save(usuario);
    }

    private String mapearNombreRolBD(String rolFormulario) {
        if (rolFormulario == null) return "CIUDADANO";

        switch (rolFormulario.trim()) {
            case "Administrador":
                return "ADMINISTRADOR";
            case "Funcionario Municipal":
                return "FUNCIONARIO_MUNICIPAL";
            case "Inspector de Campo":
                return "INSPECTOR_CAMPO";
            case "Especialista Técnico":
                return "ESPECIALISTA_TECNICO";
            case "Ciudadano":
                return "CIUDADANO";
            default:
                return "CIUDADANO";
        }
    }

    @Transactional
    public void cambiarEstadoEmpleado(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));

        usuario.setEstado(activo ? "ACTIVO" : "INACTIVO");
        usuarioRepository.save(usuario);
    }

    public EmpleadoDTO convertirAEmpleadoDTO(Usuario usuario) {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setId(usuario.getUsuarioId().longValue());
        dto.setDpi(usuario.getDpi());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setCorreoInstitucional(usuario.getCorreo());
        dto.setTelefono(usuario.getTelefono());
        dto.setActivo("ACTIVO".equalsIgnoreCase(usuario.getEstado()));

        // Obtener la dependencia si aplica
        if (usuario.getDependencia() != null) {
            dto.setDependencia(usuario.getDependencia().getNombreDependencia());
        }

        // Obtener el rol desde Credencial -> RolUser
        if (usuario.getCredencial() != null && usuario.getCredencial().getRolUser() != null) {
            String rolBD = usuario.getCredencial().getRolUser().getNombreRol();
            dto.setRol(mapearNombreRolVista(rolBD));
        } else {
            dto.setRol("Funcionario Municipal");
        }

        return dto;
    }

    private String mapearNombreRolVista(String rolBD) {
        if (rolBD == null) return "Ciudadano";

        switch (rolBD.trim().toUpperCase()) {
            case "ADMINISTRADOR":
            case "ROLE_ADMINISTRADOR":
                return "Administrador";
            case "FUNCIONARIO_MUNICIPAL":
            case "ROLE_FUNCIONARIO":
                return "Funcionario Municipal";
            case "INSPECTOR_CAMPO":
            case "ROLE_INSPECTOR":
                return "Inspector de Campo";
            case "ESPECIALISTA_TECNICO":
            case "ROLE_ESPECIALISTA":
                return "Especialista Técnico";
            case "CIUDADANO":
            case "ROLE_CIUDADANO":
                return "Ciudadano";
            default:
                return "Ciudadano";
        }
    }
}