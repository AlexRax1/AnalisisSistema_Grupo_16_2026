package com.muni.backend.quejas.model;

public enum EstadoQueja {
    REGISTRADA("REGISTRADA"),
    EN_INSPECCION("EN INSPECCIÓN"),
    EN_VALIDACION_REPARACION("EN VALIDACIÓN DE REPARACIÓN"),
    EN_REPARACION_TECNICA("EN REPARACIÓN TÉCNICA"),
    PENDIENTE_DE_CIERRE("PENDIENTE DE CIERRE"),
    SOLUCIONADA_CERRADA("SOLUCIONADA / CERRADA"),
    RECHAZADA("RECHAZADA");

    private final String valor;

    EstadoQueja(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoQueja fromValor(String valor) {
        for (EstadoQueja e : values()) {
            if (e.valor.equals(valor)) return e;
        }
        throw new IllegalArgumentException("Estado no reconocido: " + valor);
    }
}
