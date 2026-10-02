package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.MensajeResponse;
import com.muni.backend.quejas.dto.QuejaInspectorBandejaDTO;
import com.muni.backend.quejas.dto.RegistroInspeccionDTO;
import com.muni.backend.quejas.exception.AccesoDenegadoException;
import com.muni.backend.quejas.exception.EstadoInvalidoException;
import com.muni.backend.quejas.exception.QuejaNoEncontradaException;
import com.muni.backend.quejas.model.EvidenciaDigital;
import com.muni.backend.quejas.model.HistorialEstadoQueja;
import com.muni.backend.quejas.model.InformeQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.HistorialEstadoQuejaRepository;
import com.muni.backend.quejas.repository.InformeQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectorCampoService {

    private final QuejaRepository quejaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InformeQuejaRepository informeQuejaRepository;
    private final HistorialEstadoQuejaRepository historialRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;

    private Usuario resolverUsuarioActual(String username) {
        return usuarioRepository.findByCorreo(username)
                .or(() -> usuarioRepository.findByCredencial_Username(username))
                .orElseThrow(() -> new AccesoDenegadoException(
                        "No se encontró el perfil de usuario registrado para: " + username));
    }

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

    /**
     * GET /api/inspector/asignaciones
     * Listar asignaciones activas para el inspector autenticado.
     */
    @Transactional(readOnly = true)
    public List<QuejaInspectorBandejaDTO> listarMisAsignaciones(String username) {
        Usuario inspector = resolverUsuarioActual(username);

        List<Queja> quejas = quejaRepository.findTareasActivasInspector(inspector.getUsuarioId());

        return quejas.stream().map(queja -> {
            QuejaInspectorBandejaDTO dto = new QuejaInspectorBandejaDTO();
            dto.setQuejaId(queja.getQuejaId());
            dto.setCorrelativo(queja.getCorrelativo());
            dto.setCategoria(obtenerNombreCategoria(queja.getCategoriaId()));
            dto.setSubcategoria(obtenerNombreSubcategoria(queja.getSubcategoriaId()));
            dto.setDireccionExacta(queja.getDireccionExacta());
            dto.setPuntoReferencia(queja.getPuntoReferencia());
            dto.setLatitud(queja.getLatitud());
            dto.setLongitud(queja.getLongitud());
            dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());
            dto.setFechaRegistro(queja.getFechaRegistro());
            dto.setTipoRegistro(queja.getTipoRegistro());
            dto.setQuejaOrigenId(queja.getQuejaOrigen() != null ? queja.getQuejaOrigen().getQuejaId() : null);

            List<EvidenciaDigital> evidenciasCiudadano = evidenciaRepository
                    .findByQueja_QuejaIdAndEtapa(queja.getQuejaId(), "REPORTE_CIUDADANO");

            if (evidenciasCiudadano == null || evidenciasCiudadano.isEmpty()) {
                evidenciasCiudadano = evidenciaRepository.findByQueja(queja);
            }

            List<String> fotosUrls = evidenciasCiudadano.stream()
                    .map(EvidenciaDigital::getUrlArchivo)
                    .collect(Collectors.toList());
            dto.setFotosCiudadano(fotosUrls);

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * POST /api/inspector/quejas/{quejaId}/informe-inspeccion
     * Registrar el informe de inspección inicial in situ.
     */
    @Transactional
    public MensajeResponse registrarInspeccionInicial(Integer quejaId, RegistroInspeccionDTO dto, String username) {
        Usuario inspector = resolverUsuarioActual(username);

        // Validación 1: Rol del usuario
        String rol = inspector.getCredencial() != null && inspector.getCredencial().getRolUser() != null
                ? inspector.getCredencial().getRolUser().getNombreRol() : null;
        if (!"INSPECTOR_CAMPO".equals(rol)) {
            throw new AccesoDenegadoException("El usuario autenticado no tiene el rol de Inspector de Campo.");
        }

        // Validación 2: Queja existente y estado 'EN INSPECCIÓN'
        Queja queja = quejaRepository.findById(quejaId)
                .orElseThrow(() -> new QuejaNoEncontradaException("No se encontró la queja con ID: " + quejaId));

        if (!"EN INSPECCIÓN".equals(queja.getEstadoActual())) {
            throw new EstadoInvalidoException(
                    "La queja debe estar estrictamente en estado 'EN INSPECCIÓN'. Estado actual: " + queja.getEstadoActual());
        }

        // Validación 3: Queja asignada al inspector autenticado
        if (queja.getInspector() == null || !queja.getInspector().getUsuarioId().equals(inspector.getUsuarioId())) {
            throw new AccesoDenegadoException("La queja no está asignada al inspector autenticado.");
        }

        // Validación 4: No informe previo de tipo INSPECCION_INICIAL
        boolean yaExisteInforme = informeQuejaRepository.existsByQueja_QuejaIdAndTipoInforme(quejaId, "INSPECCION_INICIAL");
        if (yaExisteInforme) {
            throw new EstadoInvalidoException("Ya existe un informe de inspección inicial registrado para esta queja.");
        }

        // Validación 5: Detalle y Gravedad
        if (Boolean.TRUE.equals(dto.getProblemaVerificado())) {
            if (dto.getGravedad() == null || !List.of("LEVE", "MODERADA", "GRAVE", "CRITICA").contains(dto.getGravedad())) {
                throw new EstadoInvalidoException("Debe indicar una gravedad válida ('LEVE', 'MODERADA', 'GRAVE', 'CRITICA') cuando el problema es verificado.");
            }
        }

        if (dto.getFotos() == null || dto.getFotos().isEmpty() || dto.getFotos().size() > 3) {
            throw new EstadoInvalidoException("Debe adjuntar entre 1 y 3 fotografías de evidencia.");
        }

        // Guardar informe en informes_queja
        InformeQueja informe = new InformeQueja();
        informe.setQueja(queja);
        informe.setTipoInforme("INSPECCION_INICIAL");
        informe.setAutor(inspector);
        informe.setProblemaVerificado(dto.getProblemaVerificado());
        informe.setGravedad(dto.getGravedad());
        informe.setDescripcion(dto.getDiagnostico().trim());
        informe.setRecursosSugeridos(dto.getRecursosSugeridos());
        informe.setInstruccionesCuadrilla(dto.getInstruccionesCuadrilla());
        InformeQueja informeGuardado = informeQuejaRepository.save(informe);

        // Guardar evidencias digitales
        for (String urlFoto : dto.getFotos()) {
            EvidenciaDigital evidencia = new EvidenciaDigital();
            evidencia.setQueja(queja);
            evidencia.setInforme(informeGuardado);
            evidencia.setEtapa("INSPECCION_INICIAL");
            evidencia.setUrlArchivo(urlFoto);
            evidencia.setSubidoPor(inspector);
            evidenciaRepository.save(evidencia);
        }

        // Actualizar estado de la queja
        String estadoAnterior = queja.getEstadoActual();
        queja.setEstadoActual("EN VALIDACIÓN DE REPARACIÓN");
        queja.setFechaModificacion(LocalDateTime.now());
        quejaRepository.save(queja);

        // Guardar historial
        HistorialEstadoQueja historial = new HistorialEstadoQueja();
        historial.setQueja(queja);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo("EN VALIDACIÓN DE REPARACIÓN");
        historial.setCambiadoPor(inspector);

        String comentario = Boolean.TRUE.equals(dto.getProblemaVerificado())
                ? "Inspección completada. Gravedad: " + dto.getGravedad()
                : "Inspección descartada en sitio (falsa alarma)";
        historial.setComentario(comentario);
        historialRepository.save(historial);

        return new MensajeResponse(
                "Informe de inspección registrado exitosamente para la queja " + queja.getCorrelativo(),
                queja.getCorrelativo(),
                "EN VALIDACIÓN DE REPARACIÓN"
        );
    }
}
