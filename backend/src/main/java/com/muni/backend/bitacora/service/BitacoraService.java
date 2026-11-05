package com.muni.backend.bitacora.service;

import com.muni.backend.bitacora.dto.BitacoraFiltroDTO;
import com.muni.backend.bitacora.dto.BitacoraRespuestaDTO;
import com.muni.backend.bitacora.model.Bitacora;
import com.muni.backend.bitacora.repository.BitacoraRepository;
import com.muni.backend.security.model.Credencial;
import com.muni.backend.security.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Servicio centralizado de auditoría.
@Service
@RequiredArgsConstructor
public class BitacoraService {

    private static final int MAX_FILAS_EXPORTACION = 5_000;
    private static final int MAX_DESCRIPCION = 500;
    private static final String USUARIO_SISTEMA = "SISTEMA";

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

        if (username == null || username.isBlank()) {
            username = USUARIO_SISTEMA;
        }

        Credencial usuario = credencialRepository.findByUsernameIgnoreCase(username).orElse(null);

        Bitacora bitacora = new Bitacora();
        bitacora.setUserId(usuario != null ? usuario.getUserId() : null);
        bitacora.setUsuarioAccion(username);
        bitacora.setDireccionIp(clientIpService.obtenerIp());
        bitacora.setTablaAfectada(tablaAfectada);
        bitacora.setIdAfectado(idAfectado);
        bitacora.setCampoAfectado(campoAfectado);
        bitacora.setAccion(accion);
        bitacora.setDescripcion(construirDescripcion(
                tablaAfectada, idAfectado, campoAfectado, accion, registroAntiguo, registroNuevo));
        bitacora.setRegistroAntiguo(registroAntiguo);
        bitacora.setRegistroNuevo(registroNuevo);

        bitacoraRepository.save(bitacora);
    }

    // Consulta paginada de la bitácora con filtros opcionales.
    @Transactional(readOnly = true)
    public Page<BitacoraRespuestaDTO> listar(BitacoraFiltroDTO filtro, Pageable pageable) {
        return bitacoraRepository.buscarConFiltros(
                normalizar(filtro.getUsuario()),
                normalizar(filtro.getTabla()),
                normalizar(filtro.getAccion()),
                normalizar(filtro.getIdAfectado()),
                normalizar(filtro.getCampoAfectado()),
                inicio(filtro.getFechaInicio()),
                fin(filtro.getFechaFin()),
                pageable
        ).map(this::convertirADTO);
    }

    // Devuelve las filas que se incluirán en un reporte de auditoría.
    @Transactional(readOnly = true)
    public List<BitacoraRespuestaDTO> listarParaExportar(BitacoraFiltroDTO filtro) {
        Pageable limite = PageRequest.of(0, MAX_FILAS_EXPORTACION);
        return listar(filtro, limite).getContent();
    }

    private LocalDateTime inicio(LocalDate fechaInicio) {
        return fechaInicio != null
                ? fechaInicio.atStartOfDay()
                : LocalDateTime.of(1900, 1, 1, 0, 0);
    }

    private LocalDateTime fin(LocalDate fechaFin) {
        return fechaFin != null
                ? fechaFin.atTime(LocalTime.MAX)
                : LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999);
    }

    private String normalizar(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }

    private BitacoraRespuestaDTO convertirADTO(Bitacora bitacora) {
        BitacoraRespuestaDTO dto = new BitacoraRespuestaDTO();
        dto.setRegistroId(bitacora.getRegistroId());
        dto.setUserId(bitacora.getUserId());
        dto.setUsuarioAccion(bitacora.getUsuarioAccion());
        dto.setDireccionIp(bitacora.getDireccionIp());
        dto.setFechaHora(bitacora.getFechaHora());
        dto.setTablaAfectada(bitacora.getTablaAfectada());
        dto.setIdAfectado(bitacora.getIdAfectado());
        dto.setCampoAfectado(bitacora.getCampoAfectado());
        dto.setAccion(bitacora.getAccion());
        dto.setDescripcion(bitacora.getDescripcion());
        dto.setRegistroAntiguo(bitacora.getRegistroAntiguo());
        dto.setRegistroNuevo(bitacora.getRegistroNuevo());
        return dto;
    }

    // Construye una descripción legible y acotada para cada evento.
    private String construirDescripcion(String tablaAfectada,
                                        String idAfectado,
                                        String campoAfectado,
                                        String accion,
                                        String registroAntiguo,
                                        String registroNuevo) {
        StringBuilder descripcion = new StringBuilder();
        descripcion.append(accion != null ? accion : "EVENTO");

        if (tablaAfectada != null && !tablaAfectada.isBlank()) {
            descripcion.append(" en ").append(tablaAfectada);
        }
        if (campoAfectado != null && !campoAfectado.isBlank()) {
            descripcion.append(" (").append(campoAfectado).append(")");
        }
        if (idAfectado != null && !idAfectado.isBlank()) {
            descripcion.append(", registro ").append(idAfectado);
        }

        if (registroAntiguo != null && registroNuevo != null) {
            descripcion.append(": ")
                    .append(recortar(registroAntiguo, 120))
                    .append(" -> ")
                    .append(recortar(registroNuevo, 160));
        } else if (registroNuevo != null && !registroNuevo.isBlank()) {
            descripcion.append(": ").append(recortar(registroNuevo, 200));
        }

        return recortar(descripcion.toString(), MAX_DESCRIPCION);
    }

    private String recortar(String valor, int maximo) {
        if (valor == null || valor.length() <= maximo) {
            return valor;
        }
        return valor.substring(0, maximo - 1) + "…";
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
