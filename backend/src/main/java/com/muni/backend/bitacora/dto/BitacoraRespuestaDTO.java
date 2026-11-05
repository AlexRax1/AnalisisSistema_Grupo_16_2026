package com.muni.backend.bitacora.dto;

import lombok.Data;

import java.time.LocalDateTime;

// Datos de una acción registrada en la bitácora para consulta.
@Data
public class BitacoraRespuestaDTO {
    private Integer registroId;
    private Integer userId;
    private String usuarioAccion;
    private String direccionIp;
    private LocalDateTime fechaHora;
    private String tablaAfectada;
    private String idAfectado;
    private String campoAfectado;
    private String accion;
    private String descripcion;
    private String registroAntiguo;
    private String registroNuevo;
}
