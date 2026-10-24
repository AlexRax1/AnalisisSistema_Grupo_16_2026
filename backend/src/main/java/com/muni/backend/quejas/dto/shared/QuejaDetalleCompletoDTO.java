package com.muni.backend.quejas.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuejaDetalleCompletoDTO {
    private Integer quejaId;
    private String correlativo;
    private String tipoRegistro;
    private String correlativoOrigen;

    // Datos ciudadanos
    private Integer ciudadanoId;
    private String ciudadanoNombre;
    private String ciudadanoDpi;
    private String ciudadanoTelefono;
    private String ciudadanoCorreo;

    // Clasificación y ubicación
    private Integer categoriaId;
    private String categoria;
    private Integer subcategoriaId;
    private String subcategoria;
    private Integer zona;
    private String direccionExacta;
    private String puntoReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String descripcion;

    // Estado y prioridades
    private String estadoActual;
    private String prioridadSugerida;
    private String prioridadConfirmada;

    // Asignaciones
    private Integer funcionarioId;
    private String funcionarioNombre;
    private Integer inspectorId;
    private String inspectorNombre;
    private Integer especialistaId;
    private String especialistaNombre;
    private Integer dependenciaId;
    private String dependenciaNombre;

    // Fechas y cierre
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;
    private LocalDateTime fechaCierre;
    private String motivoRechazo;

    // Relaciones completas
    private List<EvidenciaDTO> evidencias;
    private List<InformeDetalleDTO> informes;
    private List<HistorialDetalleDTO> historial;

    // Acciones dinámicas que el rol autenticado puede realizar sobre esta queja
    private List<String> accionesDisponibles;
}
