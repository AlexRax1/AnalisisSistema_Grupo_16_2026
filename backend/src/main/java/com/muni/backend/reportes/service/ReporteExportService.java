package com.muni.backend.reportes.service;

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
import com.muni.backend.reportes.dto.ReporteDetalleDTO;
import com.muni.backend.reportes.dto.ReporteFilaDTO;
import com.muni.backend.reportes.dto.ReporteKpiDTO;
import com.muni.backend.reportes.dto.ReporteResponseDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteExportService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarExcel(ReporteResponseDTO reporte) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titulo = estiloTitulo(workbook);
            CellStyle encabezado = estiloEncabezado(workbook);

            Sheet resumen = workbook.createSheet("Resumen");
            int fila = 0;
            Row tituloResumen = resumen.createRow(fila++);
            tituloResumen.createCell(0).setCellValue(reporte.titulo());
            tituloResumen.getCell(0).setCellStyle(titulo);
            resumen.createRow(fila++).createCell(0).setCellValue(reporte.subtitulo());
            resumen.createRow(fila++).createCell(0).setCellValue("Emitido: " + reporte.fechaEmision().format(FECHA));

            fila++;
            for (ReporteKpiDTO kpi : reporte.kpis()) {
                Row row = resumen.createRow(fila++);
                row.createCell(0).setCellValue(kpi.etiqueta());
                row.createCell(1).setCellValue(kpi.valor());
            }

            fila++;
            Row filtrosHeader = resumen.createRow(fila++);
            filtrosHeader.createCell(0).setCellValue("Filtros aplicados");
            filtrosHeader.getCell(0).setCellStyle(encabezado);
            for (var entry : reporte.filtrosAplicados().entrySet()) {
                Row row = resumen.createRow(fila++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            Sheet agrupado = workbook.createSheet("Agrupado");
            Row cabeceraAgrupado = agrupado.createRow(0);
            String[] columnasAgrupado = {"Dimensión", "Total", "Resueltas", "En proceso", "Rechazadas", "%"};
            for (int i = 0; i < columnasAgrupado.length; i++) {
                Cell cell = cabeceraAgrupado.createCell(i);
                cell.setCellValue(columnasAgrupado[i]);
                cell.setCellStyle(encabezado);
            }
            int filaAgrupado = 1;
            for (ReporteFilaDTO filaDto : reporte.tabla().filas()) {
                Row row = agrupado.createRow(filaAgrupado++);
                row.createCell(0).setCellValue(filaDto.etiqueta());
                row.createCell(1).setCellValue(filaDto.total());
                row.createCell(2).setCellValue(filaDto.cerradas());
                row.createCell(3).setCellValue(filaDto.enProceso());
                row.createCell(4).setCellValue(filaDto.rechazadas());
                row.createCell(5).setCellValue(filaDto.porcentaje());
            }

            Sheet detalle = workbook.createSheet("Detalle");
            Row cabeceraDetalle = detalle.createRow(0);
            String[] columnasDetalle = {"Correlativo", "Fecha", "Ciudadano", "DPI", "Categoría", "Estado", "Prioridad", "Inspector", "Especialista", "Dependencia"};
            for (int i = 0; i < columnasDetalle.length; i++) {
                Cell cell = cabeceraDetalle.createCell(i);
                cell.setCellValue(columnasDetalle[i]);
                cell.setCellStyle(encabezado);
            }
            int filaDetalle = 1;
            for (ReporteDetalleDTO detalleDto : reporte.detalle()) {
                Row row = detalle.createRow(filaDetalle++);
                row.createCell(0).setCellValue(texto(detalleDto.correlativo()));
                row.createCell(1).setCellValue(detalleDto.fechaRegistro() != null ? detalleDto.fechaRegistro().format(FECHA) : "");
                row.createCell(2).setCellValue(texto(detalleDto.ciudadano()));
                row.createCell(3).setCellValue(texto(detalleDto.dpi()));
                row.createCell(4).setCellValue(texto(detalleDto.categoria()));
                row.createCell(5).setCellValue(texto(detalleDto.estadoActual()));
                row.createCell(6).setCellValue(texto(detalleDto.prioridad()));
                row.createCell(7).setCellValue(texto(detalleDto.inspector()));
                row.createCell(8).setCellValue(texto(detalleDto.especialista()));
                row.createCell(9).setCellValue(texto(detalleDto.dependencia()));
            }

            for (int i = 0; i < columnasAgrupado.length; i++) {
                agrupado.autoSizeColumn(i);
            }
            for (int i = 0; i < columnasDetalle.length; i++) {
                detalle.autoSizeColumn(i);
            }
            for (int i = 0; i < 2; i++) {
                resumen.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el archivo Excel del reporte.", ex);
        }
    }

    public byte[] generarPdf(ReporteResponseDTO reporte) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font seccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);

            Paragraph tituloPdf = new Paragraph(reporte.titulo(), titulo);
            tituloPdf.setAlignment(Element.ALIGN_CENTER);
            document.add(tituloPdf);

            Paragraph subtituloPdf = new Paragraph(reporte.subtitulo() + "  |  Emitido: " + reporte.fechaEmision().format(FECHA), subtitulo);
            subtituloPdf.setAlignment(Element.ALIGN_CENTER);
            subtituloPdf.setSpacingAfter(14);
            document.add(subtituloPdf);

            PdfPTable kpis = new PdfPTable(3);
            kpis.setWidthPercentage(100);
            for (ReporteKpiDTO kpi : reporte.kpis()) {
                PdfPCell cell = new PdfPCell(new Phrase(kpi.etiqueta() + "\n" + kpi.valor(), texto));
                cell.setPadding(6);
                kpis.addCell(cell);
            }
            document.add(kpis);

            Paragraph filtros = new Paragraph("Filtros aplicados: " + reporte.filtrosAplicados(), subtitulo);
            filtros.setSpacingBefore(10);
            filtros.setSpacingAfter(10);
            document.add(filtros);

            document.add(new Paragraph(reporte.tabla().titulo(), seccion));
            document.add(new Paragraph(" ", subtitulo));

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{34, 10, 12, 12, 12, 8});
            for (String columna : reporte.tabla().columnas()) {
                PdfPCell cell = new PdfPCell(new Phrase(columna, encabezado));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setPadding(5);
                tabla.addCell(cell);
            }
            for (ReporteFilaDTO fila : reporte.tabla().filas()) {
                agregarCelda(tabla, fila.etiqueta(), texto);
                agregarCelda(tabla, String.valueOf(fila.total()), texto);
                agregarCelda(tabla, String.valueOf(fila.cerradas()), texto);
                agregarCelda(tabla, String.valueOf(fila.enProceso()), texto);
                agregarCelda(tabla, String.valueOf(fila.rechazadas()), texto);
                agregarCelda(tabla, String.format("%.1f%%", fila.porcentaje()), texto);
            }
            document.add(tabla);

            List<ReporteDetalleDTO> detalle = reporte.detalle().stream().limit(100).toList();
            if (!detalle.isEmpty()) {
                document.newPage();
                document.add(new Paragraph("Detalle de quejas (máximo 100 filas)", seccion));
                document.add(new Paragraph(" ", subtitulo));

                PdfPTable tablaDetalle = new PdfPTable(7);
                tablaDetalle.setWidthPercentage(100);
                tablaDetalle.setWidths(new float[]{14, 10, 20, 18, 14, 14, 10});
                for (String columna : List.of("Correlativo", "Fecha", "Ciudadano", "Categoría", "Estado", "Inspector", "Prioridad")) {
                    PdfPCell cell = new PdfPCell(new Phrase(columna, encabezado));
                    cell.setBackgroundColor(new Color(30, 58, 138));
                    cell.setPadding(4);
                    tablaDetalle.addCell(cell);
                }
                for (ReporteDetalleDTO fila : detalle) {
                    agregarCelda(tablaDetalle, texto(fila.correlativo()), texto);
                    agregarCelda(tablaDetalle, fila.fechaRegistro() != null ? fila.fechaRegistro().format(FECHA) : "", texto);
                    agregarCelda(tablaDetalle, texto(fila.ciudadano()), texto);
                    agregarCelda(tablaDetalle, texto(fila.categoria()), texto);
                    agregarCelda(tablaDetalle, texto(fila.estadoActual()), texto);
                    agregarCelda(tablaDetalle, texto(fila.inspector()), texto);
                    agregarCelda(tablaDetalle, texto(fila.prioridad()), texto);
                }
                document.add(tablaDetalle);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el archivo PDF del reporte.", ex);
        }
    }

    private void agregarCelda(PdfPTable tabla, String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setPadding(4);
        tabla.addCell(cell);
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
        style.setFont(font);
        return style;
    }

    private String texto(String valor) {
        return valor != null ? valor : "";
    }
}
