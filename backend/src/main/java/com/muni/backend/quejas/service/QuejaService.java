package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.ciudadano.*;
import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.model.EvidenciaDigital;
import com.muni.backend.quejas.model.HistorialEstadoQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.HistorialEstadoQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.shared.service.FileStorageService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuejaService {

    private final QuejaRepository quejaRepository;
    private final EvidenciaDigitalRepository evidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialEstadoQuejaRepository historialRepository;
    private final FileStorageService fileStorageService;
    private final AsignacionAutomaticaService asignacionAutomaticaService;

    // 1. Registro de una queja principal

    // Registra la queja, genera el correlativo y guarda las evidencias.
    @Transactional
    public QuejaRegistroResponse registrarQueja(QuejaRegistroDTO dto, List<MultipartFile> fotos, String correoCiudadano) {

        // Validaciones del formulario ciudadano antes de persistir.
        if (dto.getCategoriaId() == null || dto.getZona() == null ||
                dto.getDireccionExacta() == null || dto.getDescripcion() == null ||
                dto.getLatitud() == null || dto.getLongitud() == null) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }

        if (dto.getZona() < 1 || dto.getZona() > 25) {
            throw new IllegalArgumentException("La zona municipal debe estar entre 1 y 25.");
        }

        String desc = dto.getDescripcion().trim();
        if (desc.length() < 20 || desc.length() > 500) {
            throw new IllegalArgumentException("La descripción debe tener entre 20 y 500 caracteres.");
        }

        if (fotos == null || fotos.isEmpty() || fotos.get(0).isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar al menos una fotografía de evidencia.");
        }
        if (fotos.size() > 3) {
            throw new IllegalArgumentException("No puede adjuntar más de 3 fotografías.");
        }

        Usuario ciudadano = usuarioRepository.findByCorreo(correoCiudadano)
                .orElseThrow(() -> new IllegalArgumentException("Ciudadano no encontrado."));

        // La prioridad por demanda está deshabilitada; toda queja nueva entra como MEDIA.
        Queja queja = new Queja();
        queja.setCorrelativo("TEMP-" + System.currentTimeMillis());
        queja.setCiudadano(ciudadano);
        queja.setCategoriaId(dto.getCategoriaId());
        queja.setSubcategoriaId(dto.getSubcategoriaId());
        queja.setZona(dto.getZona());
        queja.setDireccionExacta(dto.getDireccionExacta().trim());
        queja.setPuntoReferencia(dto.getPuntoReferencia());
        queja.setLatitud(dto.getLatitud());
        queja.setLongitud(dto.getLongitud());
        queja.setDescripcion(desc);
        queja.setPrioridadSugerida("MEDIA");
        queja.setPrioridadConfirmada("MEDIA");

        asignacionAutomaticaService.asignarFuncionarioAutomatico().ifPresent(queja::setFuncionario);

        // El primer guardado genera el ID para construir el correlativo oficial.
        Queja guardada = quejaRepository.save(queja);

        String correlativo = String.format("QUE-%d-%06d", Year.now().getValue(), guardada.getQuejaId());
        guardada.setCorrelativo(correlativo);
        quejaRepository.save(guardada);

        // El historial conserva la trazabilidad desde el estado inicial.
        HistorialEstadoQueja historial = new HistorialEstadoQueja();
        historial.setQueja(guardada);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo("REGISTRADA");
        historial.setCambiadoPor(ciudadano);
        historial.setComentario(guardada.getFuncionario() != null
                ? "Queja registrada. Asignada automáticamente al funcionario: "
                    + guardada.getFuncionario().getNombres() + " " + guardada.getFuncionario().getApellidos()
                : "Queja registrada.");
        historialRepository.save(historial);

        // Las evidencias se asocian a la queja ya persistida.
        for (MultipartFile foto : fotos) {
            String url = fileStorageService.guardarArchivo(foto);

            EvidenciaDigital evidencia = new EvidenciaDigital();
            evidencia.setQueja(guardada);
            evidencia.setUrlArchivo(url);
            evidencia.setNombreArchivo(foto.getOriginalFilename());
            evidencia.setFormato(foto.getContentType() != null && foto.getContentType().contains("png") ? "png" : "jpg");
            evidencia.setTamanioBytes(foto.getSize());
            evidencia.setSubidoPor(ciudadano);

            evidenciaRepository.save(evidencia);
        }

        return new QuejaRegistroResponse(
                correlativo,
                "Su queja ha sido registrada exitosamente con el número " + correlativo + ". Puede consultar su estado en cualquier momento."
        );
    }

    // 2. Consulta de quejas del ciudadano

    // Historial del ciudadano, de la queja más reciente a la más antigua.
    @Transactional(readOnly = true)
    public List<QuejaDetalleDTO> obtenerMisQuejas(String correoCiudadano) {
        Usuario ciudadano = usuarioRepository.findByCorreo(correoCiudadano)
                .orElseThrow(() -> new IllegalArgumentException("Ciudadano no encontrado."));

        List<Queja> quejas = quejaRepository.findByCiudadano_UsuarioIdOrderByQuejaIdDesc(ciudadano.getUsuarioId());

        return quejas.stream()
                .map(this::convertirADetalleDTO)
                .collect(Collectors.toList());
    }

    // Busca una queja por su correlativo oficial.
    @Transactional(readOnly = true)
    public QuejaDetalleDTO obtenerPorCorrelativo(String correlativo) {
        Queja queja = quejaRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la queja con correlativo: " + correlativo));

        return convertirADetalleDTO(queja);
    }

    // 3. Mapeo de entidad a DTO de respuesta

    // Convierte la entidad Queja al DTO que consume el portal ciudadano.
    private QuejaDetalleDTO convertirADetalleDTO(Queja queja) {
        QuejaDetalleDTO dto = new QuejaDetalleDTO();
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());

        // Categoría y subcategoría se mantienen como ID para no cambiar el contrato del frontend.
        if (queja.getCategoriaId() != null) {
            dto.setCategoria(queja.getCategoriaId().toString());
        }
        if (queja.getSubcategoriaId() != null) {
            dto.setSubcategoria(queja.getSubcategoriaId().toString());
        }

        dto.setZona(queja.getZona());
        dto.setDireccionExacta(queja.getDireccionExacta());
        dto.setPuntoReferencia(queja.getPuntoReferencia());
        dto.setLatitud(queja.getLatitud());
        dto.setLongitud(queja.getLongitud());
        dto.setDescripcion(queja.getDescripcion());
        dto.setEstadoActual(queja.getEstadoActual());
        dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());
        dto.setFechaRegistro(queja.getFechaRegistro());

        // Queja no tiene relación JPA con sus evidencias; se consultan aparte.
        List<EvidenciaDigital> evidencias = evidenciaRepository.findByQueja(queja);
        if (evidencias != null && !evidencias.isEmpty()) {
            List<EvidenciaDTO> evidenciasDTO = evidencias.stream().map(e -> {
                EvidenciaDTO evDto = new EvidenciaDTO();
                evDto.setUrlArchivo(e.getUrlArchivo());
                evDto.setNombreArchivo(e.getNombreArchivo());
                return evDto;
            }).collect(Collectors.toList());
            dto.setEvidencias(evidenciasDTO);
        }

        return dto;
    }

    // 4. Registro de agravamiento o reincidencia

    // Registra un agravamiento o reincidencia a partir de una queja padre.
    @Transactional
    public QuejaRegistroResponse registrarQuejaDerivada(QuejaDerivadaDTO dto, List<MultipartFile> fotos, String correoCiudadano) {

        // Validaciones del formulario antes de persistir.
        if (dto.getCorrelativoPadre() == null || dto.getDescripcion() == null || dto.getTipoDerivacion() == null) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }

        String desc = dto.getDescripcion().trim();
        if (desc.length() < 20 || desc.length() > 500) {
            throw new IllegalArgumentException("La descripción debe tener entre 20 y 500 caracteres.");
        }

        if (fotos == null || fotos.isEmpty() || fotos.get(0).isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar al menos una fotografía de evidencia.");
        }
        if (fotos.size() > 3) {
            throw new IllegalArgumentException("No puede adjuntar más de 3 fotografías.");
        }

        // La queja padre aporta la ubicación y la clasificación del reporte.
        Queja quejaPadre = quejaRepository.findByCorrelativo(dto.getCorrelativoPadre())
                .orElseThrow(() -> new IllegalArgumentException("La queja original no existe."));

        Usuario ciudadano = usuarioRepository.findByCorreo(correoCiudadano)
                .orElseThrow(() -> new IllegalArgumentException("Ciudadano no encontrado."));

        Queja nuevaQueja = new Queja();
        nuevaQueja.setCorrelativo("TEMP-" + System.currentTimeMillis());
        nuevaQueja.setCiudadano(ciudadano);
        nuevaQueja.setCategoriaId(quejaPadre.getCategoriaId());
        nuevaQueja.setSubcategoriaId(quejaPadre.getSubcategoriaId());
        nuevaQueja.setZona(quejaPadre.getZona());
        nuevaQueja.setDireccionExacta(quejaPadre.getDireccionExacta());
        nuevaQueja.setPuntoReferencia(quejaPadre.getPuntoReferencia());
        nuevaQueja.setLatitud(quejaPadre.getLatitud());
        nuevaQueja.setLongitud(quejaPadre.getLongitud());

        // El correlativo padre queda en la descripción para mantener la trazabilidad.
        nuevaQueja.setDescripcion("[" + dto.getTipoDerivacion() + " DE " + dto.getCorrelativoPadre() + "] " + desc);

        // Regla de negocio: un agravamiento es urgente; una reincidencia, alta.
        String prioridad = "AGRAVAMIENTO".equalsIgnoreCase(dto.getTipoDerivacion()) ? "URGENTE" : "ALTA";
        nuevaQueja.setPrioridadSugerida(prioridad);
        nuevaQueja.setPrioridadConfirmada(prioridad);

        asignacionAutomaticaService.asignarFuncionarioAutomatico().ifPresent(nuevaQueja::setFuncionario);

        // El ID generado permite construir el correlativo oficial.
        Queja guardada = quejaRepository.save(nuevaQueja);

        String correlativoOficial = String.format("QUE-%d-%06d", Year.now().getValue(), guardada.getQuejaId());
        guardada.setCorrelativo(correlativoOficial);
        quejaRepository.save(guardada);

        // Historial inicial de la queja derivada.
        HistorialEstadoQueja historial = new HistorialEstadoQueja();
        historial.setQueja(guardada);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo("REGISTRADA");
        historial.setCambiadoPor(ciudadano);
        historial.setComentario(guardada.getFuncionario() != null
                ? "Reporte de " + dto.getTipoDerivacion() + " registrado. Asignado automáticamente al funcionario: "
                    + guardada.getFuncionario().getNombres() + " " + guardada.getFuncionario().getApellidos()
                : "Reporte de " + dto.getTipoDerivacion() + " registrado.");
        historialRepository.save(historial);

        // Las evidencias se asocian a la nueva queja.
        for (MultipartFile foto : fotos) {
            String url = fileStorageService.guardarArchivo(foto);

            EvidenciaDigital evidencia = new EvidenciaDigital();
            evidencia.setQueja(guardada);
            evidencia.setUrlArchivo(url);
            evidencia.setNombreArchivo(foto.getOriginalFilename());
            evidencia.setFormato(foto.getContentType() != null && foto.getContentType().contains("png") ? "png" : "jpg");
            evidencia.setTamanioBytes(foto.getSize());
            evidencia.setSubidoPor(ciudadano);

            evidenciaRepository.save(evidencia);
        }

        String tipoMensaje = "AGRAVAMIENTO".equalsIgnoreCase(dto.getTipoDerivacion()) ? "agravamiento" : "inconformidad";
        return new QuejaRegistroResponse(
                correlativoOficial,
                "Su reporte de " + tipoMensaje + " ha sido registrado exitosamente y vinculado al expediente "
                        + dto.getCorrelativoPadre() + ". Se le ha asignado el nuevo correlativo " + correlativoOficial + "."
        );
    }
}