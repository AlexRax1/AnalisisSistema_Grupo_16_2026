package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.*;
import com.muni.backend.quejas.exception.*;
import com.muni.backend.quejas.model.*;
import com.muni.backend.quejas.repository.*;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionMunicipalService {

    private final QuejaRepository quejaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DependenciaRepository dependenciaRepository;
    private final InformeQuejaRepository informeQuejaRepository;
    private final HistorialEstadoQuejaRepository historialRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;

    private String obtenerNombreCategoria(Integer categoriaId) {
        if (categoriaId == null) return null;
        return switch (categoriaId) {
            case 1 -> "Alumbrado Público";
            case 2 -> "Drenajes y Alcantarillado";
            case 3 -> "Vialidad y Espacios Públicos";
            case 4 -> "Limpieza y Áreas Verdes";
            default -> "Categoría " + categoriaId;
        };
    }

    private String obtenerNombreSubcategoria(Integer subcategoriaId) {
        if (subcategoriaId == null) return null;
        return switch (subcategoriaId) {
            case 1 -> "Luminaria apagada / quemada";
            case 2 -> "Poste inclinado o dañado";
            case 3 -> "Cableado expuesto";
            case 4 -> "Tragante obstruido";
            case 5 -> "Inundación por drenaje";
            case 6 -> "Falta de tapadera de alcantarilla";
            case 7 -> "Mal olor proveniente de drenaje";
            case 8 -> "Bache en calle";
            case 9 -> "Señalización vial dañada";
            case 10 -> "Banqueta dañada";
            case 11 -> "Obstáculo en vía pública";
            case 12 -> "Acumulación de basura en vía pública";
            case 13 -> "Basurero clandestino";
            case 14 -> "Problema con recolección de residuos";
            case 15 -> "Árbol con riesgo de caída";
            default -> "Subcategoría " + subcategoriaId;
        };
    }

    // ==========================================
    // MÉTODO AUXILIAR: Resolver el Usuario actual
    // ==========================================

    /**
     * Dado el username (correo) extraído de Authentication.getName(),
     * busca el Usuario correspondiente en la tabla usuarios.
     */
    private Usuario resolverUsuarioActual(String username) {
        return usuarioRepository.findByCorreo(username)
                .or(() -> usuarioRepository.findByCredencial_Username(username))
                .orElseThrow(() -> new AccesoDenegadoException(
                    "No se encontró el perfil de usuario registrado para: " + username));
    }

    // ==========================================
    // MÉTODO AUXILIAR: Registrar Historial
    // ==========================================

    private void registrarHistorial(Queja queja, String estadoAnterior, String estadoNuevo,
                                     Usuario cambiadoPor, String comentario) {
        HistorialEstadoQueja historial = new HistorialEstadoQueja();
        historial.setQueja(queja);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setCambiadoPor(cambiadoPor);
        historial.setComentario(comentario);
        historialRepository.save(historial);
    }

    // ==========================================
    // MÉTODO AUXILIAR: Determinar fase requerida
    // ==========================================

    private String determinarFase(String estadoActual) {
        return switch (estadoActual) {
            case "REGISTRADA" -> "ASIGNAR_INSPECTOR";
            case "EN VALIDACIÓN DE REPARACIÓN" -> "AUTORIZAR_REPARACION";
            case "PENDIENTE DE CIERRE" -> "CIERRE_ADMINISTRATIVO";
            default -> "DESCONOCIDA";
        };
    }

    // ==========================================
    // ENDPOINT 1: Asignar Siguiente Tarea (Pull)
    // ==========================================

    /**
     * POST /quejas/tareas/asignar-siguiente
     *
     * Busca la queja más urgente y antigua en los 3 estados operativos.
     * Aplica bloqueo pesimista con SKIP LOCKED para evitar concurrencia.
     * Asigna el funcionario_id al usuario autenticado.
     */
    @Transactional
    public TareaAsignadaDTO asignarSiguienteTarea(String username) {
        Usuario funcionario = resolverUsuarioActual(username);

        // Buscar exactamente 1 resultado (LIMIT 1) con PageRequest.of(0, 1)
        List<Queja> resultados = quejaRepository.buscarSiguienteTarea(
                funcionario.getUsuarioId(),
                PageRequest.of(0, 1)
        );

        if (resultados.isEmpty()) {
            throw new SinTareasDisponiblesException(
                "No hay tareas pendientes de gestión en este momento.");
        }

        Queja queja = resultados.get(0);

        // Asignar funcionario si aún no tiene
        if (queja.getFuncionario() == null) {
            queja.setFuncionario(funcionario);
            queja.setFechaModificacion(LocalDateTime.now());
            quejaRepository.save(queja);
        }

        // Construir DTO de respuesta
        TareaAsignadaDTO dto = new TareaAsignadaDTO();
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());
        dto.setEstadoActual(queja.getEstadoActual());
        dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());
        dto.setCategoriaId(queja.getCategoriaId());
        dto.setCategoria(obtenerNombreCategoria(queja.getCategoriaId()));
        dto.setSubcategoriaId(queja.getSubcategoriaId());
        dto.setSubcategoria(obtenerNombreSubcategoria(queja.getSubcategoriaId()));
        dto.setZona(queja.getZona());
        dto.setDireccionExacta(queja.getDireccionExacta());
        dto.setPuntoReferencia(queja.getPuntoReferencia());
        dto.setDescripcion(queja.getDescripcion());
        dto.setLatitud(queja.getLatitud());
        dto.setLongitud(queja.getLongitud());
        dto.setFechaRegistro(queja.getFechaRegistro());
        dto.setFaseRequerida(determinarFase(queja.getEstadoActual()));
        dto.setFaseAdministrativa(dto.getFaseRequerida());

        if (queja.getCiudadano() != null) {
            dto.setCiudadanoNombre(queja.getCiudadano().getNombres() + " " + queja.getCiudadano().getApellidos());
        }

        // Cargar evidencias y fotografías
        List<EvidenciaDigital> evidencias = evidenciaRepository.findByQueja(queja);
        if (evidencias != null && !evidencias.isEmpty()) {
            List<String> fotosUrls = evidencias.stream()
                    .map(EvidenciaDigital::getUrlArchivo)
                    .collect(Collectors.toList());
            dto.setFotos(fotosUrls);

            List<EvidenciaDTO> evidenciasDTO = evidencias.stream().map(e -> {
                EvidenciaDTO evDto = new EvidenciaDTO();
                evDto.setUrlArchivo(e.getUrlArchivo());
                evDto.setNombreArchivo(e.getNombreArchivo());
                return evDto;
            }).collect(Collectors.toList());
            dto.setEvidencias(evidenciasDTO);
        } else {
            dto.setFotos(List.of());
            dto.setEvidencias(List.of());
        }

        dto.setMensaje("Tarea asignada exitosamente. Proceda con la fase: "
                        + dto.getFaseRequerida());

        return dto;
    }

    // ==========================================
    // ENDPOINT 2: Asignar Inspector de Campo
    // ==========================================

    /**
     * POST /quejas/{quejaId}/fase-inicial/asignar-inspector
     */
    @Transactional
    public MensajeResponse asignarInspector(Integer quejaId, AsignarInspectorDTO dto, String username) {
        Usuario funcionario = resolverUsuarioActual(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = quejaRepository.findById(targetQuejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la queja con ID: " + targetQuejaId));

        // Validar estado
        if (!"REGISTRADA".equals(queja.getEstadoActual())) {
            throw new EstadoInvalidoException(
                "La queja no está en estado 'REGISTRADA'. Estado actual: " + queja.getEstadoActual());
        }

        if (dto == null || dto.getInspectorId() == null) {
            throw new EstadoInvalidoException("Debe seleccionar un inspector de campo.");
        }

        // Validar que el inspector exista (buscando por usuario_id o por credencial.user_id)
        Usuario inspector = usuarioRepository.findById(dto.getInspectorId())
                .or(() -> usuarioRepository.findByCredencial_UserId(dto.getInspectorId()))
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró el inspector con ID: " + dto.getInspectorId()));

        String rolInspector = inspector.getCredencial().getRolUser().getNombreRol();
        if (!"INSPECTOR_CAMPO".equals(rolInspector)) {
            throw new EstadoInvalidoException(
                "El usuario seleccionado no tiene el rol de Inspector de Campo.");
        }

        // Actualizar la queja
        String estadoAnterior = queja.getEstadoActual();
        queja.setInspector(inspector);
        queja.setEstadoActual("EN INSPECCIÓN");
        queja.setFechaModificacion(LocalDateTime.now());
        quejaRepository.save(queja);

        // Registrar historial
        String comentario = dto.getInstrucciones() != null
                ? "Inspector asignado. Instrucciones: " + dto.getInstrucciones()
                : "Inspector asignado.";
        registrarHistorial(queja, estadoAnterior, "EN INSPECCIÓN", funcionario, comentario);

        return new MensajeResponse(
                "Inspector asignado exitosamente a la queja " + queja.getCorrelativo(),
                queja.getCorrelativo(),
                "EN INSPECCIÓN"
        );
    }

    // ==========================================
    // ENDPOINT 3: Autorizar Reparación
    // ==========================================

    /**
     * POST /quejas/{quejaId}/fase-aprobacion/autorizar-reparacion
     */
    @Transactional
    public MensajeResponse autorizarReparacion(Integer quejaId, AutorizarReparacionDTO dto, String username) {
        Usuario funcionario = resolverUsuarioActual(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = quejaRepository.findById(targetQuejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la queja con ID: " + targetQuejaId));

        // Validar estado
        if (!"EN VALIDACIÓN DE REPARACIÓN".equals(queja.getEstadoActual())) {
            throw new EstadoInvalidoException(
                "La queja no está en estado 'EN VALIDACIÓN DE REPARACIÓN'. Estado actual: "
                + queja.getEstadoActual());
        }

        if (dto == null || dto.getEspecialistaId() == null) {
            throw new EstadoInvalidoException("Debe seleccionar un especialista técnico.");
        }

        // Validar especialista
        Usuario especialista = usuarioRepository.findById(dto.getEspecialistaId())
                .or(() -> usuarioRepository.findByCredencial_UserId(dto.getEspecialistaId()))
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró el especialista con ID: " + dto.getEspecialistaId()));

        String rolEspecialista = especialista.getCredencial().getRolUser().getNombreRol();
        if (!"ESPECIALISTA_TECNICO".equals(rolEspecialista)) {
            throw new EstadoInvalidoException(
                "El usuario seleccionado no tiene el rol de Especialista Técnico.");
        }

        Integer depId = dto.getDependenciaEfectivaId();
        if (depId == null) {
            throw new EstadoInvalidoException("Debe seleccionar una dependencia municipal.");
        }

        // Validar dependencia
        Dependencia dependencia = dependenciaRepository.findById(depId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la dependencia con ID: " + depId));

        // Actualizar la queja
        String estadoAnterior = queja.getEstadoActual();
        queja.setEspecialista(especialista);
        queja.setDependenciaAsignada(dependencia);
        queja.setEstadoActual("EN REPARACIÓN TÉCNICA");
        queja.setFechaModificacion(LocalDateTime.now());
        quejaRepository.save(queja);

        // Registrar historial
        String comentario = dto.getInstrucciones() != null
                ? "Reparación autorizada. Dependencia: " + dependencia.getNombreDependencia()
                  + ". Instrucciones: " + dto.getInstrucciones()
                : "Reparación autorizada. Dependencia: " + dependencia.getNombreDependencia();
        registrarHistorial(queja, estadoAnterior, "EN REPARACIÓN TÉCNICA", funcionario, comentario);

        return new MensajeResponse(
                "Reparación autorizada para queja " + queja.getCorrelativo()
                + ". Dependencia asignada: " + dependencia.getNombreDependencia(),
                queja.getCorrelativo(),
                "EN REPARACIÓN TÉCNICA"
        );
    }

    // ==========================================
    // ENDPOINT 4: Cierre Administrativo
    // ==========================================

    /**
     * POST /quejas/{quejaId}/fase-cierre/aprobar
     */
    @Transactional
    public MensajeResponse cierreAdministrativo(Integer quejaId, CierreAdministrativoDTO dto, String username) {
        Usuario funcionario = resolverUsuarioActual(username);

        Queja queja = quejaRepository.findById(quejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la queja con ID: " + quejaId));

        // Validar estado
        if (!"PENDIENTE DE CIERRE".equals(queja.getEstadoActual())) {
            throw new EstadoInvalidoException(
                "La queja no está en estado 'PENDIENTE DE CIERRE'. Estado actual: "
                + queja.getEstadoActual());
        }

        // Validar longitud mínima de descripción (doble validación: DTO + servicio)
        if (dto.getDescripcionCierre() == null || dto.getDescripcionCierre().trim().length() < 20) {
            throw new EstadoInvalidoException(
                "La descripción de cierre debe tener al menos 20 caracteres.");
        }

        // Actualizar la queja
        String estadoAnterior = queja.getEstadoActual();
        queja.setEstadoActual("SOLUCIONADA / CERRADA");
        queja.setFechaCierre(LocalDateTime.now());
        queja.setFechaModificacion(LocalDateTime.now());
        quejaRepository.save(queja);

        // Crear informe de cierre administrativo
        InformeQueja informe = new InformeQueja();
        informe.setQueja(queja);
        informe.setTipoInforme("CIERRE_ADMINISTRATIVO");
        informe.setAutor(funcionario);
        informe.setDescripcion(dto.getDescripcionCierre().trim());
        informeQuejaRepository.save(informe);

        // Registrar historial
        registrarHistorial(queja, estadoAnterior, "SOLUCIONADA / CERRADA", funcionario,
                "Cierre administrativo aprobado.");

        return new MensajeResponse(
                "La queja " + queja.getCorrelativo() + " ha sido cerrada exitosamente.",
                queja.getCorrelativo(),
                "SOLUCIONADA / CERRADA"
        );
    }

    // ==========================================
    // ENDPOINT 5: Rechazar o Devolver
    // ==========================================

    /**
     * POST /quejas/{quejaId}/rechazar-devolver
     */
    @Transactional
    public MensajeResponse rechazarODevolver(Integer quejaId, RechazoDevolucionDTO dto, String username) {
        Usuario funcionario = resolverUsuarioActual(username);

        Queja queja = quejaRepository.findById(quejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la queja con ID: " + quejaId));

        String estadoAnterior = queja.getEstadoActual();

        // No se pueden rechazar/devolver quejas ya cerradas o rechazadas
        if ("SOLUCIONADA / CERRADA".equals(estadoAnterior) || "RECHAZADA".equals(estadoAnterior)) {
            throw new EstadoInvalidoException(
                "No se puede rechazar o devolver una queja en estado: " + estadoAnterior);
        }

        if (Boolean.TRUE.equals(dto.getEsRechazoDefinitivo())) {
            // RECHAZO DEFINITIVO
            queja.setEstadoActual("RECHAZADA");
            queja.setMotivoRechazo(dto.getMotivoRechazo());
            queja.setFechaCierre(LocalDateTime.now());
            queja.setFechaModificacion(LocalDateTime.now());
            quejaRepository.save(queja);

            registrarHistorial(queja, estadoAnterior, "RECHAZADA", funcionario,
                    "Rechazo definitivo. Motivo: " + dto.getMotivoRechazo()
                    + ". Detalle: " + dto.getDescripcionDetallada());

            return new MensajeResponse(
                    "La queja " + queja.getCorrelativo() + " ha sido rechazada definitivamente.",
                    queja.getCorrelativo(),
                    "RECHAZADA"
            );
        } else {
            // DEVOLUCIÓN (retroceso de estado)
            String estadoDestino = determinarEstadoAnterior(estadoAnterior);

            queja.setEstadoActual(estadoDestino);
            queja.setFechaModificacion(LocalDateTime.now());
            quejaRepository.save(queja);

            registrarHistorial(queja, estadoAnterior, estadoDestino, funcionario,
                    "Devolución. Motivo: " + dto.getMotivoRechazo()
                    + ". Detalle: " + dto.getDescripcionDetallada());

            return new MensajeResponse(
                    "La queja " + queja.getCorrelativo() + " ha sido devuelta al estado: " + estadoDestino,
                    queja.getCorrelativo(),
                    estadoDestino
            );
        }
    }

    /**
     * Mapeo inverso de estados para devolución.
     */
    private String determinarEstadoAnterior(String estadoActual) {
        return switch (estadoActual) {
            case "EN INSPECCIÓN" -> "REGISTRADA";
            case "EN VALIDACIÓN DE REPARACIÓN" -> "EN INSPECCIÓN";
            case "EN REPARACIÓN TÉCNICA" -> "EN VALIDACIÓN DE REPARACIÓN";
            case "PENDIENTE DE CIERRE" -> "EN REPARACIÓN TÉCNICA";
            case "REGISTRADA" -> throw new EstadoInvalidoException(
                    "No se puede devolver una queja en estado 'REGISTRADA'. Use rechazo definitivo.");
            default -> throw new EstadoInvalidoException(
                    "Estado no soportado para devolución: " + estadoActual);
        };
    }
}
