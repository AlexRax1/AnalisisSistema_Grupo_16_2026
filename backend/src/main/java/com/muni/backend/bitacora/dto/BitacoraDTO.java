package com.muni.backend.bitacora.dto;

import lombok.Data;

// Datos para registrar una acción manual en la bitácora.
@Data
public class BitacoraDTO {
    private Integer userId;
    private String usuarioAccion;
    private String tablaAfectada;
    private String idAfectado;
    private String campoAfectado;
    private String accion;
    private String registroAntiguo;
    private String registroNuevo;
}
