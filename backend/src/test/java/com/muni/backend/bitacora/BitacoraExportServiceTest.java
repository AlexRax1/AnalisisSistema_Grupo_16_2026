package com.muni.backend.bitacora;

import com.muni.backend.bitacora.dto.BitacoraFiltroDTO;
import com.muni.backend.bitacora.dto.BitacoraRespuestaDTO;
import com.muni.backend.bitacora.service.BitacoraExportService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitacoraExportServiceTest {

    private final BitacoraExportService exportService = new BitacoraExportService();

    @Test
    void debeGenerarUnPdfValido() {
        byte[] pdf = exportService.generarPdf(List.of(registro()), filtro());

        assertTrue(pdf.length > 0);
        assertEquals('%', pdf[0]);
        assertEquals('P', pdf[1]);
        assertEquals('D', pdf[2]);
        assertEquals('F', pdf[3]);
    }

    @Test
    void debeGenerarUnExcelValido() {
        byte[] excel = exportService.generarExcel(List.of(registro()), filtro());

        assertTrue(excel.length > 0);
        assertEquals('P', excel[0]);
        assertEquals('K', excel[1]);
    }

    private BitacoraRespuestaDTO registro() {
        BitacoraRespuestaDTO dto = new BitacoraRespuestaDTO();
        dto.setRegistroId(1);
        dto.setUserId(7);
        dto.setUsuarioAccion("admin");
        dto.setDireccionIp("127.0.0.1");
        dto.setFechaHora(LocalDateTime.now());
        dto.setTablaAfectada("quejas");
        dto.setIdAfectado("13");
        dto.setCampoAfectado("estado_actual");
        dto.setAccion("ACTUALIZAR");
        dto.setDescripcion("ACTUALIZAR en quejas (estado_actual), registro 13");
        dto.setRegistroAntiguo("REGISTRADA");
        dto.setRegistroNuevo("EN INSPECCIÓN");
        return dto;
    }

    private BitacoraFiltroDTO filtro() {
        BitacoraFiltroDTO filtro = new BitacoraFiltroDTO();
        filtro.setTabla("quejas");
        return filtro;
    }
}
