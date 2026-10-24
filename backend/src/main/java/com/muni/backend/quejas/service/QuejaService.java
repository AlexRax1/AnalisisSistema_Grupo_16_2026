package com.muni.backend.quejas.service;

import com.muni.backend.quejas.dto.ciudadano.*;
import com.muni.backend.quejas.dto.shared.*;
import com.muni.backend.quejas.model.EvidenciaDigital;
import com.muni.backend.quejas.model.HistorialEstadoQueja;
import com.muni.backend.quejas.model.CategoriaQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.model.SubcategoriaQueja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.repository.EvidenciaDigitalRepository;
import com.muni.backend.quejas.repository.HistorialEstadoQuejaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.repository.SubcategoriaQuejaRepository;
import com.muni.backend.shared.service.FileStorageService;
import com.muni.backend.usuarios.model.Usuario;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuejaService {

    private final QuejaRepository quejaRepository;
    private final CategoriaQuejaRepository categoriaQuejaRepository;
    private final SubcategoriaQuejaRepository subcategoriaQuejaRepository;
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

        // El modelo usa la relación a CategoriaQueja/SubcategoriaQueja; los campos *Id son de solo lectura.
        CategoriaQueja categoria = categoriaQuejaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));
        queja.setCategoria(categoria);

        if (dto.getSubcategoriaId() != null) {
            SubcategoriaQueja subcategoria = subcategoriaQuejaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("La subcategoría seleccionada no existe."));
            queja.setSubcategoria(subcategoria);
        }

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

        // Mapeos básicos
        dto.setQuejaId(queja.getQuejaId());
        dto.setCorrelativo(queja.getCorrelativo());
        dto.setZona(queja.getZona());
        dto.setDireccionExacta(queja.getDireccionExacta());
        dto.setPuntoReferencia(queja.getPuntoReferencia());
        dto.setLatitud(queja.getLatitud());
        dto.setLongitud(queja.getLongitud());
        dto.setDescripcion(queja.getDescripcion());
        dto.setEstadoActual(queja.getEstadoActual());
        dto.setFechaRegistro(queja.getFechaRegistro());

        // 1. Categoria (Obtener nombre legible)
        if (queja.getCategoria() != null) {
            dto.setCategoria(queja.getCategoria().getNombreCategoria());
            dto.setNombreCategoria(queja.getCategoria().getNombreCategoria());
        } else if (queja.getCategoriaId() != null) {
            dto.setCategoria(queja.getCategoriaId().toString());
        }

        // 2. Subcategoria
        if (queja.getSubcategoria() != null) {
            dto.setSubcategoria(queja.getSubcategoria().getNombreSubcategoria());
        } else if (queja.getSubcategoriaId() != null) {
            dto.setSubcategoria(queja.getSubcategoriaId().toString());
        }

        // 3. DPI Ciudadano
        if (queja.getCiudadano() != null && queja.getCiudadano().getDpi() != null) {
            dto.setDpiCiudadano(queja.getCiudadano().getDpi());
        } else {
            dto.setDpiCiudadano("Sin DPI");
        }

        // 4. Nombre Inspector
        if (queja.getInspector() != null) {
            String nombreCompleto = (queja.getInspector().getNombres() != null ? queja.getInspector().getNombres() : "") + " "
                    + (queja.getInspector().getApellidos() != null ? queja.getInspector().getApellidos() : "");
            dto.setNombreInspector(nombreCompleto.trim());
        } else {
            dto.setNombreInspector("Sin Asignar");
        }

        // 5. Prioridad (Garantizar que no vaya nulo)
        if (queja.getPrioridadConfirmada() != null && !queja.getPrioridadConfirmada().trim().isEmpty()) {
            dto.setPrioridadConfirmada(queja.getPrioridadConfirmada());
        } else if (queja.getPrioridadSugerida() != null && !queja.getPrioridadSugerida().trim().isEmpty()) {
            dto.setPrioridadConfirmada(queja.getPrioridadSugerida());
        } else {
            dto.setPrioridadConfirmada("MEDIA");
        }

        // 6. Evidencias Digitales
        if (evidenciaRepository != null) {
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
        nuevaQueja.setCategoria(quejaPadre.getCategoria());
        nuevaQueja.setSubcategoria(quejaPadre.getSubcategoria());
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

    @Transactional(readOnly = true)
    public Page<QuejaDetalleDTO> obtenerCatalogoAdministrativo(
            String buscar, String estado, String categoria, String prioridad,
            LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        String termino = (buscar != null && !buscar.trim().isEmpty()) ? buscar.trim() : null;

        // Asignación directa del estado exacto
        String estadoFiltro = (estado != null && !estado.trim().isEmpty() && !"TODAS".equalsIgnoreCase(estado.trim()))
                ? estado.trim() : null;

        Page<Queja> quejasPage;

        if (inicio != null && fin != null) {
            if (termino != null && estadoFiltro != null) {
                quejasPage = quejaRepository.findByCorrelativoContainingIgnoreCaseAndEstadoActualIgnoreCaseAndFechaRegistroBetween(
                        termino, estadoFiltro, inicio, fin, pageable);
            } else if (termino != null) {
                quejasPage = quejaRepository.findByCorrelativoContainingIgnoreCaseAndFechaRegistroBetween(termino, inicio, fin, pageable);
            } else if (estadoFiltro != null) {
                quejasPage = quejaRepository.findByEstadoActualIgnoreCaseAndFechaRegistroBetween(estadoFiltro, inicio, fin, pageable);
            } else {
                quejasPage = quejaRepository.findByFechaRegistroBetween(inicio, fin, pageable);
            }
        } else {
            if (termino != null && estadoFiltro != null) {
                quejasPage = quejaRepository.findByCorrelativoContainingIgnoreCaseAndEstadoActualIgnoreCase(termino, estadoFiltro, pageable);
            } else if (termino != null) {
                quejasPage = quejaRepository.findByCorrelativoContainingIgnoreCase(termino, pageable);
            } else if (estadoFiltro != null) {
                quejasPage = quejaRepository.findByEstadoActualIgnoreCase(estadoFiltro, pageable);
            } else {
                quejasPage = quejaRepository.findAll(pageable);
            }
        }

        return quejasPage.map(this::convertirADetalleDTO);
    }

    @Transactional(readOnly = true)
    public byte[] generarPdfCatalogo() {
        List<Queja> quejas = quejaRepository.findAll();

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("CATÁLOGO GENERAL DE QUEJAS MUNICIPALES");
                contentStream.endText();

                int y = 710;
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                for (Queja q : quejas) {
                    if (y < 50) { // Si se acaba la página, no encimar texto
                        break;
                    }
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    String linea = String.format("%s | %s | %s | Zona %d",
                            q.getCorrelativo() != null ? q.getCorrelativo() : "S/C",
                            q.getEstadoActual() != null ? q.getEstadoActual() : "S/E",
                            q.getPrioridadConfirmada() != null ? q.getPrioridadConfirmada() : "MEDIA",
                            q.getZona());
                    contentStream.showText(linea);
                    contentStream.endText();
                    y -= 20;
                }
            }

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarExcelCatalogo() {
        List<Queja> quejas = quejaRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Catálogo de Quejas");

            // Crear fila de encabezados
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"Correlativo", "Fecha Registro", "Zona", "Dirección", "Estado", "Prioridad", "Descripción"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (Queja q : quejas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(q.getCorrelativo() != null ? q.getCorrelativo() : "");
                row.createCell(1).setCellValue(q.getFechaRegistro() != null ? q.getFechaRegistro().toString() : "");
                row.createCell(2).setCellValue(q.getZona());
                row.createCell(3).setCellValue(q.getDireccionExacta() != null ? q.getDireccionExacta() : "");
                row.createCell(4).setCellValue(q.getEstadoActual() != null ? q.getEstadoActual() : "");
                row.createCell(5).setCellValue(q.getPrioridadConfirmada() != null ? q.getPrioridadConfirmada() : "");
                row.createCell(6).setCellValue(q.getDescripcion() != null ? q.getDescripcion() : "");
            }

            // Autoajustar ancho de columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<QuejaDetalleDTO> obtenerCatalogoConFiltros(
            String estado,
            Integer categoriaId,
            String prioridad,
            String buscar,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Pageable pageable
    ) {
        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        String estadoFiltro = (estado != null && !estado.trim().isEmpty() && !estado.equalsIgnoreCase("TODAS")) ? estado.trim() : null;
        String prioridadFiltro = (prioridad != null && !prioridad.trim().isEmpty() && !prioridad.equalsIgnoreCase("TODAS")) ? prioridad.trim().toUpperCase() : null;

        // Si viene vacio o espacios, se pasa como NULL exacto, de lo contrario se convierte a minusculas
        String buscarFiltro = (buscar != null && !buscar.trim().isEmpty()) ? buscar.trim().toLowerCase() : null;

        Integer categoriaFiltro = (categoriaId != null && categoriaId > 0) ? categoriaId : null;

        Page<Queja> quejas = quejaRepository.buscarCatalogoConFiltros(
                estadoFiltro, categoriaFiltro, prioridadFiltro, buscarFiltro, inicio, fin, pageable
        );

        return quejas.map(this::convertirADetalleDTO);
    }
}