package com.muni.backend.bitacora.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.muni.backend.bitacora.dto.BitacoraFiltroDTO;
import com.muni.backend.bitacora.dto.BitacoraRespuestaDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Genera los reportes de auditoría en PDF y Excel a partir de la bitácora.
@Service
public class BitacoraExportService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String[] COLUMNAS_PDF = {
            "Registro", "Fecha y hora", "Usuario", "IP", "Tabla", "ID afectado",
            "Campo", "Acción", "Descripción"
    };
    private static final String[] COLUMNAS_EXCEL = {
            "Registro", "Fecha y hora", "Usuario", "IP", "Tabla", "ID afectado",
            "Campo", "Acción", "Descripción", "Registro anterior", "Registro nuevo"
    };

    public byte[] generarPdf(List<BitacoraRespuestaDTO> filas, BitacoraFiltroDTO filtro) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);

            Paragraph cabecera = new Paragraph("REPORTE DE AUDITORÍA - BITÁCORA DEL SISTEMA", titulo);
            cabecera.setAlignment(Element.ALIGN_CENTER);
            document.add(cabecera);

            Paragraph detalle = new Paragraph(
                    "Emitido: " + LocalDateTime.now().format(FECHA)
                            + "  |  Filas: " + filas.size()
                            + "  |  Filtros: " + filtro.resumen(),
                    subtitulo);
            detalle.setAlignment(Element.ALIGN_CENTER);
            detalle.setSpacingAfter(12);
            document.add(detalle);

            PdfPTable tabla = new PdfPTable(COLUMNAS_PDF.length);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{6, 11, 12, 9, 12, 7, 10, 8, 25});

            for (String columna : COLUMNAS_PDF) {
                PdfPCell celda = new PdfPCell(new Phrase(columna, encabezado));
                celda.setBackgroundColor(new Color(30, 58, 138));
                celda.setPadding(4);
                tabla.addCell(celda);
            }

            for (BitacoraRespuestaDTO fila : filas) {
                agregarCelda(tabla, String.valueOf(fila.getRegistroId()), texto);
                agregarCelda(tabla, fecha(fila.getFechaHora()), texto);
                agregarCelda(tabla, texto(fila.getUsuarioAccion()), texto);
                agregarCelda(tabla, texto(fila.getDireccionIp()), texto);
                agregarCelda(tabla, texto(fila.getTablaAfectada()), texto);
                agregarCelda(tabla, texto(fila.getIdAfectado()), texto);
                agregarCelda(tabla, texto(fila.getCampoAfectado()), texto);
                agregarCelda(tabla, texto(fila.getAccion()), texto);
                agregarCelda(tabla, texto(fila.getDescripcion()), texto);
            }

            if (filas.isEmpty()) {
                PdfPCell vacia = new PdfPCell(new Phrase("No hay registros para los filtros indicados.", texto));
                vacia.setColspan(COLUMNAS_PDF.length);
                vacia.setPadding(8);
                tabla.addCell(vacia);
            }

            document.add(tabla);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el reporte de auditoría en PDF.", ex);
        }
    }

    public byte[] generarExcel(List<BitacoraRespuestaDTO> filas, BitacoraFiltroDTO filtro) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titulo = estiloTitulo(workbook);
            CellStyle encabezado = estiloEncabezado(workbook);
            CellStyle fecha = estiloFecha(workbook);

            Sheet hoja = workbook.createSheet("Bitacora");
            int filaActual = 0;

            Row tituloRow = hoja.createRow(filaActual++);
            tituloRow.createCell(0).setCellValue("REPORTE DE AUDITORÍA - BITÁCORA DEL SISTEMA");
            tituloRow.getCell(0).setCellStyle(titulo);

            hoja.createRow(filaActual++).createCell(0).setCellValue(
                    "Emitido: " + LocalDateTime.now().format(FECHA));
            hoja.createRow(filaActual++).createCell(0).setCellValue(
                    "Filas: " + filas.size() + " | Filtros: " + filtro.resumen());

            filaActual++;
            Row cabecera = hoja.createRow(filaActual++);
            for (int i = 0; i < COLUMNAS_EXCEL.length; i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(COLUMNAS_EXCEL[i]);
                celda.setCellStyle(encabezado);
            }

            for (BitacoraRespuestaDTO dto : filas) {
                Row row = hoja.createRow(filaActual++);
                row.createCell(0).setCellValue(dto.getRegistroId() != null ? dto.getRegistroId() : 0);
                Cell celdaFecha = row.createCell(1);
                celdaFecha.setCellValue(fecha(dto.getFechaHora()));
                celdaFecha.setCellStyle(fecha);
                row.createCell(2).setCellValue(texto(dto.getUsuarioAccion()));
                row.createCell(3).setCellValue(texto(dto.getDireccionIp()));
                row.createCell(4).setCellValue(texto(dto.getTablaAfectada()));
                row.createCell(5).setCellValue(texto(dto.getIdAfectado()));
                row.createCell(6).setCellValue(texto(dto.getCampoAfectado()));
                row.createCell(7).setCellValue(texto(dto.getAccion()));
                row.createCell(8).setCellValue(texto(dto.getDescripcion()));
                row.createCell(9).setCellValue(texto(dto.getRegistroAntiguo()));
                row.createCell(10).setCellValue(texto(dto.getRegistroNuevo()));
            }

            for (int i = 0; i < COLUMNAS_EXCEL.length; i++) {
                hoja.autoSizeColumn(i);
            }
            hoja.setColumnWidth(8, 60 * 256);
            hoja.setColumnWidth(9, 40 * 256);
            hoja.setColumnWidth(10, 40 * 256);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el reporte de auditoría en Excel.", ex);
        }
    }

    private void agregarCelda(PdfPTable tabla, String valor, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(valor, font));
        celda.setPadding(3);
        tabla.addCell(celda);
    }

    private String fecha(LocalDateTime valor) {
        return valor != null ? valor.format(FECHA) : "";
    }

    private String texto(String valor) {
        return valor != null ? valor : "";
    }

    private CellStyle estiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle estiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle estiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return style;
    }
}
