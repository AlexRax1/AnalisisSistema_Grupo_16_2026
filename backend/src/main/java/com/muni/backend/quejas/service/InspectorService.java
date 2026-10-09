package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.inspector.InformeInspeccionDTO;
import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.exception.AccesoDenegadoException;
import com.muni.backend.quejas.model.*;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.InformeQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.shared.service.FileStorageService;
import com.muni.backend.usuarios.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectorService {

    private final QuejaRepository quejaRepository;
    private final InformeQuejaRepository informeQuejaRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;
    private final FileStorageService fileStorageService;
    private final QuejaEstadoService estadoService;
    private final CatalogoService catalogoService;
    private final AsignacionAutomaticaService asignacionAutomaticaService;

    /**
     * Bandeja de quejas asignadas al inspector en estado EN INSPECCIÓN (resumen simple para lista).
     */
    @Transactional(readOnly = true)
    public List<QuejaResumenDTO> obtenerMisQuejas(String username) {
        Usuario inspector = estadoService.resolverUsuario(username);
        List<Queja> quejas = quejaRepository.findMisQuejasInspector(inspector.getUsuarioId());

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
     * Detalle completo de una queja para visualización del inspector y acciones dinámicas.
     */
    @Transactional(readOnly = true)
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(Integer quejaId, String username) {
        Usuario inspector = estadoService.resolverUsuario(username);
        return estadoService.obtenerDetalleCompleto(quejaId, inspector, "INSPECTOR_CAMPO");
    }

    /**
     * Registra informe de inspección y avanza la queja a EN VALIDACIÓN DE REPARACIÓN.
     * Transición: EN INSPECCIÓN → EN VALIDACIÓN DE REPARACIÓN
     * Tipo informe: INSPECCION_INICIAL
     */
    @Transactional
    public MensajeResponse registrarInformeInspeccion(Integer quejaId,
                                                      InformeInspeccionDTO dto,
                                                      List<MultipartFile> fotos,
                                                      String username) {
        Usuario inspector = estadoService.resolverUsuario(username);
        Queja queja = estadoService.buscarQueja(quejaId);
        estadoService.validarEstado(queja, EstadoQueja.EN_INSPECCION);

        if (queja.getInspector() == null ||
                !queja.getInspector().getUsuarioId().equals(inspector.getUsuarioId())) {
            throw new AccesoDenegadoException(
                "Solo el inspector asignado puede registrar el informe de inspección.");
        }

        InformeQueja informe = new InformeQueja();
        informe.setQueja(queja);
        informe.setTipoInforme(TipoInforme.INSPECCION_INICIAL.name());
        informe.setAutor(inspector);
        informe.setProblemaVerificado(dto.getProblemaVerificado());
        informe.setGravedad(dto.getGravedad());
        informe.setRecursosSugeridos(dto.getRecursosSugeridos());
        informe.setDescripcion(dto.getDescripcion().trim());
        InformeQueja informeGuardado = informeQuejaRepository.save(informe);

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (foto != null && !foto.isEmpty()) {
                    String url = fileStorageService.guardarArchivo(foto);
                    EvidenciaDigital evidencia = new EvidenciaDigital();
                    evidencia.setQueja(queja);
                    evidencia.setInforme(informeGuardado);
                    evidencia.setEtapa(EtapaEvidencia.INSPECCION_INICIAL.name());
                    evidencia.setUrlArchivo(url);
                    evidencia.setNombreArchivo(foto.getOriginalFilename());
                    evidencia.setFormato(foto.getContentType() != null && foto.getContentType().contains("png") ? "png" : "jpg");
                    evidencia.setTamanioBytes(foto.getSize());
                    evidencia.setSubidoPor(inspector);
                    evidencia.setFechaSubida(LocalDateTime.now());
                    evidenciaRepository.save(evidencia);
                }
            }
        }

        // Reasignación automática de la queja al funcionario municipal con menor carga activa
        asignacionAutomaticaService.asignarFuncionarioAutomatico().ifPresent(queja::setFuncionario);

        String infoFuncionario = queja.getFuncionario() != null
                ? queja.getFuncionario().getNombres() + " " + queja.getFuncionario().getApellidos()
                : "Sin asignar";

        estadoService.cambiarEstado(queja, EstadoQueja.EN_VALIDACION_REPARACION, inspector,
                "Inspección completada (Problema verificado: " + dto.getProblemaVerificado()
                + ", Gravedad: " + dto.getGravedad() + "). Asignada automáticamente al funcionario: "
                + infoFuncionario);

        return new MensajeResponse(
                "Informe de inspección registrado para queja " + queja.getCorrelativo(),
                queja.getCorrelativo(),
                EstadoQueja.EN_VALIDACION_REPARACION.getValor()
        );
    }
}
