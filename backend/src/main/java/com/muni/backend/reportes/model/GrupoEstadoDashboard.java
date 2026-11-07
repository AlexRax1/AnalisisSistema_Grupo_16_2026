package com.muni.backend.reportes.model;

/**
 * Agrupa los estados operativos de la queja en los cuatro bloques que muestra
 * el dashboard: nuevos, en proceso, pendientes y resueltos. Las quejas
 * rechazadas se exponen aparte para que el total cuadre.
 */
public enum GrupoEstadoDashboard {
    NUEVOS("Nuevos", "fiber_new"),
    EN_PROCESO("En proceso", "autorenew"),
    PENDIENTES("Pendientes", "pending_actions"),
    RESUELTOS("Resueltos", "check_circle"),
    RECHAZADOS("Rechazadas", "cancel");

    private final String etiqueta;
    private final String icono;

    GrupoEstadoDashboard(String etiqueta, String icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getIcono() {
        return icono;
    }

    public static GrupoEstadoDashboard desdeEstado(String estado) {
        if (estado == null) {
            return RECHAZADOS;
        }
        return switch (estado) {
            case "REGISTRADA" -> NUEVOS;
            case "EN INSPECCIÓN", "EN REPARACIÓN TÉCNICA", "EN VALIDACIÓN DE REPARACIÓN" -> EN_PROCESO;
            case "PENDIENTE DE CIERRE" -> PENDIENTES;
            case "SOLUCIONADA / CERRADA" -> RESUELTOS;
            default -> RECHAZADOS;
        };
    }
}
