package com.muni.backend.reportes.service;

import com.muni.backend.reportes.dto.ReporteSerieDTO;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera las imágenes de los gráficos que se incrustan en las exportaciones
 * PDF y Excel. Se usa Java2D para no agregar dependencias externas: la
 * plantilla sigue funcionando aunque el proyecto se compile sin internet.
 *
 * La idea es que este servicio sea reutilizable por cualquier reporte que
 * devuelva {@link ReporteSerieDTO}. Si el reporte nuevo usa otra estructura,
 * basta con mapear sus datos a una lista de series antes de llamar aquí.
 */
@Service
public class ReporteChartService {

    private static final int ANCHO = 960;
    private static final int ALTO = 540;
    private static final int MAX_BARRAS = 8;
    private static final int MAX_PORCIONES = 7;

    private static final Color FONDO = Color.WHITE;
    private static final Color TEXTO = new Color(30, 41, 59);
    private static final Color TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color TRACK = new Color(226, 232, 240);
    private static final Color BORDE = new Color(203, 213, 225);

    private static final Color[] PALETA = {
            new Color(37, 99, 235),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(239, 68, 68),
            new Color(139, 92, 246),
            new Color(8, 145, 178),
            new Color(100, 116, 139),
            new Color(236, 72, 153)
    };

    // ---------------------------------------------------------------------
    // API pública
    // ---------------------------------------------------------------------

    public byte[] generarBarras(List<ReporteSerieDTO> series) {
        BufferedImage imagen = crearLienzo();
        Graphics2D g = imagen.createGraphics();
        try {
            configurar(g);
            dibujarTitulo(g, "Distribución por dimensión");

            List<ReporteSerieDTO> datos = agrupar(series, MAX_BARRAS);
            if (datos.isEmpty()) {
                dibujarSinDatos(g);
                return convertirAPng(imagen);
            }

            int izquierda = 260;
            int derecha = 90;
            int arriba = 110;
            int abajo = 45;
            int anchoBarra = ANCHO - izquierda - derecha;
            int altoDisponible = ALTO - arriba - abajo;
            int altoFila = Math.max(1, altoDisponible / datos.size());
            long maximo = datos.stream().mapToLong(ReporteSerieDTO::valor).max().orElse(1);
            Font etiqueta = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
            Font valor = new Font(Font.SANS_SERIF, Font.BOLD, 14);

            for (int i = 0; i < datos.size(); i++) {
                ReporteSerieDTO dato = datos.get(i);
                int y = arriba + i * altoFila;
                int altoBarra = Math.max(18, altoFila - 16);
                int yBarra = y + (altoFila - altoBarra) / 2;

                g.setFont(etiqueta);
                g.setColor(TEXTO);
                dibujarDerecha(g, recortar(dato.etiqueta(), 30), izquierda - 16,
                        yBarra + altoBarra / 2 + 6);

                g.setColor(TRACK);
                g.fillRoundRect(izquierda, yBarra, anchoBarra, altoBarra, 12, 12);

                int anchoValor = (int) Math.round(anchoBarra * (dato.valor() / (double) maximo));
                if (anchoValor > 0) {
                    g.setColor(PALETA[i % PALETA.length]);
                    g.fillRoundRect(izquierda, yBarra, anchoValor, altoBarra, 12, 12);
                }

                String textoValor = dato.valor() + "  (" + porcentaje(dato.porcentaje()) + ")";
                g.setFont(valor);
                g.setColor(TEXTO);
                int xValor = izquierda + Math.max(anchoValor, 0) + 12;
                if (xValor + g.getFontMetrics().stringWidth(textoValor) > ANCHO - 20) {
                    xValor = ANCHO - 20 - g.getFontMetrics().stringWidth(textoValor);
                }
                g.drawString(textoValor, xValor, yBarra + altoBarra / 2 + 5);
            }

            dibujarPiePagina(g, "Total de registros: " + suma(datos));
            return convertirAPng(imagen);
        } finally {
            g.dispose();
        }
    }

