package vista;

import conexion.ConexionMySQL;
import controlador.BurokControlador;
import excepciones.DocumentoFaltanteException;
import excepciones.EmpresaNoEncontradaException;
import excepciones.PresupuestoInsuficienteException;
import hilos.ProcesoVentanillaHilo;
import modelo.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Ventana principal Swing de TrámiteFácil HN (Burok).
 * Implementa el diseño Stitch (Topbar + Sidebar + CardLayout) sobre MVC + MySQL.
 */
public class VentanaPrincipal extends JFrame {
    private final BurokControlador controlador;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    // Botones de navegación lateral
    private JButton btnNavDashboard, btnNavEmpresas, btnNavTramites, btnNavSeguimiento, btnNavReportes;

    // Métricas del Panel Principal
    private final JLabel lblMetricaEmpresas = new JLabel("00");
    private final JLabel lblMetricaPendientes = new JLabel("00");
    private final JLabel lblMetricaCompletados = new JLabel("00");
    private final JLabel lblMetricaHoras = new JLabel("00 hrs");

    // Tablas principales
    private DefaultTableModel modelResumenEmpresas, modelEmprendedores, modelEmpresas, modelCatalogo, modelDocumentos, modelSeguimiento, modelLiquidaciones;
    private JTable tblResumenEmpresas, tblEmprendedores, tblEmpresas, tblCatalogo, tblDocumentos, tblSeguimiento, tblLiquidaciones;

    // Formularios Emprendedores y Empresas
    private JTextField txtEmpCod, txtEmpNom, txtEmpDni, txtEmpTel, txtEmpCorreo, txtEmpDir;
    private JTextField txtPymCod, txtPymNom, txtPymRtn, txtPymCap, txtBuscarEmpresa;
    private JComboBox<Emprendedor> cmbPymProp;
    private JComboBox<String> cmbPymForma, cmbPymCiiu, cmbPymMun, cmbFiltroMun;
    private JComboBox<Integer> cmbPymRiesgo;

    // Trámites & Expediente Único
    private JComboBox<Empresa> cmbEmpresaActiva;
    private JProgressBar pbApertura, pbFuncionamiento, pbCierre;
    private JLabel lblPctApertura, lblPctFuncionamiento, lblPctCierre;
    private JLabel lblEstadoDocs;

    // Seguimiento & Multihilo
    private JComboBox<ExpedienteFormalizacion> cmbExpSeguimiento;
    private JProgressBar pbHiloVentanilla;
    private JLabel lblEstadoHilo;
    private JButton btnIniciarHilo;

    // Reportes & Liquidación
    private JComboBox<ExpedienteFormalizacion> cmbExpLiquidar;
    private JLabel lblCostoTotalExp, lblSubMun, lblSubCen, lblAhorroHoras;
    private JTextField txtMontoRecibido;
    private CanvasReporte2D canvasReporte;

    public VentanaPrincipal() {
        this.controlador = new BurokControlador();
        setTitle("TrámiteFácil HN (Burok) · Gestión de Formalización PyME Honduras");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        construirUI();
        cargarTodosLosDatos();
        mostrarVista("PANEL", btnNavDashboard);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(TemaBurok.SURFACE);

        root.add(crearBarraSuperior(), BorderLayout.NORTH);
        root.add(crearSidebar(), BorderLayout.WEST);

        panelContenido.setBackground(TemaBurok.SURFACE);
        panelContenido.setBorder(new EmptyBorder(20, 24, 20, 24));
        panelContenido.add(crearVistaDashboard(), "PANEL");
        panelContenido.add(crearVistaMisEmpresas(), "EMPRESAS");
        panelContenido.add(crearVistaTramites(), "TRAMITES");
        panelContenido.add(crearVistaSeguimiento(), "SEGUIMIENTO");
        panelContenido.add(crearVistaReportes(), "REPORTES");

        root.add(panelContenido, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel crearBarraSuperior() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaBurok.PRIMARY_CONTAINER),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel lblApp = new JLabel("Proyecto: TrámiteFácil HN (Burok)  ·  v1.2.0-MVP (Java 17 + MySQL)");
        lblApp.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblApp.setForeground(TemaBurok.TEXT_MUTED);

        boolean conectado = ConexionMySQL.verificarConexion();
        JLabel lblMysql = new JLabel(conectado ? "● MySQL Conectado (localhost:3306 / burok_db)" : "● MySQL Desconectado");
        lblMysql.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMysql.setForeground(conectado ? TemaBurok.SUCCESS : TemaBurok.DANGER);

        top.add(lblApp, BorderLayout.WEST);
        top.add(lblMysql, BorderLayout.EAST);
        return top;
    }

