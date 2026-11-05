package com.muni.backend.reportes.model;

import java.util.Locale;

public enum ReporteAgrupacion {
    CATEGORIA("Categoría"),
    ESTADO("Estado"),
    SUBCATEGORIA("Subcategoría"),
    PRIORIDAD("Prioridad"),
    TIPO_REGISTRO("Tipo de registro"),
    DEPENDENCIA("Dependencia"),
    ESPECIALISTA("Especialista"),
    INSPECTOR("Inspector");

    private final String etiqueta;

    ReporteAgrupacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static ReporteAgrupacion from(String valor) {
        if (valor == null || valor.isBlank()) {
            return CATEGORIA;
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Agrupación no válida: " + valor);
        }
    }
}