    public byte[] generarPastel(List<ReporteSerieDTO> series) {
        BufferedImage imagen = crearLienzo();
        Graphics2D g = imagen.createGraphics();
        try {
            configurar(g);
            dibujarTitulo(g, "Participación porcentual");

            List<ReporteSerieDTO> datos = agrupar(series, MAX_PORCIONES);
            long total = suma(datos);
            if (datos.isEmpty() || total <= 0) {
                dibujarSinDatos(g);
                return convertirAPng(imagen);
            }

            int diametro = 330;
            int x = 70;
            int y = 120;
            double inicio = 90;
            for (int i = 0; i < datos.size(); i++) {
                ReporteSerieDTO dato = datos.get(i);
                double angulo = 360.0 * dato.valor() / total;
                g.setColor(PALETA[i % PALETA.length]);
                g.fill(new Arc2D.Double(x, y, diametro, diametro, inicio, -angulo, Arc2D.PIE));
                inicio -= angulo;
            }

            // Aro blanco y borde para separar el pastel del fondo.
            g.setColor(FONDO);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x, y, diametro, diametro);
            g.setColor(BORDE);
            g.drawOval(x, y, diametro, diametro);

            int leyendaX = 470;
            int leyendaY = 130;
            Font nombre = new Font(Font.SANS_SERIF, Font.PLAIN, 17);
            Font detalle = new Font(Font.SANS_SERIF, Font.BOLD, 15);

            for (int i = 0; i < datos.size(); i++) {
                ReporteSerieDTO dato = datos.get(i);
                int filaY = leyendaY + i * 48;

                g.setColor(PALETA[i % PALETA.length]);
                g.fillRoundRect(leyendaX, filaY - 16, 18, 18, 5, 5);

                g.setColor(TEXTO);
                g.setFont(nombre);
                dibujarConRecorte(g, dato.etiqueta(), leyendaX + 30, filaY, 300);

                g.setColor(TEXTO_SUAVE);
                g.setFont(detalle);
                g.drawString(dato.valor() + "  (" + porcentaje(dato.porcentaje()) + ")",
                        leyendaX + 30, filaY + 22);
            }

            dibujarPiePagina(g, "Total de registros: " + total);
            return convertirAPng(imagen);
        } finally {
            g.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Utilidades de dibujo
    // ---------------------------------------------------------------------

    private BufferedImage crearLienzo() {
        BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        try {
            configurar(g);
            g.setColor(FONDO);
            g.fillRect(0, 0, ANCHO, ALTO);
        } finally {
            g.dispose();
        }
        return imagen;
    }

    private void configurar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void dibujarTitulo(Graphics2D g, String titulo) {
        g.setColor(FONDO);
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(TEXTO);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.drawString(titulo, 48, 58);
        g.setColor(BORDE);
        g.fillRect(48, 76, ANCHO - 96, 2);
    }

    private void dibujarPiePagina(Graphics2D g, String texto) {
        g.setColor(TEXTO_SUAVE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString(texto, 48, ALTO - 22);
    }

    private void dibujarSinDatos(Graphics2D g) {
        g.setColor(TEXTO_SUAVE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        String texto = "Sin datos para los filtros seleccionados";
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, (ANCHO - ancho) / 2, ALTO / 2);
    }

    private void dibujarDerecha(Graphics2D g, String texto, int xDerecha, int y) {
        FontMetrics metricas = g.getFontMetrics();
        g.drawString(texto, xDerecha - metricas.stringWidth(texto), y);
    }

    private void dibujarConRecorte(Graphics2D g, String texto, int x, int y, int anchoMaximo) {
        FontMetrics metricas = g.getFontMetrics();
        String valor = texto != null ? texto : "";
        while (metricas.stringWidth(valor) > anchoMaximo && valor.length() > 3) {
            valor = valor.substring(0, valor.length() - 4) + "...";
        }
        g.drawString(valor, x, y);
    }

    private byte[] convertirAPng(BufferedImage imagen) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la imagen del gráfico.", ex);
        }
    }

    // ---------------------------------------------------------------------
    // Transformación de datos
    // ---------------------------------------------------------------------

    /**
     * Agrupa las series que exceden el máximo en una porción "Otros" para que
     * el gráfico siga siendo legible. Si el reporte nuevo ya devuelve pocas
     * categorías, esta función no cambia nada.
     */
    private List<ReporteSerieDTO> agrupar(List<ReporteSerieDTO> series, int maximo) {
        List<ReporteSerieDTO> datos = series == null
                ? List.of()
                : series.stream()
                    .filter(dato -> dato != null && dato.valor() > 0)
                    .toList();

        if (datos.size() <= maximo) {
            return datos;
        }

        List<ReporteSerieDTO> resultado = new ArrayList<>(datos.subList(0, maximo - 1));
        long valorOtros = 0;
        double porcentajeOtros = 0;
        for (ReporteSerieDTO dato : datos.subList(maximo - 1, datos.size())) {
            valorOtros += dato.valor();
            porcentajeOtros += dato.porcentaje();
        }
        resultado.add(new ReporteSerieDTO("Otros", valorOtros, porcentajeOtros));
        return resultado;
    }

    private long suma(List<ReporteSerieDTO> datos) {
        return datos.stream().mapToLong(ReporteSerieDTO::valor).sum();
    }

    private String porcentaje(double valor) {
        return String.format(java.util.Locale.US, "%.1f%%", valor);
    }

    private String recortar(String valor, int maximo) {
        if (valor == null) {
            return "";
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo - 3) + "...";
    }
}
