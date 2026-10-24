package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.especialista.InformeReparacionDTO;
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
public class EspecialistaService {

    private final QuejaRepository quejaRepository;
    private final InformeQuejaRepository informeQuejaRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;
    private final FileStorageService fileStorageService;
    private final QuejaEstadoService estadoService;
    private final CatalogoService catalogoService;
    private final AsignacionAutomaticaService asignacionAutomaticaService;

    /**
     * Bandeja de quejas asignadas al especialista en estado EN REPARACIÓN TÉCNICA (resumen simple para lista).
     */
    @Transactional(readOnly = true)
    public List<QuejaResumenDTO> obtenerMisQuejas(String username) {
        Usuario especialista = estadoService.resolverUsuario(username);
        List<Queja> quejas = quejaRepository.findMisQuejasEspecialista(especialista.getUsuarioId());

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
     * Detalle completo de una queja para visualización del especialista y acciones dinámicas.
     */
    @Transactional(readOnly = true)
    public QuejaDetalleCompletoDTO obtenerDetalleQueja(Integer quejaId, String username) {
        Usuario especialista = estadoService.resolverUsuario(username);
        return estadoService.obtenerDetalleCompleto(quejaId, especialista, "ESPECIALISTA_TECNICO");
    }

    /**
     * Registra informe de reparación y avanza la queja a PENDIENTE DE CIERRE.
     * Transición: EN REPARACIÓN TÉCNICA → PENDIENTE DE CIERRE
     * Tipo informe: SOLUCION_TECNICA
     */
    @Transactional
    public MensajeResponse registrarInformeReparacion(Integer quejaId,
                                                      InformeReparacionDTO dto,
                                                      List<MultipartFile> fotos,
                                                      String username) {
        Usuario especialista = estadoService.resolverUsuario(username);
        Queja queja = estadoService.buscarQueja(quejaId);
        estadoService.validarEstado(queja, EstadoQueja.EN_REPARACION_TECNICA);

        if (queja.getEspecialista() == null ||
                !queja.getEspecialista().getUsuarioId().equals(especialista.getUsuarioId())) {
            throw new AccesoDenegadoException(
                "Solo el especialista asignado puede registrar el informe de reparación.");
        }

        InformeQueja informe = new InformeQueja();
        informe.setQueja(queja);
        informe.setTipoInforme(TipoInforme.SOLUCION_TECNICA.name());
        informe.setAutor(especialista);
        informe.setDescripcion(dto.getDescripcion().trim());
        informe.setMaterialesUtilizados(dto.getMaterialesUtilizados());
        informe.setHorasTrabajadas(dto.getHorasTrabajadas());
        informe.setInstruccionesCuadrilla(dto.getInstruccionesCuadrilla());
        informe.setFechaFinTrabajo(LocalDateTime.now());
        InformeQueja informeGuardado = informeQuejaRepository.save(informe);

        if (fotos != null && !fotos.isEmpty()) {
            for (MultipartFile foto : fotos) {
                if (foto != null && !foto.isEmpty()) {
                    String url = fileStorageService.guardarArchivo(foto);
                    EvidenciaDigital evidencia = new EvidenciaDigital();
                    evidencia.setQueja(queja);
                    evidencia.setInforme(informeGuardado);
                    evidencia.setEtapa(EtapaEvidencia.SOLUCION_TECNICA.name());
                    evidencia.setUrlArchivo(url);
                    evidencia.setNombreArchivo(foto.getOriginalFilename());
                    evidencia.setFormato(foto.getContentType() != null && foto.getContentType().contains("png") ? "png" : "jpg");
                    evidencia.setTamanioBytes(foto.getSize());
                    evidencia.setSubidoPor(especialista);
                    evidencia.setFechaSubida(LocalDateTime.now());
                    evidenciaRepository.save(evidencia);
                }
            }
        }

        // Reasignación automática de la queja al funcionario municipal con menor carga activa para cierre administrativo
        asignacionAutomaticaService.asignarFuncionarioAutomatico().ifPresent(queja::setFuncionario);

        String infoFuncionario = queja.getFuncionario() != null
                ? queja.getFuncionario().getNombres() + " " + queja.getFuncionario().getApellidos()
                : "Sin asignar";

        estadoService.cambiarEstado(queja, EstadoQueja.PENDIENTE_DE_CIERRE, especialista,
                "Reparación técnica completada. Materiales: " + dto.getMaterialesUtilizados()
                + ". Asignada automáticamente al funcionario: " + infoFuncionario + " para cierre administrativo.");

        return new MensajeResponse(
                "Informe de reparación registrado para queja " + queja.getCorrelativo(),
                queja.getCorrelativo(),
                EstadoQueja.PENDIENTE_DE_CIERRE.getValor()
        );
    }
}
