package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Paleta y componentes visuales oficiales del sistema de diseño
 *
 */
public class TemaBurok {
    // Colores principales
    public static final Color PRIMARY = new Color(0x0B, 0x2D, 0x6B);           // #0B2D6B
    public static final Color HONDURAS_BLUE = new Color(0x00, 0x66, 0xCC);     // #0066CC
    public static final Color SKY_BLUE = new Color(0x7E, 0xC8, 0xFF);          // #7EC8FF
    public static final Color PRIMARY_CONTAINER = new Color(0xD6, 0xE8, 0xFF); // #D6E8FF

    // Superficies y texto
    public static final Color SURFACE = new Color(0xE8, 0xF4, 0xFF);           // #E8F4FF
    public static final Color SURFACE_CARD = Color.WHITE;
    public static final Color SURFACE_SUBTLE = new Color(0xD6, 0xE8, 0xFF);    // #D6E8FF
    public static final Color SURFACE_VARIANT = new Color(0xC4, 0xDE, 0xFC);   // #C4DEFC
    public static final Color TEXT_PRIMARY = new Color(0x2E, 0x3A, 0x59);      // #2E3A59
    public static final Color TEXT_MUTED = new Color(0x4A, 0x55, 0x68);        // #4A5568
    public static final Color OUTLINE = new Color(0xB8, 0xD5, 0xF2);           // #B8D5F2

    // Estados semánticos
    public static final Color SUCCESS = new Color(0x04, 0x78, 0x57);           // #047857
    public static final Color SUCCESS_CONTAINER = new Color(0xA7, 0xF3, 0xD0); // #A7F3D0
    public static final Color ON_SUCCESS = new Color(0x06, 0x4E, 0x3B);        // #064E3B

    public static final Color ACCENT = new Color(0xB4, 0x53, 0x09);            // #B45309
    public static final Color ACCENT_CONTAINER = new Color(0xFE, 0xF3, 0xC7);  // #FEF3C7
    public static final Color ON_ACCENT = new Color(0x78, 0x35, 0x0F);         // #78350F

    public static final Color DANGER = new Color(0xBE, 0x12, 0x3C);            // #BE123C
    public static final Color DANGER_CONTAINER = new Color(0xFB, 0xCF, 0xE8);  // #FBCFE8

    // Tipografía base
    public static final Font FONT_DISPLAY = new Font("SansSerif", Font.BOLD, 28);
    public static final Font FONT_HEADLINE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_CODE = new Font("Monospaced", Font.BOLD, 12);

    public static JPanel crearTarjeta(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(PRIMARY_CONTAINER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    public static JPanel crearTarjetaMetrica(String titulo, JLabel lblValor, String subtitulo, Color bgBadge, Color fgBadge) {
        JPanel card = crearTarjeta(new BorderLayout(0, 8));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel badge = new JLabel(subtitulo, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(bgBadge);
        badge.setForeground(fgBadge);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        top.add(badge, BorderLayout.EAST);

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(FONT_BODY);
        lblTit.setForeground(TEXT_MUTED);

        lblValor.setFont(FONT_DISPLAY);
        lblValor.setForeground(PRIMARY);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 2));
        body.setOpaque(false);
        body.add(lblTit);
        body.add(lblValor);

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    public static JButton crearBotonPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_LABEL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    public static JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(PRIMARY_CONTAINER);
        btn.setForeground(PRIMARY);
        btn.setFont(FONT_LABEL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        return btn;
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(34);
        tabla.setFont(FONT_BODY);
        tabla.setForeground(TEXT_PRIMARY);
        tabla.setGridColor(PRIMARY_CONTAINER);
        tabla.setSelectionBackground(PRIMARY_CONTAINER);
        tabla.setSelectionForeground(PRIMARY);
        tabla.setShowVerticalLines(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(0xEA, 0xF3, 0xFF));
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer padRenderer = new DefaultTableCellRenderer();
        padRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(padRenderer);
        }
    }

    public static JPanel crearBannerAccion(String titulo, String descripcion, JButton botonAccion) {
        JPanel banner = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), getHeight(), HONDURAS_BLUE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(126, 200, 255, 45));
                g2.fillOval(getWidth() - 180, -30, 220, 160);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4));
        textos.setOpaque(false);
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(FONT_HEADLINE);
        lblTit.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(0xD6, 0xE8, 0xFF));

        textos.add(lblTit);
        textos.add(lblDesc);

        botonAccion.setBackground(Color.WHITE);
        botonAccion.setForeground(PRIMARY);
        botonAccion.setFont(FONT_LABEL);
        botonAccion.setFocusPainted(false);
        botonAccion.setBorder(new EmptyBorder(10, 18, 10, 18));

        banner.add(textos, BorderLayout.CENTER);
        banner.add(botonAccion, BorderLayout.EAST);
        return banner;
    }
}
