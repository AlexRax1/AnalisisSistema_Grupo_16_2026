package com.muni.backend.quejas.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.quejas.dto.funcionario.*;
import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.exception.*;
import com.muni.backend.quejas.model.*;
import com.muni.backend.quejas.repository.*;
import com.muni.backend.usuarios.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final QuejaRepository quejaRepository;
    private final DependenciaRepository dependenciaRepository;
    private final InformeQuejaRepository informeQuejaRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;
    private final QuejaEstadoService estadoService;
    private final CatalogoService catalogoService;
    private final AsignacionAutomaticaService asignacionAutomaticaService;
    private final BitacoraService bitacoraService;

    private String nombreUsuario(Usuario usuario) {
        return usuario != null
                ? usuario.getNombres() + " " + usuario.getApellidos()
                    + " (id " + usuario.getUsuarioId() + ")"
                : "Sin asignar";
    }

    private String determinarFase(String estadoActual) {
        return switch (estadoActual) {
            case "REGISTRADA" -> "ASIGNAR_INSPECTOR";
            case "EN VALIDACIÓN DE REPARACIÓN" -> "AUTORIZAR_REPARACION";
            case "PENDIENTE DE CIERRE" -> "CIERRE_ADMINISTRATIVO";
            default -> "DESCONOCIDA";
        };
    }

    /**
     * Lista todas las dependencias municipales activas (para dropdowns de asignación).
     */
    @Transactional(readOnly = true)
    public List<Dependencia> listarDependenciasActivas() {
        return dependenciaRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActivo()))
                .collect(Collectors.toList());
    }

    /**
     * Bandeja de quejas asignadas al funcionario (resumen simple para lista).
     */
    @Transactional(readOnly = true)
    public List<QuejaResumenDTO> obtenerMisQuejas(String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);
        List<Queja> quejas = quejaRepository.findMisQuejasFuncionario(funcionario.getUsuarioId());

        return quejas.stream().map(q -> {
            QuejaResumenDTO dto = new QuejaResumenDTO();
            dto.setQuejaId(q.getQuejaId());
            dto.setCorrelativo(q.getCorrelativo());
            dto.setTipoRegistro(q.getTipoRegistro());
            dto.setCategoria(catalogoService.obtenerNombreCategoria(q.getCategoriaId()));
            dto.setSubcategoria(catalogoService.obtenerNombreSubcategoria(q.getSubcategoriaId()));
            dto.setZona(q.getZona());
            dto.setDireccionExacta(q.getDireccionExacta());
            dto.setEstadoActual(q.getEstadoActual());
            dto.setPrioridadConfirmada(q.getPrioridadConfirmada());
            dto.setFechaRegistro(q.getFechaRegistro());
            if (q.getCiudadano() != null) {
                dto.setCiudadanoNombre(q.getCiudadano().getNombres() + " " + q.getCiudadano().getApellidos());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Detalle completo de una queja para visualización y acciones dinámicas.
     */
    @Transactional(readOnly = true)
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(Integer quejaId, String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);
        return estadoService.obtenerDetalleCompleto(quejaId, funcionario, "FUNCIONARIO_MUNICIPAL");
    }

    /**
     * Pull por demanda - Asignar siguiente tarea (más prioritaria y antigua).
     */
    @Transactional
    public TareaAsignadaDTO asignarSiguienteTarea(String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);

        List<Queja> resultados = quejaRepository.buscarSiguienteTarea(
                funcionario.getUsuarioId(),
                PageRequest.of(0, 1)
        );

        if (resultados.isEmpty()) {
            throw new SinTareasDisponiblesException(
                "No hay tareas pendientes de gestión en este momento.");
        }

        Queja queja = resultados.get(0);

        if (queja.getFuncionario() == null) {
            queja.setFuncionario(funcionario);
            queja.setFechaModificacion(LocalDateTime.now());
            quejaRepository.save(queja);

            bitacoraService.registrar(
                    username,
                    "quejas",
                    String.valueOf(queja.getQuejaId()),
                    "funcionario_id",
                    "ACTUALIZAR",
                    "Sin asignar",
                    nombreUsuario(funcionario)
            );
        }

        TareaAsignadaDTO dto = new TareaAsignadaDTO();
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());
        dto.setEstadoActual(queja.getEstadoActual());
        dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());
        dto.setCategoriaId(queja.getCategoriaId());
        dto.setCategoria(catalogoService.obtenerNombreCategoria(queja.getCategoriaId()));
        dto.setSubcategoriaId(queja.getSubcategoriaId());
        dto.setSubcategoria(catalogoService.obtenerNombreSubcategoria(queja.getSubcategoriaId()));
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

        List<EvidenciaDigital> evidencias = evidenciaRepository.findByQueja(queja);
        if (evidencias != null && !evidencias.isEmpty()) {
            List<String> fotosUrls = evidencias.stream()
                    .map(EvidenciaDigital::getUrlArchivo)
                    .collect(Collectors.toList());
            dto.setFotos(fotosUrls);

            List<EvidenciaDTO> evidenciasDTO = evidencias.stream().map(e -> {
                EvidenciaDTO evDto = new EvidenciaDTO();
                evDto.setEvidenciaId(e.getEvidenciaId());
                evDto.setUrlArchivo(e.getUrlArchivo());
                evDto.setNombreArchivo(e.getNombreArchivo());
                evDto.setFormato(e.getFormato());
                evDto.setFechaSubida(e.getFechaSubida());
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

    /**
     * Fase 1: Asignar inspector de campo de forma AUTOMÁTICA (menor carga activa).
     */
    @Transactional
    public MensajeResponse asignarInspector(Integer quejaId, AsignarInspectorDTO dto, String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = estadoService.buscarQueja(targetQuejaId);
        estadoService.validarEstado(queja, EstadoQueja.REGISTRADA);

        // Asignación automática por carga de trabajo
        Usuario inspector = asignacionAutomaticaService.asignarInspectorAutomatico();
        String inspectorAnterior = nombreUsuario(queja.getInspector());
        queja.setInspector(inspector);

        String instrucciones = dto != null ? dto.getInstrucciones() : null;
        String comentario = instrucciones != null && !instrucciones.trim().isEmpty()
                ? "Inspector " + inspector.getNombres() + " " + inspector.getApellidos()
                    + " asignado automáticamente. Instrucciones: " + instrucciones.trim()
                : "Inspector " + inspector.getNombres() + " " + inspector.getApellidos()
                    + " asignado automáticamente.";
        estadoService.cambiarEstado(queja, EstadoQueja.EN_INSPECCION, funcionario, comentario);

        bitacoraService.registrar(
                username,
                "quejas",
                String.valueOf(queja.getQuejaId()),
                "inspector_id",
                "ACTUALIZAR",
                inspectorAnterior,
                nombreUsuario(inspector)
        );

        return new MensajeResponse(
                "Inspector " + inspector.getNombres() + " " + inspector.getApellidos()
                        + " asignado automáticamente a la queja " + queja.getCorrelativo(),
                queja.getCorrelativo(),
                EstadoQueja.EN_INSPECCION.getValor()
        );
    }

    /**
     * Fase 2: Autorizar reparación con asignación AUTOMÁTICA de especialista de la dependencia seleccionada.
     */
    @Transactional
    public MensajeResponse autorizarReparacion(Integer quejaId, AutorizarReparacionDTO dto, String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = estadoService.buscarQueja(targetQuejaId);
        estadoService.validarEstado(queja, EstadoQueja.EN_VALIDACION_REPARACION);

        Integer depId = dto != null ? dto.getDependenciaEfectivaId() : null;
        if (depId == null) {
            throw new EstadoInvalidoException("Debe seleccionar una dependencia municipal.");
        }

        Dependencia dependencia = dependenciaRepository.findById(depId)
                .orElseThrow(() -> new QuejaNoEncontradaException(
                    "No se encontró la dependencia con ID: " + depId));

        // Asignación automática de especialista (priorizando dependencia y menor carga)
        Usuario especialista = asignacionAutomaticaService.asignarEspecialistaAutomatico(depId);

        String especialistaAnterior = nombreUsuario(queja.getEspecialista());
        String dependenciaAnterior = queja.getDependenciaAsignada() != null
                ? queja.getDependenciaAsignada().getNombreDependencia()
                : "Sin asignar";

        queja.setEspecialista(especialista);
        queja.setDependenciaAsignada(dependencia);

        String instrucciones = dto != null ? dto.getInstrucciones() : null;
        String comentario = instrucciones != null && !instrucciones.trim().isEmpty()
                ? "Reparación autorizada. Dependencia: " + dependencia.getNombreDependencia()
                    + ". Especialista: " + especialista.getNombres() + " " + especialista.getApellidos()
                    + ". Instrucciones: " + instrucciones.trim()
                : "Reparación autorizada. Dependencia: " + dependencia.getNombreDependencia()
                    + ". Especialista: " + especialista.getNombres() + " " + especialista.getApellidos();
        estadoService.cambiarEstado(queja, EstadoQueja.EN_REPARACION_TECNICA, funcionario, comentario);

        bitacoraService.registrar(
                username,
                "quejas",
                String.valueOf(queja.getQuejaId()),
                "especialista_id",
                "ACTUALIZAR",
                especialistaAnterior,
                nombreUsuario(especialista)
        );

        bitacoraService.registrar(
                username,
                "quejas",
                String.valueOf(queja.getQuejaId()),
                "dependencia_id",
                "ACTUALIZAR",
                dependenciaAnterior,
                dependencia.getNombreDependencia()
        );

        return new MensajeResponse(
                "Reparación autorizada para queja " + queja.getCorrelativo()
                + ". Especialista asignado: " + especialista.getNombres() + " " + especialista.getApellidos()
                + " (" + dependencia.getNombreDependencia() + ")",
                queja.getCorrelativo(),
                EstadoQueja.EN_REPARACION_TECNICA.getValor()
        );
    }

    /**
     * Fase 3: Cierre administrativo
     */
    @Transactional
    public MensajeResponse cierreAdministrativo(Integer quejaId, CierreAdministrativoDTO dto, String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = estadoService.buscarQueja(targetQuejaId);
        estadoService.validarEstado(queja, EstadoQueja.PENDIENTE_DE_CIERRE);

        if (dto == null) {
            throw new EstadoInvalidoException("Debe proporcionar la resolución de cierre.");
        }

        String resolucion = dto.getDescripcionCierre() != null
                ? dto.getDescripcionCierre().trim()
                : "";
        if (resolucion.length() < 20 || resolucion.length() > 500) {
            throw new EstadoInvalidoException(
                "La resolución de cierre debe tener entre 20 y 500 caracteres.");
        }

        queja.setFechaCierre(LocalDateTime.now());

        InformeQueja informe = new InformeQueja();
        informe.setQueja(queja);
        informe.setTipoInforme(TipoInforme.CIERRE_ADMINISTRATIVO.name());
        informe.setAutor(funcionario);
        informe.setDescripcion(resolucion);
        InformeQueja informeGuardado = informeQuejaRepository.save(informe);

        estadoService.cambiarEstado(queja, EstadoQueja.SOLUCIONADA_CERRADA, funcionario,
                "Cierre administrativo aprobado.");

        bitacoraService.registrar(
                username,
                "informes_queja",
                String.valueOf(informeGuardado.getInformeId()),
                "registro",
                "CREAR",
                null,
                "Cierre administrativo de la queja " + queja.getCorrelativo()
        );

        return new MensajeResponse(
                "La queja " + queja.getCorrelativo() + " ha sido cerrada exitosamente.",
                queja.getCorrelativo(),
                EstadoQueja.SOLUCIONADA_CERRADA.getValor()
        );
    }

    /**
     * Rechazar o Devolver queja
     */
    @Transactional
    public MensajeResponse rechazarODevolver(Integer quejaId, RechazoDevolucionDTO dto, String username) {
        Usuario funcionario = estadoService.resolverUsuario(username);

        Integer targetQuejaId = quejaId != null ? quejaId : (dto != null ? dto.getQuejaId() : null);
        if (targetQuejaId == null) {
            throw new EstadoInvalidoException("Debe proporcionar el ID de la queja.");
        }

        Queja queja = estadoService.buscarQueja(targetQuejaId);
        String estadoAnterior = queja.getEstadoActual();

        if (EstadoQueja.SOLUCIONADA_CERRADA.getValor().equals(estadoAnterior) ||
            EstadoQueja.RECHAZADA.getValor().equals(estadoAnterior)) {
            throw new EstadoInvalidoException(
                "No se puede rechazar o devolver una queja en estado: " + estadoAnterior);
        }

        if (dto == null) {
            throw new EstadoInvalidoException(
                "Debe proporcionar los datos del rechazo o devolución.");
        }
        if (dto.getEsRechazoDefinitivo() == null) {
            throw new EstadoInvalidoException(
                "Debe indicar si la acción es rechazo definitivo o devolución.");
        }

        String motivo = dto.getMotivoRechazo() != null ? dto.getMotivoRechazo().trim() : "";
        if (motivo.isEmpty()) {
            throw new EstadoInvalidoException("Debe seleccionar un motivo principal.");
        }

        String justificacion = dto.getDescripcionDetallada() != null
                ? dto.getDescripcionDetallada().trim()
                : "";
        if (justificacion.length() < 20 || justificacion.length() > 500) {
            throw new EstadoInvalidoException(
                "La justificación debe tener entre 20 y 500 caracteres.");
        }

        if (Boolean.TRUE.equals(dto.getEsRechazoDefinitivo())) {
            String motivoAnterior = queja.getMotivoRechazo();
            queja.setMotivoRechazo(motivo);
            queja.setFechaCierre(LocalDateTime.now());

            String comentario = "Rechazo definitivo. Motivo: " + motivo
                    + ". Detalle: " + justificacion;
            estadoService.cambiarEstado(queja, EstadoQueja.RECHAZADA, funcionario, comentario);

            bitacoraService.registrar(
                    username,
                    "quejas",
                    String.valueOf(queja.getQuejaId()),
                    "motivo_rechazo",
                    "ACTUALIZAR",
                    motivoAnterior,
                    motivo
            );

            return new MensajeResponse(
                    "La queja " + queja.getCorrelativo() + " ha sido rechazada definitivamente.",
                    queja.getCorrelativo(),
                    EstadoQueja.RECHAZADA.getValor()
            );
        } else {
            EstadoQueja estadoDestino = estadoService.determinarEstadoAnterior(estadoAnterior);

            String comentario = "Devolución. Motivo: " + motivo
                    + ". Detalle: " + justificacion;
            estadoService.cambiarEstado(queja, estadoDestino, funcionario, comentario);

            bitacoraService.registrar(
                    username,
                    "quejas",
                    String.valueOf(queja.getQuejaId()),
                    "motivo_devolucion",
                    "ACTUALIZAR",
                    null,
                    motivo
            );

            return new MensajeResponse(
                    "La queja " + queja.getCorrelativo() + " ha sido devuelta al estado: " + estadoDestino.getValor(),
                    queja.getCorrelativo(),
                    estadoDestino.getValor()
            );
        }
    }
}
