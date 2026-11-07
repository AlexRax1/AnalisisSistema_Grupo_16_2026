package com.muni.backend.dashboard.service;

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
import com.muni.backend.dashboard.dto.DashboardContadorDTO;
import com.muni.backend.dashboard.dto.DashboardEstadoDTO;
import com.muni.backend.dashboard.dto.DashboardQuejaDTO;
import com.muni.backend.dashboard.dto.DashboardResponseDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exportaciones simples del dashboard. No depende del módulo de reportes:
 * genera un PDF y un Excel con los contadores, la tabla por estado y las
 * quejas recientes.
 */
@Service
public class DashboardExportService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarPdf(DashboardResponseDTO dashboard) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font seccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);

            Paragraph tituloPdf = new Paragraph("Dashboard de quejas", titulo);
            tituloPdf.setAlignment(Element.ALIGN_CENTER);
            document.add(tituloPdf);

            Paragraph subtituloPdf = new Paragraph(
                    "Actualizado: " + dashboard.fechaActualizacion().format(FECHA)
                            + "  |  Total de quejas: " + dashboard.total(),
                    subtitulo
            );
            subtituloPdf.setAlignment(Element.ALIGN_CENTER);
            subtituloPdf.setSpacingAfter(14);
            document.add(subtituloPdf);

            document.add(new Paragraph("Resumen por grupo", seccion));
            PdfPTable contadores = new PdfPTable(3);
            contadores.setWidthPercentage(100);
            contadores.setWidths(new float[]{40, 20, 20});
            contadores.addCell(celdaEncabezado("Grupo", encabezado));
            contadores.addCell(celdaEncabezado("Valor", encabezado));
            contadores.addCell(celdaEncabezado("Porcentaje", encabezado));
            for (DashboardContadorDTO contador : dashboard.contadores()) {
                contadores.addCell(celdaTexto(contador.etiqueta(), texto));
                contadores.addCell(celdaTexto(String.valueOf(contador.valor()), texto));
                contadores.addCell(celdaTexto(formatoPorcentaje(contador.porcentaje()), texto));
            }
            document.add(contadores);

            document.add(new Paragraph(" ", subtitulo));
            document.add(new Paragraph("Distribución por estado", seccion));
            PdfPTable estados = new PdfPTable(4);
            estados.setWidthPercentage(100);
            estados.setWidths(new float[]{38, 24, 18, 20});
            estados.addCell(celdaEncabezado("Estado", encabezado));
            estados.addCell(celdaEncabezado("Grupo", encabezado));
            estados.addCell(celdaEncabezado("Total", encabezado));
            estados.addCell(celdaEncabezado("Porcentaje", encabezado));
            for (DashboardEstadoDTO estado : dashboard.estados()) {
                estados.addCell(celdaEstado(estado.estado(), estado.grupo(), texto));
                estados.addCell(celdaEstado(estado.grupo(), estado.grupo(), texto));
                estados.addCell(celdaEstado(String.valueOf(estado.total()), estado.grupo(), texto));
                estados.addCell(celdaEstado(formatoPorcentaje(estado.porcentaje()), estado.grupo(), texto));
            }
            document.add(estados);

            List<DashboardQuejaDTO> recientes = dashboard.recientes().stream().limit(25).toList();
            if (!recientes.isEmpty()) {
                document.add(new Paragraph(" ", subtitulo));
                document.add(new Paragraph("Quejas recientes", seccion));
                PdfPTable tabla = new PdfPTable(7);
                tabla.setWidthPercentage(100);
                tabla.setWidths(new float[]{15, 11, 20, 20, 14, 10, 18});
                for (String columna : List.of("Correlativo", "Fecha", "Ciudadano", "Categoría", "Estado", "Prioridad", "Dependencia")) {
                    tabla.addCell(celdaEncabezado(columna, encabezado));
                }
                for (DashboardQuejaDTO queja : recientes) {
                    agregarCelda(tabla, texto(queja.correlativo()), texto);
                    agregarCelda(tabla, queja.fechaRegistro() != null ? queja.fechaRegistro().format(FECHA) : "", texto);
                    agregarCelda(tabla, texto(queja.ciudadano()), texto);
                    agregarCelda(tabla, texto(queja.categoria()), texto);
                    agregarCelda(tabla, texto(queja.estadoActual()), texto);
                    agregarCelda(tabla, texto(queja.prioridad()), texto);
                    agregarCelda(tabla, texto(queja.dependencia()), texto);
                }
                document.add(tabla);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el PDF del dashboard.", ex);
        }
    }

    public byte[] generarExcel(DashboardResponseDTO dashboard) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titulo = estiloTitulo(workbook);
            CellStyle encabezado = estiloEncabezado(workbook);

            Sheet resumen = workbook.createSheet("Resumen");
            int fila = 0;
            Row tituloResumen = resumen.createRow(fila++);
            tituloResumen.createCell(0).setCellValue("Dashboard de quejas");
            tituloResumen.getCell(0).setCellStyle(titulo);
            resumen.createRow(fila++).createCell(0)
                    .setCellValue("Actualizado: " + dashboard.fechaActualizacion().format(FECHA));
            resumen.createRow(fila++).createCell(0)
                    .setCellValue("Total de quejas: " + dashboard.total());

            fila++;
            Row cabeceraResumen = resumen.createRow(fila++);
            cabeceraResumen.createCell(0).setCellValue("Grupo");
            cabeceraResumen.createCell(1).setCellValue("Valor");
            cabeceraResumen.createCell(2).setCellValue("Porcentaje");
            cabeceraResumen.getCell(0).setCellStyle(encabezado);
            cabeceraResumen.getCell(1).setCellStyle(encabezado);
            cabeceraResumen.getCell(2).setCellStyle(encabezado);

            for (DashboardContadorDTO contador : dashboard.contadores()) {
                Row row = resumen.createRow(fila++);
                row.createCell(0).setCellValue(contador.etiqueta());
                row.createCell(1).setCellValue(contador.valor());
                row.createCell(2).setCellValue(contador.porcentaje());
            }

            Sheet estados = workbook.createSheet("Estados");
            Row cabeceraEstados = estados.createRow(0);
            String[] columnasEstados = {"Estado", "Grupo", "Total", "Porcentaje"};
            for (int i = 0; i < columnasEstados.length; i++) {
                Cell cell = cabeceraEstados.createCell(i);
                cell.setCellValue(columnasEstados[i]);
                cell.setCellStyle(encabezado);
            }
            int filaEstados = 1;
            for (DashboardEstadoDTO estado : dashboard.estados()) {
                Row row = estados.createRow(filaEstados++);
                CellStyle color = estiloEstado(workbook, estado.grupo());
                for (int i = 0; i < columnasEstados.length; i++) {
                    row.createCell(i).setCellStyle(color);
                }
                row.getCell(0).setCellValue(estado.estado());
                row.getCell(1).setCellValue(estado.grupo());
                row.getCell(2).setCellValue(estado.total());
                row.getCell(3).setCellValue(estado.porcentaje());
            }

            Sheet recientes = workbook.createSheet("Recientes");
            Row cabeceraRecientes = recientes.createRow(0);
            String[] columnasRecientes = {"Correlativo", "Fecha", "Ciudadano", "Categoría", "Estado", "Prioridad", "Dependencia"};
            for (int i = 0; i < columnasRecientes.length; i++) {
                Cell cell = cabeceraRecientes.createCell(i);
                cell.setCellValue(columnasRecientes[i]);
                cell.setCellStyle(encabezado);
            }
            int filaRecientes = 1;
            for (DashboardQuejaDTO queja : dashboard.recientes()) {
                Row row = recientes.createRow(filaRecientes++);
                row.createCell(0).setCellValue(texto(queja.correlativo()));
                row.createCell(1).setCellValue(queja.fechaRegistro() != null ? queja.fechaRegistro().format(FECHA) : "");
                row.createCell(2).setCellValue(texto(queja.ciudadano()));
                row.createCell(3).setCellValue(texto(queja.categoria()));
                row.createCell(4).setCellValue(texto(queja.estadoActual()));
                row.createCell(5).setCellValue(texto(queja.prioridad()));
                row.createCell(6).setCellValue(texto(queja.dependencia()));
            }

            for (int i = 0; i < 4; i++) {
                estados.autoSizeColumn(i);
            }
            for (int i = 0; i < columnasRecientes.length; i++) {
                recientes.autoSizeColumn(i);
            }
            for (int i = 0; i < 3; i++) {
                resumen.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el Excel del dashboard.", ex);
        }
    }

    private PdfPCell celdaEncabezado(String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor, font));
        cell.setBackgroundColor(new Color(30, 58, 138));
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell celdaTexto(String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell celdaEstado(String valor, String grupo, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setBackgroundColor(colorGrupo(grupo));
        cell.setPadding(5);
        return cell;
    }

    private void agregarCelda(PdfPTable tabla, String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setPadding(4);
        tabla.addCell(cell);
    }

    private Color colorGrupo(String grupo) {
        return switch (grupo != null ? grupo : "") {
            case "NUEVOS" -> new Color(212, 160, 23);
            case "EN_PROCESO" -> new Color(234, 88, 12);
            case "PENDIENTES" -> new Color(220, 38, 38);
            case "RESUELTOS" -> new Color(22, 163, 74);
            case "RECHAZADOS" -> new Color(153, 27, 27);
            default -> new Color(100, 116, 139);
        };
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
        font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
        style.setFont(font);
        return style;
    }

    private CellStyle estiloEstado(Workbook workbook, String grupo) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(indiceGrupo(grupo));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private short indiceGrupo(String grupo) {
        return switch (grupo != null ? grupo : "") {
            case "NUEVOS" -> IndexedColors.GOLD.getIndex();
            case "EN_PROCESO" -> IndexedColors.ORANGE.getIndex();
            case "PENDIENTES" -> IndexedColors.RED.getIndex();
            case "RESUELTOS" -> IndexedColors.GREEN.getIndex();
            case "RECHAZADOS" -> IndexedColors.DARK_RED.getIndex();
            default -> IndexedColors.GREY_50_PERCENT.getIndex();
        };
    }

    private String formatoPorcentaje(double valor) {
        return String.format(java.util.Locale.US, "%.1f%%", valor);
    }

    private String texto(String valor) {
        return valor != null ? valor : "";
    }
}
