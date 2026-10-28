package com.muni.backend.bitacora.service;

import com.muni.backend.bitacora.model.Bitacora;
import com.muni.backend.bitacora.repository.BitacoraRepository;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Servicio centralizado de auditoría.
@Service
@RequiredArgsConstructor
public class BitacoraService {

    private final BitacoraRepository bitacoraRepository;
    private final CredencialRepository credencialRepository;
    private final ClientIpService clientIpService;

    // Registra una acción con el usuario autenticado.
    @Transactional
    public void registrar(String tablaAfectada,
                          String campoAfectado,
                          String accion,
                          String registroAntiguo,
                          String registroNuevo) {
        registrar(null, tablaAfectada, null, campoAfectado, accion, registroAntiguo, registroNuevo);
    }

    // Registra resolviendo el username desde userId.
    @Transactional
    public void registrar(Integer userId,
                          String usuarioAccion,
                          String tablaAfectada,
                          String idAfectado,
                          String campoAfectado,
                          String accion,
                          String registroAntiguo,
                          String registroNuevo) {

        String username = usuarioAccion;
        if ((username == null || username.isBlank()) && userId != null) {
            username = credencialRepository.findById(userId)
                    .map(Credencial::getUsername)
                    .orElse(null);
        }

        registrar(username, tablaAfectada, idAfectado, campoAfectado, accion, registroAntiguo, registroNuevo);
    }

    // Registra con el username explícito o el del JWT.
    @Transactional
    public void registrar(String usuarioAccion,
                          String tablaAfectada,
                          String idAfectado,
                          String campoAfectado,
                          String accion,
                          String registroAntiguo,
                          String registroNuevo) {

        String username = (usuarioAccion != null && !usuarioAccion.isBlank())
                ? usuarioAccion.trim()
                : obtenerUsernameDelToken();

        Credencial usuario = username != null
                ? credencialRepository.findByUsernameIgnoreCase(username).orElse(null)
                : null;

        Bitacora bitacora = new Bitacora();
        bitacora.setUsuario(usuario);
        bitacora.setUsuarioAccion(username);
        bitacora.setDireccionIp(clientIpService.obtenerIp());
        bitacora.setTablaAfectada(tablaAfectada);
        bitacora.setIdAfectado(idAfectado);
        bitacora.setCampoAfectado(campoAfectado);
        bitacora.setAccion(accion);
        bitacora.setRegistroAntiguo(registroAntiguo);
        bitacora.setRegistroNuevo(registroNuevo);

        bitacoraRepository.save(bitacora);
    }

    private String obtenerUsernameDelToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
