package com.muni.backend.quejas.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.muni.backend.quejas.dto.QuejaDetalleDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.awt.Color;


@Service
public class PdfService {

    public byte[] generarConstanciaPdf(QuejaDetalleDTO queja) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Estilos de Fuente
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Encabezado
            Paragraph titulo = new Paragraph("MUNICIPALIDAD - SISTEMA DE QUEJAS", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("CONSTANCIA OFICIAL DE RECEPCIÓN Y TRAZABILIDAD", subtituloFont);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);

            // Tabla de Datos
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30, 70});

            agregarFila(table, "Correlativo:", queja.getCorrelativo(), labelFont, textFont);
            agregarFila(table, "Estado Actual:", queja.getEstadoActual(), labelFont, textFont);
            agregarFila(table, "Categoría:", queja.getCategoria() != null ? queja.getCategoria() : "N/A", labelFont, textFont);
            agregarFila(table, "Ubicación:", "Zona " + queja.getZona() + " - " + queja.getDireccionExacta(), labelFont, textFont);
            agregarFila(table, "Prioridad:", queja.getPrioridadConfirmada(), labelFont, textFont);

            if (queja.getFechaRegistro() != null) {
                agregarFila(table, "Fecha de Registro:", queja.getFechaRegistro().toString(), labelFont, textFont);
            }

            agregarFila(table, "Descripción:", queja.getDescripcion(), labelFont, textFont);

            document.add(table);

            // Nota al pie
            Font pieFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
            Paragraph pie = new Paragraph("\nDocumento generado automáticamente por el Portal Ciudadano. Código de verificación autogenerado.", pieFont);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setSpacingBefore(30);
            document.add(pie);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el documento PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void agregarFila(PdfPTable table, String label, String valor, Font labelFont, Font textFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBackgroundColor(new Color(240, 240, 240));
        cellLabel.setPadding(6);

        PdfPCell cellValor = new PdfPCell(new Phrase(valor != null ? valor : "", textFont));
        cellValor.setPadding(6);

        table.addCell(cellLabel);
        table.addCell(cellValor);
    }
}
