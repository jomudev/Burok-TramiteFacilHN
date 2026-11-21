package vista;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CanvasReporte2D extends JPanel {
    private Map<String, Integer> horasTradicionales = new LinkedHashMap<>();
    private Map<String, Integer> horasBurok = new LinkedHashMap<>();
    private Map<String, Integer> expedientesPorEstado = new LinkedHashMap<>();

    public CanvasReporte2D() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(920, 320));
    }

    public void setDatos(Map<String, Integer> trad, Map<String, Integer> burok, Map<String, Integer> estados) {
        this.horasTradicionales = trad != null ? trad : new LinkedHashMap<>();
        this.horasBurok = burok != null ? burok : new LinkedHashMap<>();
        this.expedientesPorEstado = estados != null ? estados : new LinkedHashMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int mitad = w / 2;

        // Fondo redondeado sutil
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
        g2.setColor(TemaBurok.PRIMARY_CONTAINER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

        // Divisor vertical
        g2.setColor(TemaBurok.PRIMARY_CONTAINER);
        g2.drawLine(mitad, 20, mitad, h - 20);

        dibujarGraficoHoras(g2, 24, 24, mitad - 48, h - 48);
        dibujarGraficoEstados(g2, mitad + 24, 24, mitad - 48, h - 48);

        g2.dispose();
    }

    private void dibujarGraficoHoras(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(TemaBurok.PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Horas Tradicionales vs. Burok por Entidad", x, y + 14);

        // Leyenda
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(0xF8, 0x71, 0x71));
        g2.fillRect(x, y + 24, 10, 10);
        g2.setColor(TemaBurok.TEXT_MUTED);
        g2.drawString("Tradicional (1,933h)", x + 15, y + 33);

        g2.setColor(TemaBurok.HONDURAS_BLUE);
        g2.fillRect(x + 145, y + 24, 10, 10);
        g2.setColor(TemaBurok.TEXT_MUTED);
        g2.drawString("Burok Optimizado", x + 160, y + 33);

        if (horasTradicionales.isEmpty()) return;

        int maxVal = 1;
        for (int v : horasTradicionales.values()) {
            if (v > maxVal) maxVal = v;
        }

        int baseY = y + h - 30;
        int areaH = h - 85;
        int n = horasTradicionales.size();
        int grupoW = Math.max(40, w / Math.max(1, n));
        int barW = Math.max(12, (grupoW - 22) / 2);

        g2.setColor(TemaBurok.OUTLINE);
        g2.drawLine(x, baseY, x + w, baseY);

        int idx = 0;
        for (Map.Entry<String, Integer> entry : horasTradicionales.entrySet()) {
            String entidad = entry.getKey();
            int vTrad = entry.getValue();
            int vBur = horasBurok.getOrDefault(entidad, 0);

            int gx = x + idx * grupoW + 10;
            int hTrad = (int) ((vTrad * 1.0 / maxVal) * areaH);
            int hBur = (int) ((vBur * 1.0 / maxVal) * areaH);

            g2.setColor(new Color(0xF8, 0x71, 0x71));
            g2.fillRoundRect(gx, baseY - hTrad, barW, hTrad, 6, 6);
            g2.setColor(TemaBurok.TEXT_PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(vTrad + "h", gx, baseY - hTrad - 4);

            g2.setColor(TemaBurok.HONDURAS_BLUE);
            g2.fillRoundRect(gx + barW + 4, baseY - Math.max(4, hBur), barW, Math.max(4, hBur), 6, 6);
            g2.setColor(TemaBurok.PRIMARY);
            g2.drawString(vBur + "h", gx + barW + 4, baseY - Math.max(4, hBur) - 4);

            String label = entidad.length() > 11 ? entidad.substring(0, 10) + "." : entidad;
            g2.setColor(TemaBurok.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(label, gx, baseY + 14);
            idx++;
        }
    }

    private void dibujarGraficoEstados(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(TemaBurok.PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Expedientes por Estado", x, y + 14);

        if (expedientesPorEstado.isEmpty()) return;

        int maxVal = 1;
        for (int v : expedientesPorEstado.values()) {
            if (v > maxVal) maxVal = v;
        }

        int baseY = y + h - 30;
        int areaH = h - 85;
        int n = expedientesPorEstado.size();
        int colW = Math.max(50, w / Math.max(1, n));
        int barW = Math.min(44, colW - 24);

        g2.setColor(TemaBurok.OUTLINE);
        g2.drawLine(x, baseY, x + w, baseY);

        Color[] paleta = {
                TemaBurok.ACCENT,
                TemaBurok.HONDURAS_BLUE,
                TemaBurok.SKY_BLUE,
                TemaBurok.SUCCESS
        };

        int idx = 0;
        for (Map.Entry<String, Integer> entry : expedientesPorEstado.entrySet()) {
            int val = entry.getValue();
            int barH = (int) ((val * 1.0 / maxVal) * areaH);
            int bx = x + idx * colW + (colW - barW) / 2;

            g2.setColor(paleta[idx % paleta.length]);
            g2.fillRoundRect(bx, baseY - Math.max(4, barH), barW, Math.max(4, barH), 8, 8);

            g2.setColor(TemaBurok.PRIMARY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(String.valueOf(val), bx + barW / 2 - 4, baseY - Math.max(4, barH) - 6);

            String lbl = entry.getKey();
            if (lbl.length() > 12) lbl = lbl.substring(0, 11) + ".";
            g2.setColor(TemaBurok.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(lbl, bx - 4, baseY + 14);
            idx++;
        }
    }
}
