package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.QuejaRegistroDTO;
import com.muni.backend.quejas.dto.QuejaRegistroResponse;
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
}