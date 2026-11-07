package com.muni.backend.dashboard.service;

import com.muni.backend.bitacora.service.BitacoraService;
import com.muni.backend.dashboard.dto.DashboardContadorDTO;
import com.muni.backend.dashboard.dto.DashboardEstadoDTO;
import com.muni.backend.dashboard.dto.DashboardQuejaDTO;
import com.muni.backend.dashboard.dto.DashboardResponseDTO;
import com.muni.backend.dashboard.model.GrupoEstadoDashboard;
import com.muni.backend.dashboard.repository.DashboardRepository;
import com.muni.backend.dashboard.repository.proyeccion.DashboardEstadoProjection;
import com.muni.backend.quejas.model.EstadoQueja;
import com.muni.backend.quejas.model.Queja;
import com.muni.backend.quejas.repository.QuejaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMITE_RECIENTES_MAXIMO = 50;
    private static final List<EstadoQueja> ORDEN_ESTADOS = List.of(
            EstadoQueja.REGISTRADA,
            EstadoQueja.EN_INSPECCION,
            EstadoQueja.EN_REPARACION_TECNICA,
            EstadoQueja.EN_VALIDACION_REPARACION,
            EstadoQueja.PENDIENTE_DE_CIERRE,
            EstadoQueja.SOLUCIONADA_CERRADA,
            EstadoQueja.RECHAZADA
    );

    private final DashboardRepository dashboardRepository;
    private final QuejaRepository quejaRepository;
    private final BitacoraService bitacoraService;

    @Transactional(readOnly = true)
    public DashboardResponseDTO obtenerDashboard(int limiteRecientes) {
        int limite = Math.min(Math.max(limiteRecientes, 1), LIMITE_RECIENTES_MAXIMO);

        List<DashboardEstadoProjection> crudos = dashboardRepository.contarPorEstado();
        Map<String, Long> porEstado = crudos.stream()
                .filter(fila -> fila.getEstado() != null)
                .collect(Collectors.toMap(
                        DashboardEstadoProjection::getEstado,
                        fila -> valor(fila.getTotal()),
                        Long::sum
                ));

        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();

        List<DashboardEstadoDTO> estados = new ArrayList<>();
        Map<GrupoEstadoDashboard, Long> porGrupo = new EnumMap<>(GrupoEstadoDashboard.class);

        for (EstadoQueja estado : ORDEN_ESTADOS) {
            long cantidad = porEstado.getOrDefault(estado.getValor(), 0L);
            GrupoEstadoDashboard grupo = GrupoEstadoDashboard.desdeEstado(estado.getValor());
            porGrupo.merge(grupo, cantidad, Long::sum);
            estados.add(new DashboardEstadoDTO(
                    estado.getValor(),
                    grupo.name(),
                    cantidad,
                    porcentaje(cantidad, total)
            ));
        }

        // Si existiera un estado histórico no reconocido, se agrupa con las
        // rechazadas para que los contadores siempre sumen el total.
        long clasificados = estados.stream().mapToLong(DashboardEstadoDTO::total).sum();
        if (total > clasificados) {
            porGrupo.merge(GrupoEstadoDashboard.RECHAZADOS, total - clasificados, Long::sum);
        }

        List<DashboardContadorDTO> contadores = Arrays.stream(GrupoEstadoDashboard.values())
                .map(grupo -> {
                    long cantidad = porGrupo.getOrDefault(grupo, 0L);
                    return new DashboardContadorDTO(
                            grupo.name(),
                            grupo.getEtiqueta(),
                            cantidad,
                            porcentaje(cantidad, total),
                            grupo.getIcono()
                    );
                })
                .toList();

        List<DashboardQuejaDTO> recientes = quejaRepository.buscarCatalogoConFiltros(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, limite)
                ).getContent().stream()
                .map(this::mapearQueja)
                .toList();

        return new DashboardResponseDTO(
                LocalDateTime.now(),
                total,
                contadores,
                estados,
                recientes
        );
    }

    @Transactional
    public void registrarExportacion(String username, String formato, long total) {
        bitacoraService.registrar(
                username,
                "dashboard",
                "quejas",
                "exportacion_" + formato,
                "DESCARGAR",
                null,
                "{\"total\":" + total + "}"
        );
    }

    private DashboardQuejaDTO mapearQueja(Queja queja) {
        return new DashboardQuejaDTO(
                queja.getQuejaId(),
                queja.getCorrelativo(),
                queja.getFechaRegistro(),
                queja.getCiudadano() != null
                        ? queja.getCiudadano().getNombres() + " " + queja.getCiudadano().getApellidos()
                        : "Sin ciudadano",
                queja.getCategoria() != null
                        ? queja.getCategoria().getNombreCategoria()
                        : "Sin categoría",
                queja.getEstadoActual(),
                queja.getPrioridadConfirmada(),
                queja.getDependenciaAsignada() != null
                        ? queja.getDependenciaAsignada().getNombreDependencia()
                        : "Sin dependencia"
        );
    }

    private long valor(Long valor) {
        return valor != null ? valor : 0L;
    }

    private double porcentaje(long valor, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round(valor * 1000.0 / total) / 10.0;
    }
}
