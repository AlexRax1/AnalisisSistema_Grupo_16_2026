package com.muni.backend.reportes.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.quejas.model.EstadoQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.repository.DependenciaRepository;
import com.muni.backend.quejas.repository.QuejaRepository;
import com.muni.backend.quejas.repository.SubcategoriaQuejaRepository;
import com.muni.backend.reportes.dto.OpcionDTO;
import com.muni.backend.reportes.dto.ReporteDetalleDTO;
import com.muni.backend.reportes.dto.ReporteFilaDTO;
import com.muni.backend.reportes.dto.ReporteFiltroDTO;
import com.muni.backend.reportes.dto.ReporteKpiDTO;
import com.muni.backend.reportes.dto.ReporteOpcionesDTO;
import com.muni.backend.reportes.dto.ReporteResponseDTO;
import com.muni.backend.reportes.dto.ReporteSerieDTO;
import com.muni.backend.reportes.dto.ReporteTablaDTO;
import com.muni.backend.reportes.model.ReporteAgrupacion;
import com.muni.backend.reportes.repository.ReporteQuejaRepository;
import com.muni.backend.reportes.repository.proyeccion.ReporteAgrupadoProjection;
import com.muni.backend.reportes.repository.proyeccion.ReporteKpiProjection;
import com.muni.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteQuejaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int LIMITE_DETALLE_DEFECTO = 100;
    private static final int LIMITE_DETALLE_MAXIMO = 10000;

    private final ReporteQuejaRepository reporteRepository;
    private final QuejaRepository quejaRepository;
    private final CategoriaQuejaRepository categoriaRepository;
    private final SubcategoriaQuejaRepository subcategoriaRepository;
    private final DependenciaRepository dependenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraService bitacoraService;

    @Transactional(readOnly = true)
    public ReporteOpcionesDTO obtenerOpciones() {
        List<OpcionDTO> categorias = categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getEstado() == null || "ACTIVO".equalsIgnoreCase(categoria.getEstado()))
                .map(categoria -> new OpcionDTO(
                        categoria.getCategoriaId(),
                        categoria.getNombreCategoria(),
                        null
                ))
                .toList();

        List<OpcionDTO> subcategorias = subcategoriaRepository.findAll().stream()
                .filter(subcategoria -> subcategoria.getEstado() == null || "ACTIVO".equalsIgnoreCase(subcategoria.getEstado()))
                .map(subcategoria -> new OpcionDTO(
                        subcategoria.getSubcategoriaId(),
                        subcategoria.getNombreSubcategoria(),
                        subcategoria.getCategoria() != null ? subcategoria.getCategoria().getCategoriaId() : null
                ))
                .toList();

        List<OpcionDTO> dependencias = dependenciaRepository.findAll().stream()
                .filter(dependencia -> dependencia.getActivo() == null || Boolean.TRUE.equals(dependencia.getActivo()))
                .map(dependencia -> new OpcionDTO(
                        dependencia.getDependenciaId(),
                        dependencia.getNombreDependencia(),
                        null
                ))
                .toList();

        List<OpcionDTO> especialistas = usuarioRepository.findByRolActivo("ESPECIALISTA_TECNICO").stream()
                .map(usuario -> new OpcionDTO(
                        usuario.getUsuarioId(),
                        usuario.getNombres() + " " + usuario.getApellidos(),
                        null
                ))
                .toList();

        List<String> estados = Arrays.stream(EstadoQueja.values())
                .map(EstadoQueja::getValor)
                .toList();

        return new ReporteOpcionesDTO(
                categorias,
                subcategorias,
                dependencias,
                especialistas,
                estados,
                List.of("BAJA", "MEDIA", "ALTA", "URGENTE"),
                Arrays.stream(ReporteAgrupacion.values()).map(Enum::name).toList()
        );
    }

    @Transactional
    public ReporteResponseDTO generar(ReporteFiltroDTO filtro, String username, int limiteDetalle) {
        return generar(filtro, username, limiteDetalle, true);
    }

    @Transactional
    public ReporteResponseDTO generar(ReporteFiltroDTO filtro, String username, int limiteDetalle, boolean auditarConsulta) {
        LocalDate fechaDesde = filtro != null && filtro.fechaDesde() != null
                ? filtro.fechaDesde()
                : LocalDate.now().withDayOfMonth(1);
        LocalDate fechaHasta = filtro != null && filtro.fechaHasta() != null
                ? filtro.fechaHasta()
                : LocalDate.now();

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser mayor que la fecha hasta.");
        }

        String estado = normalizar(filtro != null ? filtro.estado() : null);
        String prioridad = normalizar(filtro != null ? filtro.prioridad() : null);
        Integer categoriaId = filtro != null && filtro.categoriaId() != null && filtro.categoriaId() > 0
                ? filtro.categoriaId()
                : null;
        ReporteAgrupacion agrupacion = ReporteAgrupacion.from(filtro != null ? filtro.agrupacion() : null);
        int limite = Math.min(Math.max(limiteDetalle, 1), LIMITE_DETALLE_MAXIMO);

        List<ReporteAgrupadoProjection> agrupados = reporteRepository.agrupar(
                fechaDesde, fechaHasta, estado, categoriaId, prioridad, agrupacion.name()
        );
        ReporteKpiProjection kpi = reporteRepository.obtenerKpis(
                fechaDesde, fechaHasta, estado, categoriaId, prioridad
        );

        long total = kpi != null ? valor(kpi.getTotal()) : 0;
        long cerradas = kpi != null ? valor(kpi.getCerradas()) : 0;
        long rechazadas = kpi != null ? valor(kpi.getRechazadas()) : 0;
        long enProceso = kpi != null ? valor(kpi.getEnProceso()) : 0;
        BigDecimal promedio = kpi != null && kpi.getPromedioDiasCierre() != null
                ? kpi.getPromedioDiasCierre()
                : BigDecimal.ZERO;

        List<ReporteFilaDTO> filas = agrupados.stream()
                .map(fila -> new ReporteFilaDTO(
                        fila.getEtiqueta() != null ? fila.getEtiqueta() : "Sin dato",
                        valor(fila.getTotal()),
                        valor(fila.getCerradas()),
                        valor(fila.getEnProceso()),
                        valor(fila.getRechazadas()),
                        porcentaje(fila.getPorcentaje(), valor(fila.getTotal()), total)
                ))
                .toList();

        List<ReporteSerieDTO> series = filas.stream()
                .map(fila -> new ReporteSerieDTO(fila.etiqueta(), fila.total(), fila.porcentaje()))
                .toList();

        Map<String, String> filtrosAplicados = new LinkedHashMap<>();
        filtrosAplicados.put("Fecha desde", fechaDesde.format(FECHA));
        filtrosAplicados.put("Fecha hasta", fechaHasta.format(FECHA));
        filtrosAplicados.put("Estado", estado != null ? estado : "Todas");
        filtrosAplicados.put("Categoría", categoriaId != null
                ? categoriaRepository.findById(categoriaId)
                    .map(categoria -> categoria.getNombreCategoria())
                    .orElse("Todas")
                : "Todas");
        filtrosAplicados.put("Prioridad", prioridad != null ? prioridad : "Todas");
        filtrosAplicados.put("Agrupación", agrupacion.getEtiqueta());

        Page<Queja> paginaDetalle = quejaRepository.buscarCatalogoConFiltros(
                estado,
                categoriaId,
                prioridad,
                null,
                fechaDesde.atStartOfDay(),
                fechaHasta.plusDays(1).atStartOfDay().minusNanos(1),
                PageRequest.of(0, limite)
        );

        List<ReporteDetalleDTO> detalle = paginaDetalle.getContent().stream()
                .map(this::mapearDetalle)
                .toList();

        double tasaResolucion = total > 0 ? cerradas * 100.0 / total : 0;

        ReporteResponseDTO respuesta = new ReporteResponseDTO(
                "Reporte de quejas por " + agrupacion.getEtiqueta().toLowerCase(Locale.ROOT),
                "Periodo: " + fechaDesde.format(FECHA) + " - " + fechaHasta.format(FECHA),
                LocalDateTime.now(),
                filtrosAplicados,
                List.of(
                        new ReporteKpiDTO("Total quejas", formatearEntero(total), "description", "primary"),
                        new ReporteKpiDTO("Resueltas", formatearEntero(cerradas), "check_circle", "success"),
                        new ReporteKpiDTO("En proceso", formatearEntero(enProceso), "pending", "warning"),
                        new ReporteKpiDTO("Rechazadas", formatearEntero(rechazadas), "cancel", "danger"),
                        new ReporteKpiDTO("Tasa de resolución", String.format(Locale.US, "%.1f%%", tasaResolucion), "percent", "info"),
                        new ReporteKpiDTO("Días promedio de cierre", String.format(Locale.US, "%.1f días", promedio.doubleValue()), "timer", "neutral")
                ),
                series,
                new ReporteTablaDTO(
                        "Resumen por " + agrupacion.getEtiqueta().toLowerCase(Locale.ROOT),
                        List.of("Dimensión", "Total", "Resueltas", "En proceso", "Rechazadas", "%"),
                        filas
                ),
                detalle,
                paginaDetalle.getTotalElements(),
                limite
        );

        if (auditarConsulta) {
            registrarConsulta(username, filtroData(fechaDesde, fechaHasta, estado, categoriaId, prioridad, agrupacion));
        }

        return respuesta;
    }

    @Transactional
    public void registrarExportacion(ReporteFiltroDTO filtro, String username, String formato) {
        LocalDate fechaDesde = filtro != null && filtro.fechaDesde() != null
                ? filtro.fechaDesde()
                : LocalDate.now().withDayOfMonth(1);
        LocalDate fechaHasta = filtro != null && filtro.fechaHasta() != null
                ? filtro.fechaHasta()
                : LocalDate.now();
        String estado = normalizar(filtro != null ? filtro.estado() : null);
        String prioridad = normalizar(filtro != null ? filtro.prioridad() : null);
        Integer categoriaId = filtro != null && filtro.categoriaId() != null && filtro.categoriaId() > 0
                ? filtro.categoriaId()
                : null;
        ReporteAgrupacion agrupacion = ReporteAgrupacion.from(filtro != null ? filtro.agrupacion() : null);

        bitacoraService.registrar(
                username,
                "reportes",
                "quejas",
                "exportacion_" + formato,
                "DESCARGAR",
                null,
                json(filtroData(fechaDesde, fechaHasta, estado, categoriaId, prioridad, agrupacion))
        );
    }

    private void registrarConsulta(String username, Map<String, Object> datos) {
        bitacoraService.registrar(
                username,
                "reportes",
                "quejas",
                "consulta",
                "CONSULTAR",
                null,
                json(datos)
        );
    }

    private ReporteDetalleDTO mapearDetalle(Queja queja) {
        return new ReporteDetalleDTO(
                queja.getQuejaId(),
                queja.getCorrelativo(),
                queja.getFechaRegistro(),
                queja.getCiudadano() != null
                        ? queja.getCiudadano().getNombres() + " " + queja.getCiudadano().getApellidos()
                        : "Sin ciudadano",
                queja.getCiudadano() != null ? queja.getCiudadano().getDpi() : null,
                queja.getCategoria() != null ? queja.getCategoria().getNombreCategoria() : "Sin categoría",
                queja.getSubcategoria() != null ? queja.getSubcategoria().getNombreSubcategoria() : "Sin subcategoría",
                queja.getEstadoActual(),
                queja.getPrioridadConfirmada(),
                queja.getInspector() != null
                        ? queja.getInspector().getNombres() + " " + queja.getInspector().getApellidos()
                        : "Sin asignar",
                queja.getEspecialista() != null
                        ? queja.getEspecialista().getNombres() + " " + queja.getEspecialista().getApellidos()
                        : "Sin asignar",
                queja.getDependenciaAsignada() != null
                        ? queja.getDependenciaAsignada().getNombreDependencia()
                        : "Sin dependencia",
                queja.getFechaCierre(),
                queja.getDireccionExacta()
        );
    }

    private Map<String, Object> filtroData(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String estado,
            Integer categoriaId,
            String prioridad,
            ReporteAgrupacion agrupacion
    ) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("fechaDesde", fechaDesde != null ? fechaDesde.toString() : null);
        datos.put("fechaHasta", fechaHasta != null ? fechaHasta.toString() : null);
        datos.put("estado", estado);
        datos.put("categoriaId", categoriaId);
        datos.put("prioridad", prioridad);
        datos.put("agrupacion", agrupacion.name());
        return datos;
    }

    private String json(Map<String, Object> datos) {
        return datos.entrySet().stream()
                .map(entry -> "\"" + escapar(entry.getKey()) + "\":" + valorJson(entry.getValue()))
                .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }

    private String valorJson(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return String.valueOf(valor);
        }
        return "\"" + escapar(String.valueOf(valor)) + "\"";
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank() || "TODAS".equalsIgnoreCase(valor.trim())) {
            return null;
        }
        return valor.trim();
    }

    private long valor(Long valor) {
        return valor != null ? valor : 0L;
    }

    private double porcentaje(BigDecimal porcentaje, long valor, long total) {
        if (porcentaje != null) {
            return porcentaje.doubleValue();
        }
        return total > 0 ? valor * 100.0 / total : 0;
    }

    private String formatearEntero(long valor) {
        return String.format(Locale.US, "%,d", valor);
    }
}
