package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.*;
import com.muni.backend.quejas.model.EvidenciaDigital;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
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
    private final FileStorageService fileStorageService;

    @Transactional
    public QuejaRegistroResponse registrarQueja(QuejaRegistroDTO dto, List<MultipartFile> fotos, String correoCiudadano) {

        // 1. Validaciones de Negocio
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

        // 2. Cálculo de Prioridad por Demanda (Radio de 300 metros)
        int quejasSimilares = quejaRepository.contarQuejasCercanasActivas(
                dto.getCategoriaId(),
                dto.getZona(),
                dto.getLatitud(),
                dto.getLongitud(),
                300.0
        );

        String prioridadCalculada;
        if (quejasSimilares >= 4) {
            prioridadCalculada = "URGENTE"; // 5 o más reportes en el área
        } else if (quejasSimilares >= 2) {
            prioridadCalculada = "ALTA";    // 3 a 4 reportes
        } else if (quejasSimilares >= 1) {
            prioridadCalculada = "MEDIA";   // 2 reportes
        } else {
            prioridadCalculada = "BAJA";    // Primer reporte aislado
        }

        // 3. Guardado inicial de Queja
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
        queja.setPrioridadSugerida(prioridadCalculada);
        queja.setPrioridadConfirmada(prioridadCalculada);

        Queja guardada = quejaRepository.save(queja);

        // 4. Asignación del Correlativo Oficial
        String correlativo = String.format("QUE-%d-%06d", Year.now().getValue(), guardada.getQuejaId());
        guardada.setCorrelativo(correlativo);
        quejaRepository.save(guardada);

        // 5. Guardado de Evidencias Digitales
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

    @Transactional(readOnly = true)
    public List<QuejaDetalleDTO> obtenerMisQuejas(String correoCiudadano) {
        Usuario ciudadano = usuarioRepository.findByCorreo(correoCiudadano)
                .orElseThrow(() -> new IllegalArgumentException("Ciudadano no encontrado."));

        // Ajusta la llamada al método del repositorio según cómo tengas definido el ID de Usuario
        List<Queja> quejas = quejaRepository.findByCiudadano_UsuarioIdOrderByQuejaIdDesc(ciudadano.getUsuarioId());

        return quejas.stream()
                .map(this::convertirADetalleDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuejaDetalleDTO obtenerPorCorrelativo(String correlativo) {
        Queja queja = quejaRepository.findByCorrelativo(correlativo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la queja con correlativo: " + correlativo));

        return convertirADetalleDTO(queja);
    }

    private QuejaDetalleDTO convertirADetalleDTO(Queja queja) {
        QuejaDetalleDTO dto = new QuejaDetalleDTO();
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());

        // Manejo seguro de Categoria/Subcategoria por ID si no existen relaciones
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

        // Búsqueda de evidencias a través del repositorio si no hay relación JPA en Queja
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

    @Transactional
    public QuejaRegistroResponse registrarQuejaDerivada(QuejaDerivadaDTO dto, List<MultipartFile> fotos, String correoCiudadano) {

        // 1. Validaciones básicas
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

        // 2. Buscar la queja original (Padre)
        Queja quejaPadre = quejaRepository.findByCorrelativo(dto.getCorrelativoPadre())
                .orElseThrow(() -> new IllegalArgumentException("La queja original no existe."));

        Usuario ciudadano = usuarioRepository.findByCorreo(correoCiudadano)
                .orElseThrow(() -> new IllegalArgumentException("Ciudadano no encontrado."));

        // 3. Crear la nueva queja derivada
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

        // Incluir referencia a la queja original en la descripción
        nuevaQueja.setDescripcion("[" + dto.getTipoDerivacion() + " DE " + dto.getCorrelativoPadre() + "] " + desc);

        // Si es Agravamiento, se asigna URGENTE automáticamente (FA05)
        String prioridad = "AGRAVAMIENTO".equalsIgnoreCase(dto.getTipoDerivacion()) ? "URGENTE" : "ALTA";
        nuevaQueja.setPrioridadSugerida(prioridad);
        nuevaQueja.setPrioridadConfirmada(prioridad);

        Queja guardada = quejaRepository.save(nuevaQueja);

        // 4. Asignación de Correlativo Oficial
        String correlativoOficial = String.format("QUE-%d-%06d", Year.now().getValue(), guardada.getQuejaId());
        guardada.setCorrelativo(correlativoOficial);
        quejaRepository.save(guardada);

        // 5. Guardado de Evidencias Digitales
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