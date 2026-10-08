package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.exception.AccesoDenegadoException;
import com.muni.backend.quejas.exception.EstadoInvalidoException;
import com.muni.backend.quejas.exception.QuejaNoEncontradaException;
import com.muni.backend.quejas.model.*;
import com.muni.backend.quejas.repository.*;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuejaEstadoService {

    private final QuejaRepository quejaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialEstadoQuejaRepository historialRepository;
    private final InformeQuejaRepository informeRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;
    private final CatalogoService catalogoService;

    /**
     * Resuelve el Usuario a partir del username del token JWT.
     * Busca primero por correo, luego por credencial.username.
     */
    public Usuario resolverUsuario(String username) {
        return usuarioRepository.findByCorreo(username)
                .or(() -> usuarioRepository.findByCredencial_Username(username))
                .orElseThrow(() -> new AccesoDenegadoException(
                    "No se encontró el perfil de usuario registrado para: " + username));
    }

    /**
     * Busca una queja por ID o lanza excepción.
     */
    public Queja buscarQueja(Integer quejaId) {
        return quejaRepository.findById(quejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la queja con ID: " + quejaId));
    }

    /**
     * Valida que la queja esté en el estado esperado.
     */
    public void validarEstado(Queja queja, EstadoQueja estadoEsperado) {
        if (!estadoEsperado.getValor().equals(queja.getEstadoActual())) {
            throw new EstadoInvalidoException(
                "La queja no está en estado '" + estadoEsperado.getValor()
                + "'. Estado actual: " + queja.getEstadoActual());
        }
    }

    /**
     * Cambia el estado de la queja y registra el historial.
     */
    public void cambiarEstado(Queja queja, EstadoQueja nuevoEstado,
                              Usuario cambiadoPor, String comentario) {
        String estadoAnterior = queja.getEstadoActual();
        queja.setEstadoActual(nuevoEstado.getValor());
        queja.setFechaModificacion(LocalDateTime.now());
        quejaRepository.save(queja);

        HistorialEstadoQueja historial = new HistorialEstadoQueja();
        historial.setQueja(queja);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado.getValor());
        historial.setCambiadoPor(cambiadoPor);
        historial.setComentario(comentario);
        historialRepository.save(historial);
    }

    /**
     * Busca un usuario por ID, probando por usuario_id y por credencial.user_id.
     */
    public Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .or(() -> usuarioRepository.findByCredencial_UserId(id))
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró el usuario con ID: " + id));
    }

    /**
     * Valida que un usuario tenga el rol esperado.
     */
    public void validarRol(Usuario usuario, String rolEsperado) {
        String rol = usuario.getCredencial().getRolUser().getNombreRol();
        if (!rolEsperado.equals(rol)) {
            throw new EstadoInvalidoException(
                "El usuario seleccionado no tiene el rol de " + rolEsperado + ".");
        }
    }

    /**
     * Mapeo inverso de estados para devolución.
     */
    public EstadoQueja determinarEstadoAnterior(String estadoActual) {
        return switch (estadoActual) {
            case "EN INSPECCIÓN" -> EstadoQueja.REGISTRADA;
            case "EN VALIDACIÓN DE REPARACIÓN" -> EstadoQueja.EN_INSPECCION;
            case "EN REPARACIÓN TÉCNICA" -> EstadoQueja.EN_VALIDACION_REPARACION;
            case "PENDIENTE DE CIERRE" -> EstadoQueja.EN_REPARACION_TECNICA;
            case "REGISTRADA" -> throw new EstadoInvalidoException(
                    "No se puede devolver una queja en estado 'REGISTRADA'. Use rechazo definitivo.");
            default -> throw new EstadoInvalidoException(
                    "Estado no soportado para devolución: " + estadoActual);
        };
    }

    /**
     * Determina las acciones disponibles que un rol puede ejecutar sobre la queja en su estado actual.
     */
    public List<String> determinarAccionesDisponibles(Queja queja, Usuario usuarioActual, String rolNombre) {
        List<String> acciones = new ArrayList<>();
        String estado = queja.getEstadoActual();

        if ("FUNCIONARIO_MUNICIPAL".equals(rolNombre)) {
            if (queja.getFuncionario() == null || queja.getFuncionario().getUsuarioId().equals(usuarioActual.getUsuarioId())) {
                switch (estado) {
                    case "REGISTRADA" -> {
                        acciones.add("ASIGNAR_INSPECTOR");
                        acciones.add("RECHAZAR");
                    }
                    case "EN VALIDACIÓN DE REPARACIÓN" -> {
                        acciones.add("AUTORIZAR_REPARACION");
                        acciones.add("DEVOLVER");
                        acciones.add("RECHAZAR");
                    }
                    case "PENDIENTE DE CIERRE" -> {
                        acciones.add("CIERRE_ADMINISTRATIVO");
                        acciones.add("DEVOLVER");
                        acciones.add("RECHAZAR");
                    }
                    case "EN INSPECCIÓN", "EN REPARACIÓN TÉCNICA" -> {
                        acciones.add("DEVOLVER");
                        acciones.add("RECHAZAR");
                    }
                }
            }
        } else if ("INSPECTOR_CAMPO".equals(rolNombre)) {
            if (queja.getInspector() != null && queja.getInspector().getUsuarioId().equals(usuarioActual.getUsuarioId())) {
                if ("EN INSPECCIÓN".equals(estado)) {
                    acciones.add("REGISTRAR_INFORME_INSPECCION");
                }
            }
        } else if ("ESPECIALISTA_TECNICO".equals(rolNombre)) {
            if (queja.getEspecialista() != null && queja.getEspecialista().getUsuarioId().equals(usuarioActual.getUsuarioId())) {
                if ("EN REPARACIÓN TÉCNICA".equals(estado)) {
                    acciones.add("REGISTRAR_INFORME_REPARACION");
                }
            }
        }

        return acciones;
    }

    /**
     * Construye el DTO con el detalle completo de la queja incluyendo evidencias,
     * informes previos, trazabilidad histórica y las acciones disponibles para el rol.
     */
    public QuejaDetalleCompletoDTO obtenerDetalleCompleto(Integer quejaId, Usuario usuarioActual, String rolNombre) {
        Queja queja = buscarQueja(quejaId);

        QuejaDetalleCompletoDTO dto = new QuejaDetalleCompletoDTO();
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());
        dto.setTipoRegistro(queja.getTipoRegistro());
        if (queja.getQuejaOrigen() != null) {
            dto.setCorrelativoOrigen(queja.getQuejaOrigen().getCorrelativo());
        }

        // Ciudadano
        if (queja.getCiudadano() != null) {
            Usuario c = queja.getCiudadano();
            dto.setCiudadanoId(c.getUsuarioId());
            dto.setCiudadanoNombre(c.getNombres() + " " + c.getApellidos());
            dto.setCiudadanoDpi(c.getDpi());
            dto.setCiudadanoTelefono(c.getTelefono());
            dto.setCiudadanoCorreo(c.getCorreo());
        }

        // Categoría y ubicación
        dto.setCategoriaId(queja.getCategoriaId());
        dto.setCategoria(catalogoService.obtenerNombreCategoria(queja.getCategoriaId()));
        dto.setSubcategoriaId(queja.getSubcategoriaId());
        dto.setSubcategoria(catalogoService.obtenerNombreSubcategoria(queja.getSubcategoriaId()));
        dto.setZona(queja.getZona());
        dto.setDireccionExacta(queja.getDireccionExacta());
        dto.setPuntoReferencia(queja.getPuntoReferencia());
        dto.setLatitud(queja.getLatitud());
        dto.setLongitud(queja.getLongitud());
        dto.setDescripcion(queja.getDescripcion());

        // Estados y prioridades
        dto.setEstadoActual(queja.getEstadoActual());
        dto.setPrioridadSugerida(queja.getPrioridadSugerida());
        dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());

        // Asignaciones
        if (queja.getFuncionario() != null) {
            dto.setFuncionarioId(queja.getFuncionario().getUsuarioId());
            dto.setFuncionarioNombre(queja.getFuncionario().getNombres() + " " + queja.getFuncionario().getApellidos());
        }
        if (queja.getInspector() != null) {
            dto.setInspectorId(queja.getInspector().getUsuarioId());
            dto.setInspectorNombre(queja.getInspector().getNombres() + " " + queja.getInspector().getApellidos());
        }
        if (queja.getEspecialista() != null) {
            dto.setEspecialistaId(queja.getEspecialista().getUsuarioId());
            dto.setEspecialistaNombre(queja.getEspecialista().getNombres() + " " + queja.getEspecialista().getApellidos());
        }
        if (queja.getDependenciaAsignada() != null) {
            dto.setDependenciaId(queja.getDependenciaAsignada().getDependenciaId());
            dto.setDependenciaNombre(queja.getDependenciaAsignada().getNombreDependencia());
        }

        dto.setFechaRegistro(queja.getFechaRegistro());
        dto.setFechaModificacion(queja.getFechaModificacion());
        dto.setFechaCierre(queja.getFechaCierre());
        dto.setMotivoRechazo(queja.getMotivoRechazo());

        // Evidencias
        List<EvidenciaDigital> evidencias = evidenciaRepository.findByQueja(queja);
        if (evidencias != null) {
            dto.setEvidencias(evidencias.stream().map(e -> {
                EvidenciaDTO evDto = new EvidenciaDTO();
                evDto.setEvidenciaId(e.getEvidenciaId());
                evDto.setUrlArchivo(e.getUrlArchivo());
                evDto.setNombreArchivo(e.getNombreArchivo());
                evDto.setFormato(e.getFormato());
                evDto.setFechaSubida(e.getFechaSubida());
                return evDto;
            }).collect(Collectors.toList()));
        } else {
            dto.setEvidencias(List.of());
        }

        // Informes previos
        List<InformeQueja> informes = informeRepository.findByQuejaOrderByFechaRegistroAsc(queja);
        if (informes != null) {
            dto.setInformes(informes.stream().map(inf -> {
                InformeDetalleDTO infDto = new InformeDetalleDTO();
                infDto.setInformeId(inf.getInformeId());
                infDto.setTipoInforme(inf.getTipoInforme());
                if (inf.getAutor() != null) {
                    infDto.setAutorNombre(inf.getAutor().getNombres() + " " + inf.getAutor().getApellidos());
                    if (inf.getAutor().getCredencial() != null && inf.getAutor().getCredencial().getRolUser() != null) {
                        infDto.setAutorRol(inf.getAutor().getCredencial().getRolUser().getNombreRol());
                    }
                }
                infDto.setProblemaVerificado(inf.getProblemaVerificado());
                infDto.setGravedad(inf.getGravedad());
                infDto.setRecursosSugeridos(inf.getRecursosSugeridos());
                infDto.setInstruccionesCuadrilla(inf.getInstruccionesCuadrilla());
                infDto.setMaterialesUtilizados(inf.getMaterialesUtilizados());
                infDto.setHorasTrabajadas(inf.getHorasTrabajadas());
                infDto.setFechaFinTrabajo(inf.getFechaFinTrabajo());
                infDto.setDictamenCalidad(inf.getDictamenCalidad());
                infDto.setDescripcion(inf.getDescripcion());
                infDto.setFechaRegistro(inf.getFechaRegistro());
                return infDto;
            }).collect(Collectors.toList()));
        } else {
            dto.setInformes(List.of());
        }

        // Historial
        List<HistorialEstadoQueja> historiales = historialRepository.findByQuejaOrderByFechaCambioAsc(queja);
        if (historiales != null) {
            dto.setHistorial(historiales.stream().map(h -> {
                HistorialDetalleDTO hDto = new HistorialDetalleDTO();
                hDto.setHistorialId(h.getHistorialId());
                hDto.setEstadoAnterior(h.getEstadoAnterior());
                hDto.setEstadoNuevo(h.getEstadoNuevo());
                if (h.getCambiadoPor() != null) {
                    hDto.setCambiadoPorNombre(h.getCambiadoPor().getNombres() + " " + h.getCambiadoPor().getApellidos());
                }
                hDto.setComentario(h.getComentario());
                hDto.setFechaCambio(h.getFechaCambio());
                return hDto;
            }).collect(Collectors.toList()));
        } else {
            dto.setHistorial(List.of());
        }

        // Acciones disponibles
        dto.setAccionesDisponibles(determinarAccionesDisponibles(queja, usuarioActual, rolNombre));

        return dto;
    }
}