    private JPanel crearSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(245, 0));
        side.setBackground(Color.WHITE);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TemaBurok.PRIMARY_CONTAINER),
                new EmptyBorder(22, 16, 20, 16)
        ));

        // Marca
        JPanel brand = new JPanel(new GridLayout(2, 1, 0, 2));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(0, 6, 20, 6));
        JLabel lblBrand = new JLabel("✦ TrámiteFácil HN");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblBrand.setForeground(TemaBurok.PRIMARY);
        JLabel lblSub = new JLabel("GESTIÓN PARA PYMES · BUROK");
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblSub.setForeground(TemaBurok.HONDURAS_BLUE);
        brand.add(lblBrand);
        brand.add(lblSub);

        // Navegación
        JPanel nav = new JPanel(new GridLayout(5, 1, 0, 8));
        nav.setOpaque(false);
        btnNavDashboard = crearBotonNav("Panel principal", "PANEL");
        btnNavEmpresas = crearBotonNav("Mis empresas", "EMPRESAS");
        btnNavTramites = crearBotonNav("Trámites y Exp. Único", "TRAMITES");
        btnNavSeguimiento = crearBotonNav("Seguimiento y SLA", "SEGUIMIENTO");
        btnNavReportes = crearBotonNav("Reportes y Java 2D", "REPORTES");

        nav.add(btnNavDashboard);
        nav.add(btnNavEmpresas);
        nav.add(btnNavTramites);
        nav.add(btnNavSeguimiento);
        nav.add(btnNavReportes);

        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setOpaque(false);
        topWrap.add(brand, BorderLayout.NORTH);
        topWrap.add(nav, BorderLayout.CENTER);

        // Tarjeta Consejo del Sidebar
        JPanel consejo = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TemaBurok.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(TemaBurok.OUTLINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        consejo.setOpaque(false);
        consejo.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel lblTipTit = new JLabel("★ CONSEJO BUROK");
        lblTipTit.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTipTit.setForeground(TemaBurok.PRIMARY);
        JLabel lblTipBody = new JLabel("<html>Organiza tus gestiones con el Expediente Único y monitorea el Silencio Positivo (15 días).</html>");
        lblTipBody.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTipBody.setForeground(TemaBurok.TEXT_PRIMARY);
        consejo.add(lblTipTit, BorderLayout.NORTH);
        consejo.add(lblTipBody, BorderLayout.CENTER);

        side.add(topWrap, BorderLayout.NORTH);
        side.add(consejo, BorderLayout.SOUTH);
        return side;
    }

    private JButton crearBotonNav(String texto, String vistaId) {
        JButton btn = new JButton(texto);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(11, 14, 11, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> mostrarVista(vistaId, btn));
        return btn;
    }

    private void mostrarVista(String vistaId, JButton activo) {
        JButton[] todos = {btnNavDashboard, btnNavEmpresas, btnNavTramites, btnNavSeguimiento, btnNavReportes};
        for (JButton b : todos) {
            b.setBackground(Color.WHITE);
            b.setForeground(TemaBurok.TEXT_MUTED);
        }
        if (activo != null) {
            activo.setBackground(TemaBurok.PRIMARY_CONTAINER);
            activo.setForeground(TemaBurok.PRIMARY);
        }
        cardLayout.show(panelContenido, vistaId);
        if ("REPORTES".equals(vistaId)) {
            actualizarGrafico2D();
        }
    }

    // --- VISTA 1: PANEL PRINCIPAL ---
    private JPanel crearVistaDashboard() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titulos = new JPanel(new GridLayout(3, 1, 0, 2));
        titulos.setOpaque(false);
        JLabel bread = new JLabel("INICIO / PANEL PRINCIPAL");
        bread.setFont(new Font("SansSerif", Font.BOLD, 11));
        bread.setForeground(TemaBurok.HONDURAS_BLUE);
        JLabel h2 = new JLabel("¡Hola, emprendedor!");
        h2.setFont(TemaBurok.FONT_DISPLAY);
        h2.setForeground(TemaBurok.PRIMARY);
        JLabel sub = new JLabel("Resumen de tus empresas registradas en MySQL y avance frente a las 1,933 horas promedio.");
        sub.setFont(TemaBurok.FONT_BODY);
        sub.setForeground(TemaBurok.TEXT_MUTED);
        titulos.add(bread);
        titulos.add(h2);
        titulos.add(sub);

        JButton btnNueva = TemaBurok.crearBotonPrimario("+ Nueva empresa");
        btnNueva.addActionListener(e -> mostrarVista("EMPRESAS", btnNavEmpresas));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(btnNueva);

        header.add(titulos, BorderLayout.CENTER);
        header.add(btnWrap, BorderLayout.EAST);

        // 4 Tarjetas de métricas
        JPanel metricas = new JPanel(new GridLayout(1, 4, 14, 0));
        metricas.setOpaque(false);
        metricas.add(TemaBurok.crearTarjetaMetrica("Empresas registradas", lblMetricaEmpresas, "MySQL", TemaBurok.PRIMARY_CONTAINER, TemaBurok.PRIMARY));
        metricas.add(TemaBurok.crearTarjetaMetrica("Trámites pendientes", lblMetricaPendientes, "En cola", TemaBurok.ACCENT_CONTAINER, TemaBurok.ON_ACCENT));
        metricas.add(TemaBurok.crearTarjetaMetrica("Trámites completados", lblMetricaCompletados, "Listos", TemaBurok.SUCCESS_CONTAINER, TemaBurok.ON_SUCCESS));
        metricas.add(TemaBurok.crearTarjetaMetrica("Horas estimadas Burok", lblMetricaHoras, "-92% tiempo", TemaBurok.PRIMARY_CONTAINER, TemaBurok.HONDURAS_BLUE));

        // Tabla de Expedientes de Empresas + Filtro
        JPanel cardTabla = TemaBurok.crearTarjeta(new BorderLayout(0, 12));
        JPanel barFiltro = new JPanel(new BorderLayout(10, 0));
        barFiltro.setOpaque(false);
        JLabel lblTitTbl = new JLabel("Lista de Expedientes de Empresas");
        lblTitTbl.setFont(TemaBurok.FONT_HEADLINE);
        lblTitTbl.setForeground(TemaBurok.PRIMARY);

        JPanel controlesFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlesFiltro.setOpaque(false);
        txtBuscarEmpresa = new JTextField(16);
        txtBuscarEmpresa.setToolTipText("Buscar por código de empresa");
        JButton btnBuscar = TemaBurok.crearBotonSecundario("Buscar Código");
        btnBuscar.addActionListener(e -> ejecutarBusquedaEmpresa());
        cmbFiltroMun = new JComboBox<>(new String[]{"Todos los municipios", "Distrito Central (Tegucigalpa)", "San Pedro Sula", "Choloma", "Villanueva (Cortés)", "La Ceiba / Roatán"});
        cmbFiltroMun.addActionListener(e -> refrescarTablaResumen());
        controlesFiltro.add(txtBuscarEmpresa);
        controlesFiltro.add(btnBuscar);
        controlesFiltro.add(cmbFiltroMun);

        barFiltro.add(lblTitTbl, BorderLayout.WEST);
        barFiltro.add(controlesFiltro, BorderLayout.EAST);

        modelResumenEmpresas = new DefaultTableModel(new String[]{"Código", "Empresa / RTN", "Forma Jurídica", "Municipio", "CIIU Rev. 4", "Avance", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblResumenEmpresas = new JTable(modelResumenEmpresas);
        TemaBurok.estilizarTabla(tblResumenEmpresas);
        cardTabla.add(barFiltro, BorderLayout.NORTH);
        cardTabla.add(new JScrollPane(tblResumenEmpresas), BorderLayout.CENTER);

        JButton btnIrTramites = new JButton("Ir a Ruta Crítica →");
        btnIrTramites.addActionListener(e -> mostrarVista("TRAMITES", btnNavTramites));
        JPanel banner = TemaBurok.crearBannerAccion(
                "Un paso a la vez ✨",
                "El Índice de Burocracia 2026 estima 1,933 horas para formalizar una PyME en Honduras. Burok optimiza tu ruta crítica.",
                btnIrTramites
        );

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(metricas, BorderLayout.NORTH);
        centro.add(cardTabla, BorderLayout.CENTER);

        p.add(header, BorderLayout.NORTH);
        p.add(centro, BorderLayout.CENTER);
        p.add(banner, BorderLayout.SOUTH);
        return p;
    }

    // --- VISTA 2: MIS EMPRESAS Y EMPRENDEDORES (MÓDULOS 1, 2, 8) ---
    private JPanel crearVistaMisEmpresas() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JLabel tit = new JLabel("Gestión de Emprendedores y Empresas PyME (MySQL + Stack Undo)");
        tit.setFont(TemaBurok.FONT_HEADLINE);
        tit.setForeground(TemaBurok.PRIMARY);
        p.add(tit, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 16, 0));
        grid.setOpaque(false);

        // Panel Izquierdo: Emprendedores
        JPanel cardEmp = TemaBurok.crearTarjeta(new BorderLayout(0, 10));
        JPanel formEmp = new JPanel(new GridLayout(6, 2, 8, 6));
        formEmp.setOpaque(false);
        txtEmpCod = new JTextField("EMP-003");
        txtEmpNom = new JTextField();
        txtEmpDni = new JTextField();
        txtEmpTel = new JTextField();
        txtEmpCorreo = new JTextField();
        txtEmpDir = new JTextField();
        formEmp.add(new JLabel("Código Emprendedor:")); formEmp.add(txtEmpCod);
        formEmp.add(new JLabel("Nombre Completo:")); formEmp.add(txtEmpNom);
        formEmp.add(new JLabel("DNI (RNP):")); formEmp.add(txtEmpDni);
        formEmp.add(new JLabel("Teléfono:")); formEmp.add(txtEmpTel);
        formEmp.add(new JLabel("Correo:")); formEmp.add(txtEmpCorreo);
        formEmp.add(new JLabel("Dirección:")); formEmp.add(txtEmpDir);

        JPanel accEmp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accEmp.setOpaque(false);
        JButton btnGuardarEmp = TemaBurok.crearBotonPrimario("Registrar Emprendedor");
        JButton btnUndoEmp = TemaBurok.crearBotonSecundario("Deshacer (Undo)");
        btnGuardarEmp.addActionListener(e -> registrarEmprendedorUI());
        btnUndoEmp.addActionListener(e -> deshacerEmprendedorUI());
        accEmp.add(btnGuardarEmp);
        accEmp.add(btnUndoEmp);

        JPanel topEmp = new JPanel(new BorderLayout(0, 8));
        topEmp.setOpaque(false);
        JLabel l1 = new JLabel("1. Emprendedores / Representantes");
        l1.setFont(TemaBurok.FONT_TITLE);
        l1.setForeground(TemaBurok.PRIMARY);
        topEmp.add(l1, BorderLayout.NORTH);
        topEmp.add(formEmp, BorderLayout.CENTER);
        topEmp.add(accEmp, BorderLayout.SOUTH);

        modelEmprendedores = new DefaultTableModel(new String[]{"Código", "Nombre", "DNI", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEmprendedores = new JTable(modelEmprendedores);
        TemaBurok.estilizarTabla(tblEmprendedores);
        cardEmp.add(topEmp, BorderLayout.NORTH);
        cardEmp.add(new JScrollPane(tblEmprendedores), BorderLayout.CENTER);

        // Panel Derecho: Empresas y CIIU
        JPanel cardPym = TemaBurok.crearTarjeta(new BorderLayout(0, 10));
        JPanel formPym = new JPanel(new GridLayout(8, 2, 8, 6));
        formPym.setOpaque(false);
        txtPymCod = new JTextField("PYM-004");
        txtPymNom = new JTextField();
        txtPymRtn = new JTextField();
        cmbPymProp = new JComboBox<>();
        cmbPymForma = new JComboBox<>(new String[]{"Comerciante Individual", "Sociedad de Resp. Ltda.", "Sociedad Anónima (S.A.)"});
        cmbPymCiiu = new JComboBox<>(new String[]{"5610 - Restaurantes y Cafés", "4711 - Supermercados", "5229 - Logística y Carga", "6201 - Desarrollo de Software", "4771 - Comercio de Prendas"});
        cmbPymRiesgo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cmbPymMun = new JComboBox<>(new String[]{"Distrito Central (Tegucigalpa)", "San Pedro Sula", "Choloma", "Villanueva (Cortés)", "La Ceiba / Roatán"});
        txtPymCap = new JTextField("85000");

        formPym.add(new JLabel("Código Empresa:")); formPym.add(txtPymCod);
        formPym.add(new JLabel("Nombre Comercial:")); formPym.add(txtPymNom);
        formPym.add(new JLabel("RTN:")); formPym.add(txtPymRtn);
        formPym.add(new JLabel("Propietario:")); formPym.add(cmbPymProp);
        formPym.add(new JLabel("Forma Jurídica:")); formPym.add(cmbPymForma);
        formPym.add(new JLabel("Actividad CIIU:")); formPym.add(cmbPymCiiu);
        formPym.add(new JLabel("Cat. Riesgo (1-4) / Mun:"));
        JPanel mix = new JPanel(new GridLayout(1, 2, 4, 0));
        mix.setOpaque(false);
        mix.add(cmbPymRiesgo);
        mix.add(cmbPymMun);
        formPym.add(mix);
        formPym.add(new JLabel("Capital Semilla (L.):")); formPym.add(txtPymCap);

        JPanel accPym = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accPym.setOpaque(false);
        JButton btnGuardarPym = TemaBurok.crearBotonPrimario("Registrar Empresa");
        JButton btnUndoPym = TemaBurok.crearBotonSecundario("Deshacer (Undo)");
        btnGuardarPym.addActionListener(e -> registrarEmpresaUI());
        btnUndoPym.addActionListener(e -> deshacerEmpresaUI());
        accPym.add(btnGuardarPym);
        accPym.add(btnUndoPym);

        JPanel topPym = new JPanel(new BorderLayout(0, 8));
        topPym.setOpaque(false);
        JLabel l2 = new JLabel("2. Empresas y Clasificación CIIU");
        l2.setFont(TemaBurok.FONT_TITLE);
        l2.setForeground(TemaBurok.PRIMARY);
        topPym.add(l2, BorderLayout.NORTH);
        topPym.add(formPym, BorderLayout.CENTER);
        topPym.add(accPym, BorderLayout.SOUTH);

        modelEmpresas = new DefaultTableModel(new String[]{"Código", "Empresa", "Municipio", "Riesgo", "Capital (L.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEmpresas = new JTable(modelEmpresas);
        TemaBurok.estilizarTabla(tblEmpresas);
        cardPym.add(topPym, BorderLayout.NORTH);
        cardPym.add(new JScrollPane(tblEmpresas), BorderLayout.CENTER);

        grid.add(cardEmp);
        grid.add(cardPym);
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    // --- VISTA 3: TRÁMITES Y EXPEDIENTE ÚNICO (MÓDULOS 3 Y 4) ---
    private JPanel crearVistaTramites() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        // Header con selector de empresa activa
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        JLabel tit = new JLabel("Ruta Crítica de Trámites y Expediente Único Digital");
        tit.setFont(TemaBurok.FONT_HEADLINE);
        tit.setForeground(TemaBurok.PRIMARY);

        JPanel sel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sel.setOpaque(false);
        cmbEmpresaActiva = new JComboBox<>();
        cmbEmpresaActiva.addActionListener(e -> refrescarVistaTramitesPorEmpresa());
        JButton btnGenerarRuta = TemaBurok.crearBotonPrimario("Generar / Abrir Expediente");
        btnGenerarRuta.addActionListener(e -> generarRutaCriticaUI());
        sel.add(new JLabel("Empresa activa:"));
        sel.add(cmbEmpresaActiva);
        sel.add(btnGenerarRuta);
        header.add(tit, BorderLayout.WEST);
        header.add(sel, BorderLayout.EAST);

        // Centro: Progreso por etapa + Expediente Único
        JPanel topGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        topGrid.setOpaque(false);

        JPanel cardProgreso = TemaBurok.crearTarjeta(new GridLayout(4, 1, 0, 8));
        JLabel lblProg = new JLabel("Progreso por Etapa (Apertura / Funcionamiento / Cierre)");
        lblProg.setFont(TemaBurok.FONT_TITLE);
        lblProg.setForeground(TemaBurok.PRIMARY);
        pbApertura = new JProgressBar(0, 100); pbApertura.setForeground(TemaBurok.HONDURAS_BLUE);
        pbFuncionamiento = new JProgressBar(0, 100); pbFuncionamiento.setForeground(TemaBurok.SKY_BLUE);
        pbCierre = new JProgressBar(0, 100); pbCierre.setForeground(TemaBurok.ACCENT);
        lblPctApertura = new JLabel("Apertura: 0%");
        lblPctFuncionamiento = new JLabel("Funcionamiento: 0%");
        lblPctCierre = new JLabel("Cierre: 0%");
        cardProgreso.add(lblProg);
        cardProgreso.add(envolverBarra(lblPctApertura, pbApertura));
        cardProgreso.add(envolverBarra(lblPctFuncionamiento, pbFuncionamiento));
        cardProgreso.add(envolverBarra(lblPctCierre, pbCierre));

        JPanel cardDocs = TemaBurok.crearTarjeta(new BorderLayout(0, 8));
        JPanel docTop = new JPanel(new BorderLayout());
        docTop.setOpaque(false);
        JLabel lblDocTit = new JLabel("Expediente Único Digital (Cero Duplicidad)");
        lblDocTit.setFont(TemaBurok.FONT_TITLE);
        lblDocTit.setForeground(TemaBurok.PRIMARY);
        lblEstadoDocs = new JLabel("Seleccione empresa");
        lblEstadoDocs.setFont(TemaBurok.FONT_LABEL);
        JButton btnToggleDoc = TemaBurok.crearBotonSecundario("Validar / Invalidar Seleccionado");
        btnToggleDoc.addActionListener(e -> alternarValidacionDocumentoUI());
        docTop.add(lblDocTit, BorderLayout.WEST);
        docTop.add(lblEstadoDocs, BorderLayout.EAST);

        modelDocumentos = new DefaultTableModel(new String[]{"ID", "Código", "Documento Maestro", "Emisor", "Validado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDocumentos = new JTable(modelDocumentos);
        TemaBurok.estilizarTabla(tblDocumentos);
        cardDocs.add(docTop, BorderLayout.NORTH);
        cardDocs.add(new JScrollPane(tblDocumentos), BorderLayout.CENTER);
        cardDocs.add(btnToggleDoc, BorderLayout.SOUTH);

        topGrid.add(cardProgreso);
        topGrid.add(cardDocs);

        // Catálogo Regulatorio Nacional
        JPanel cardCat = TemaBurok.crearTarjeta(new BorderLayout(0, 8));
        JLabel lblCat = new JLabel("Catálogo Regulatorio Nacional (IP, SAR, Alcaldía, MiAmbiente, ARSA)");
        lblCat.setFont(TemaBurok.FONT_TITLE);
        lblCat.setForeground(TemaBurok.PRIMARY);
        modelCatalogo = new DefaultTableModel(new String[]{"Código", "Entidad", "Etapa", "Trámite", "Hrs Tradicional", "Hrs Burok", "Costo (L.)", "SLA (Días)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCatalogo = new JTable(modelCatalogo);
        TemaBurok.estilizarTabla(tblCatalogo);
        cardCat.add(lblCat, BorderLayout.NORTH);
        cardCat.add(new JScrollPane(tblCatalogo), BorderLayout.CENTER);

        p.add(header, BorderLayout.NORTH);
        JPanel mid = new JPanel(new GridLayout(2, 1, 0, 14));
        mid.setOpaque(false);
        mid.add(topGrid);
        mid.add(cardCat);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private JPanel envolverBarra(JLabel lbl, JProgressBar pb) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        lbl.setFont(TemaBurok.FONT_BODY);
        pb.setPreferredSize(new Dimension(0, 10));
        p.add(lbl, BorderLayout.NORTH);
        p.add(pb, BorderLayout.CENTER);
        return p;
    }

    // --- VISTA 4: SEGUIMIENTO SLA Y MULTIHILO (MÓDULOS 5 Y 10) ---
    private JPanel crearVistaSeguimiento() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JPanel top = TemaBurok.crearTarjeta(new BorderLayout(12, 10));
        JLabel tit = new JLabel("Simulador Interinstitucional y Silencio Administrativo Positivo (Multihilo)");
        tit.setFont(TemaBurok.FONT_HEADLINE);
        tit.setForeground(TemaBurok.PRIMARY);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ctrl.setOpaque(false);
        cmbExpSeguimiento = new JComboBox<>();
        cmbExpSeguimiento.addActionListener(e -> refrescarTablaSeguimiento());
        btnIniciarHilo = TemaBurok.crearBotonPrimario("Simular Ventanillas y SLA (>= 5 seg)");
        btnIniciarHilo.addActionListener(e -> ejecutarHiloVentanillaUI());
        ctrl.add(new JLabel("Expediente:"));
        ctrl.add(cmbExpSeguimiento);
        ctrl.add(btnIniciarHilo);

        pbHiloVentanilla = new JProgressBar(0, 100);
        pbHiloVentanilla.setStringPainted(true);
        pbHiloVentanilla.setForeground(TemaBurok.HONDURAS_BLUE);
        pbHiloVentanilla.setPreferredSize(new Dimension(0, 22));
        lblEstadoHilo = new JLabel("Listo para simular recorrido en ventanillas (IP -> SAR -> Alcaldía -> MiAmbiente/ARSA).");
        lblEstadoHilo.setFont(TemaBurok.FONT_BODY);
        lblEstadoHilo.setForeground(TemaBurok.TEXT_MUTED);

        JPanel barraWrap = new JPanel(new GridLayout(2, 1, 0, 4));
        barraWrap.setOpaque(false);
        barraWrap.add(pbHiloVentanilla);
        barraWrap.add(lblEstadoHilo);

        top.add(tit, BorderLayout.NORTH);
        top.add(ctrl, BorderLayout.CENTER);
        top.add(barraWrap, BorderLayout.SOUTH);

        JPanel cardTbl = TemaBurok.crearTarjeta(new BorderLayout(0, 8));
        modelSeguimiento = new DefaultTableModel(new String[]{"Código", "Entidad", "Trámite", "Días Transcurridos", "Plazo SLA", "Estado", "Silencio Positivo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSeguimiento = new JTable(modelSeguimiento);
        TemaBurok.estilizarTabla(tblSeguimiento);
        cardTbl.add(new JScrollPane(tblSeguimiento), BorderLayout.CENTER);

        p.add(top, BorderLayout.NORTH);
        p.add(cardTbl, BorderLayout.CENTER);
        return p;
    }

    // --- VISTA 5: REPORTES, LIQUIDACIÓN EN LEMPIRAS Y JAVA 2D (MÓDULOS 6, 7, 9) ---
    private JPanel crearVistaReportes() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JPanel topLiq = TemaBurok.crearTarjeta(new BorderLayout(12, 10));
        JLabel tit = new JLabel("Liquidación Financiera en Lempiras (L.) y Analítica Java 2D");
        tit.setFont(TemaBurok.FONT_HEADLINE);
        tit.setForeground(TemaBurok.PRIMARY);

        JPanel formLiq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formLiq.setOpaque(false);
        cmbExpLiquidar = new JComboBox<>();
        cmbExpLiquidar.addActionListener(e -> actualizarResumenLiquidacion());
        lblSubMun = new JLabel("Municipal: L. 0.00");
        lblSubCen = new JLabel("Central: L. 0.00");
        lblCostoTotalExp = new JLabel("Total: L. 0.00");
        lblCostoTotalExp.setFont(TemaBurok.FONT_TITLE);
        lblCostoTotalExp.setForeground(TemaBurok.HONDURAS_BLUE);
        lblAhorroHoras = new JLabel("Ahorro: 0 hrs");
        lblAhorroHoras.setForeground(TemaBurok.SUCCESS);
        lblAhorroHoras.setFont(TemaBurok.FONT_LABEL);

        txtMontoRecibido = new JTextField("12000", 8);
        JButton btnLiquidar = TemaBurok.crearBotonPrimario("Emitir Liquidación");
        btnLiquidar.addActionListener(e -> emitirLiquidacionUI());

        formLiq.add(new JLabel("Expediente:"));
        formLiq.add(cmbExpLiquidar);
        formLiq.add(lblSubMun);
        formLiq.add(lblSubCen);
        formLiq.add(lblCostoTotalExp);
        formLiq.add(lblAhorroHoras);
        formLiq.add(new JLabel("Pago (L.):"));
        formLiq.add(txtMontoRecibido);
        formLiq.add(btnLiquidar);

        modelLiquidaciones = new DefaultTableModel(new String[]{"No. Liquidación", "Fecha", "Expediente", "Municipal (L.)", "Central (L.)", "Total (L.)", "Recibido (L.)", "Cambio (L.)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLiquidaciones = new JTable(modelLiquidaciones);
        TemaBurok.estilizarTabla(tblLiquidaciones);
        JScrollPane spLiq = new JScrollPane(tblLiquidaciones);
        spLiq.setPreferredSize(new Dimension(0, 120));

        topLiq.add(tit, BorderLayout.NORTH);
        topLiq.add(formLiq, BorderLayout.CENTER);
        topLiq.add(spLiq, BorderLayout.SOUTH);

        canvasReporte = new CanvasReporte2D();

        p.add(topLiq, BorderLayout.NORTH);
        p.add(canvasReporte, BorderLayout.CENTER);
        return p;
    }

    // --- CARGA Y SINCRONIZACIÓN CON MYSQL ---
    private void cargarTodosLosDatos() {
        try {
            refrescarEmprendedores();
            refrescarEmpresas();
            refrescarCatalogo();
            refrescarExpedientes();
            refrescarLiquidaciones();
            refrescarTablaResumen();
            actualizarMetricasDashboard();
            actualizarGrafico2D();
        } catch (SQLException ex) {
            mostrarError("Error al consultar MySQL: " + ex.getMessage());
        }
    }

    private void refrescarEmprendedores() throws SQLException {
        modelEmprendedores.setRowCount(0);
        cmbPymProp.removeAllItems();
        for (Emprendedor e : controlador.listarEmprendedores()) {
            modelEmprendedores.addRow(new Object[]{e.getCodigo(), e.getNombreCompleto(), e.getDni(), e.getTelefono()});
            cmbPymProp.addItem(e);
        }
    }

    private void refrescarEmpresas() throws SQLException {
        modelEmpresas.setRowCount(0);
        cmbEmpresaActiva.removeAllItems();
        for (Empresa emp : controlador.listarEmpresas()) {
            modelEmpresas.addRow(new Object[]{emp.getCodigo(), emp.getNombreComercial(), emp.getMunicipio(), "Cat. " + emp.getCategoriaRiesgo(), String.format("L. %,.2f", emp.getCapitalSemilla())});
            cmbEmpresaActiva.addItem(emp);
        }
    }

    private void refrescarCatalogo() throws SQLException {
        modelCatalogo.setRowCount(0);
        for (TramiteCatalogo t : controlador.listarCatalogoTramites()) {
            modelCatalogo.addRow(new Object[]{t.getCodigo(), t.getEntidad(), t.getEtapa(), t.getNombre(), t.getHorasTradicionales(), t.getHorasBurok(), String.format("L. %,.2f", t.getCostoOficialLps()), t.getPlazoSlaDias()});
        }
    }

    private void refrescarExpedientes() throws SQLException {
        cmbExpSeguimiento.removeAllItems();
        cmbExpLiquidar.removeAllItems();
        for (ExpedienteFormalizacion exp : controlador.listarExpedientes()) {
            cmbExpSeguimiento.addItem(exp);
            cmbExpLiquidar.addItem(exp);
        }
        refrescarVistaTramitesPorEmpresa();
        refrescarTablaSeguimiento();
        actualizarResumenLiquidacion();
    }

    private void refrescarLiquidaciones() throws SQLException {
        modelLiquidaciones.setRowCount(0);
        for (LiquidacionCostos l : controlador.listarLiquidaciones()) {
            modelLiquidaciones.addRow(new Object[]{
                    l.getNumeroLiquidacion(), l.getFecha(), l.getExpediente().getNumeroExpediente(),
                    String.format("L. %,.2f", l.getSubtotalMunicipal()), String.format("L. %,.2f", l.getSubtotalCentral()),
                    String.format("L. %,.2f", l.getTotalLps()), String.format("L. %,.2f", l.getMontoRecibido()),
                    String.format("L. %,.2f", l.getCambio())
            });
        }
    }

    private void refrescarTablaResumen() {
        try {
            modelResumenEmpresas.setRowCount(0);
            String munSel = (String) cmbFiltroMun.getSelectedItem();
            List<ExpedienteFormalizacion> exps = controlador.listarExpedientes();
            for (Empresa emp : controlador.listarEmpresas()) {
                if (munSel != null && !"Todos los municipios".equals(munSel) && !emp.getMunicipio().equalsIgnoreCase(munSel)) {
                    continue;
                }
                ExpedienteFormalizacion match = null;
                for (ExpedienteFormalizacion x : exps) {
                    if (x.getEmpresa().getCodigo().equals(emp.getCodigo())) {
                        match = x;
                        break;
                    }
                }
                String avance = match != null ? match.getPorcentajeAvance() + "% (" + match.getCantidadCompletados() + "/" + match.getTramites().size() + ")" : "Sin iniciar";
                String estado = match != null ? match.getEstadoGlobal().getEtiqueta() : "Pendiente";
                modelResumenEmpresas.addRow(new Object[]{emp.getCodigo(), emp.getNombreComercial() + " · RTN: " + emp.getRtn(), emp.getFormaJuridica(), emp.getMunicipio(), emp.getCodigoCIIU(), avance, estado});
            }
        } catch (SQLException ex) {
            mostrarError("Error al filtrar empresas: " + ex.getMessage());
        }
    }

    private void actualizarMetricasDashboard() throws SQLException {
        List<Empresa> empresas = controlador.listarEmpresas();
        List<ExpedienteFormalizacion> exps = controlador.listarExpedientes();
        int pend = 0, comp = 0, hrs = 0;
        for (ExpedienteFormalizacion exp : exps) {
            hrs += exp.getHorasBurokEst();
            for (DetalleTramiteExpediente d : exp.getTramites()) {
                if (d.estaCompletado()) comp++;
                else pend++;
            }
        }
        lblMetricaEmpresas.setText(String.format("%02d", empresas.size()));
        lblMetricaPendientes.setText(String.format("%02d", pend));
        lblMetricaCompletados.setText(String.format("%02d", comp));
        lblMetricaHoras.setText(hrs + " hrs");
    }

    private void refrescarVistaTramitesPorEmpresa() {
        Empresa emp = (Empresa) cmbEmpresaActiva.getSelectedItem();
        if (emp == null) return;
        try {
            modelDocumentos.setRowCount(0);
            int validados = 0;
            List<RequisitoDocumento> docs = controlador.listarDocumentosEmpresa(emp.getCodigo());
            for (RequisitoDocumento d : docs) {
                if (d.isValidado()) validados++;
                modelDocumentos.addRow(new Object[]{d.getId(), d.getCodigoDoc(), d.getNombreDocumento(), d.getEntidadEmisora(), d.isValidado() ? "VALIDADO ✓" : "PENDIENTE"});
            }
            lblEstadoDocs.setText(validados + " / " + docs.size() + " validados");
            lblEstadoDocs.setForeground(validados == docs.size() && !docs.isEmpty() ? TemaBurok.SUCCESS : TemaBurok.ACCENT);

            ExpedienteFormalizacion exp = controlador.obtenerExpedientePorEmpresa(emp.getCodigo());
            int pAp = exp != null ? exp.getPorcentajePorEtapa("Apertura") : 0;
            int pFu = exp != null ? exp.getPorcentajePorEtapa("Funcionamiento") : 0;
            int pCi = exp != null ? exp.getPorcentajePorEtapa("Cierre") : 0;
            pbApertura.setValue(pAp); lblPctApertura.setText("Apertura: " + pAp + "%");
            pbFuncionamiento.setValue(pFu); lblPctFuncionamiento.setText("Funcionamiento: " + pFu + "%");
            pbCierre.setValue(pCi); lblPctCierre.setText("Cierre: " + pCi + "%");
        } catch (SQLException ex) {
            mostrarError("Error al cargar documentos: " + ex.getMessage());
        }
    }

    private void refrescarTablaSeguimiento() {
        ExpedienteFormalizacion exp = (ExpedienteFormalizacion) cmbExpSeguimiento.getSelectedItem();
        if (exp == null) return;
        modelSeguimiento.setRowCount(0);
        for (DetalleTramiteExpediente d : exp.getTramites()) {
            modelSeguimiento.addRow(new Object[]{
                    d.getTramite().getCodigo(), d.getTramite().getEntidad(), d.getTramite().getNombre(),
                    d.getDiasTranscurridos() + " días", d.getTramite().getPlazoSlaDias() + " días",
                    d.getEstado().getEtiqueta(), d.isSilencioPositivoAplicado() ? "APLICADO ✓" : "No"
            });
        }
    }

    private void actualizarResumenLiquidacion() {
        ExpedienteFormalizacion exp = (ExpedienteFormalizacion) cmbExpLiquidar.getSelectedItem();
        if (exp == null) return;
        double mun = 0, cen = 0;
        for (DetalleTramiteExpediente d : exp.getTramites()) {
            double c = d.getTramite().getCostoOficialLps();
            if (d.getTramite().getEntidad().toLowerCase().contains("alcaldía") || d.getTramite().getEntidad().toLowerCase().contains("bomberos")) {
                mun += c;
            } else {
                cen += c;
            }
        }
        lblSubMun.setText(String.format("Municipal: L. %,.2f", mun));
        lblSubCen.setText(String.format("Central: L. %,.2f", cen));
        lblCostoTotalExp.setText(String.format("Total: L. %,.2f", mun + cen));
        lblAhorroHoras.setText("Ahorro: " + Math.max(0, exp.getHorasTradicionalesEst() - exp.getHorasBurokEst()) + " hrs");
    }

    private void actualizarGrafico2D() {
        if (canvasReporte == null) return;
        try {
            canvasReporte.setDatos(
                    controlador.obtenerHorasTradicionalesPorEntidad(),
                    controlador.obtenerHorasBurokPorEntidad(),
                    controlador.obtenerExpedientesPorEstado()
            );
        } catch (SQLException ex) {
            mostrarError("Error al actualizar gráficos 2D: " + ex.getMessage());
        }
    }

    // --- ACCIONES UI ---
    private void ejecutarBusquedaEmpresa() {
        String cod = txtBuscarEmpresa.getText().trim();
        try {
            Empresa emp = controlador.buscarEmpresa(cod);
            JOptionPane.showMessageDialog(this,
                    "Empresa encontrada:\n" + emp.getNombreComercial() + "\nMunicipio: " + emp.getMunicipio() + "\nRTN: " + emp.getRtn(),
                    "Búsqueda Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (EmpresaNoEncontradaException ex) {
            mostrarError(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("Error MySQL: " + ex.getMessage());
        }
    }

    private void registrarEmprendedorUI() {
        try {
            controlador.registrarEmprendedor(txtEmpCod.getText(), txtEmpNom.getText(), txtEmpDni.getText(), txtEmpTel.getText(), txtEmpCorreo.getText(), txtEmpDir.getText());
            cargarTodosLosDatos();
            JOptionPane.showMessageDialog(this, "Emprendedor guardado en MySQL correctamente.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void deshacerEmprendedorUI() {
        try {
            String eliminado = controlador.deshacerUltimoEmprendedor();
            if (eliminado == null) {
                JOptionPane.showMessageDialog(this, "No hay registros recientes de emprendedores para deshacer.");
            } else {
                cargarTodosLosDatos();
                JOptionPane.showMessageDialog(this, "Se deshizo el registro del emprendedor: " + eliminado);
            }
        } catch (SQLException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarEmpresaUI() {
        try {
            double cap = Double.parseDouble(txtPymCap.getText().trim());
            controlador.registrarEmpresa(
                    txtPymCod.getText(), txtPymNom.getText(), txtPymRtn.getText(),
                    (String) cmbPymForma.getSelectedItem(), (String) cmbPymCiiu.getSelectedItem(),
                    (Integer) cmbPymRiesgo.getSelectedItem(), (String) cmbPymMun.getSelectedItem(),
                    cap, (Emprendedor) cmbPymProp.getSelectedItem()
            );
            cargarTodosLosDatos();
            JOptionPane.showMessageDialog(this, "Empresa registrada en MySQL con su Expediente Único inicializado.");
        } catch (NumberFormatException ex) {
            mostrarError("El capital semilla debe ser un número válido.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void deshacerEmpresaUI() {
        try {
            String eliminado = controlador.deshacerUltimaEmpresa();
            if (eliminado == null) {
                JOptionPane.showMessageDialog(this, "No hay registros recientes de empresas para deshacer.");
            } else {
                cargarTodosLosDatos();
                JOptionPane.showMessageDialog(this, "Se deshizo el registro de la empresa: " + eliminado);
            }
        } catch (SQLException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void alternarValidacionDocumentoUI() {
        int fila = tblDocumentos.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione un documento de la tabla Expediente Único.");
            return;
        }
        int idDoc = (Integer) modelDocumentos.getValueAt(fila, 0);
        boolean actual = "VALIDADO ✓".equals(modelDocumentos.getValueAt(fila, 4));
        try {
            controlador.cambiarEstadoDocumento(idDoc, !actual);
            refrescarVistaTramitesPorEmpresa();
        } catch (SQLException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void generarRutaCriticaUI() {
        Empresa emp = (Empresa) cmbEmpresaActiva.getSelectedItem();
        if (emp == null) return;
        try {
            ExpedienteFormalizacion exp = controlador.generarRutaCritica(emp);
            cargarTodosLosDatos();
            JOptionPane.showMessageDialog(this, "Expediente " + exp.getNumeroExpediente() + " listo con " + exp.getTramites().size() + " trámites en Ruta Crítica.");
        } catch (SQLException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void ejecutarHiloVentanillaUI() {
        ExpedienteFormalizacion exp = (ExpedienteFormalizacion) cmbExpSeguimiento.getSelectedItem();
        if (exp == null) return;
        try {
            btnIniciarHilo.setEnabled(false);
            pbHiloVentanilla.setValue(0);
            controlador.simularVentanillaConHilo(exp, new ProcesoVentanillaHilo.Listener() {
                @Override
                public void onProgreso(int porcentaje, String mensajeEtapa) {
                    pbHiloVentanilla.setValue(porcentaje);
                    lblEstadoHilo.setText(mensajeEtapa);
                }

                @Override
                public void onFinalizado(ExpedienteFormalizacion finalizado) {
                    btnIniciarHilo.setEnabled(true);
                    lblEstadoHilo.setText("Simulación completada: Expediente " + finalizado.getNumeroExpediente() + " actualizado en MySQL.");
                    cargarTodosLosDatos();
                    JOptionPane.showMessageDialog(VentanaPrincipal.this,
                            "Flujo interinstitucional completado para " + finalizado.getNumeroExpediente() +
                            ".\nSe aplicó Silencio Administrativo Positivo en trámites de Categoría 1 que superaron el SLA.",
                            "Simulación Finalizada", JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void onError(String mensaje) {
                    btnIniciarHilo.setEnabled(true);
                    mostrarError(mensaje);
                }
            });
        } catch (DocumentoFaltanteException ex) {
            btnIniciarHilo.setEnabled(true);
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            btnIniciarHilo.setEnabled(true);
            mostrarError(ex.getMessage());
        }
    }

    private void emitirLiquidacionUI() {
        ExpedienteFormalizacion exp = (ExpedienteFormalizacion) cmbExpLiquidar.getSelectedItem();
        if (exp == null) return;
        try {
            double recibido = Double.parseDouble(txtMontoRecibido.getText().trim());
            LiquidacionCostos liq = controlador.emitirLiquidacion(exp, recibido);
            cargarTodosLosDatos();
            JOptionPane.showMessageDialog(this,
                    String.format("Liquidación %s emitida.\nTotal Oficial: L. %,.2f\nCambio: L. %,.2f",
                            liq.getNumeroLiquidacion(), liq.getTotalLps(), liq.getCambio()),
                    "Liquidación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (PresupuestoInsuficienteException ex) {
            mostrarError(ex.getMessage());
        } catch (NumberFormatException ex) {
            mostrarError("Ingrese un monto válido en Lempiras.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación TrámiteFácil HN", JOptionPane.WARNING_MESSAGE);
    }
}
