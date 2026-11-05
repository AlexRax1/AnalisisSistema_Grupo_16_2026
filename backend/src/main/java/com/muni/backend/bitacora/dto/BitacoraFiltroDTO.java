package com.muni.backend.bitacora.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Filtros compartidos por la consulta y las exportaciones de la bitácora.
@Data
public class BitacoraFiltroDTO {

    private String usuario;
    private String tabla;
    private String accion;
    private String idAfectado;
    private String campoAfectado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    // Resumen legible que se guarda en la auditoría de la propia exportación.
    public String resumen() {
        List<String> partes = new ArrayList<>();
        agregar(partes, "usuario", usuario);
        agregar(partes, "tabla", tabla);
        agregar(partes, "accion", accion);
        agregar(partes, "idAfectado", idAfectado);
        agregar(partes, "campoAfectado", campoAfectado);
        if (fechaInicio != null) {
            partes.add("fechaInicio=" + fechaInicio);
        }
        if (fechaFin != null) {
            partes.add("fechaFin=" + fechaFin);
        }
        return partes.isEmpty() ? "Sin filtros" : String.join(", ", partes);
    }

    private void agregar(List<String> partes, String nombre, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(nombre + "=" + valor.trim());
        }
    }
}
