package controlador;

import excepciones.DocumentoFaltanteException;
import excepciones.EmpresaNoEncontradaException;
import excepciones.PresupuestoInsuficienteException;
import hilos.ProcesoVentanillaHilo;
import modelo.*;
import repositorio.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Controlador principal MVC de TrámiteFácil HN (Burok).
 */
public class BurokControlador {
    private final EmprendedorDAO emprendedorDAO;
    private final EmpresaDAO empresaDAO;
    private final TramiteCatalogoDAO tramiteDAO;
    private final ExpedienteDAO expedienteDAO;
    private final LiquidacionDAO liquidacionDAO;

    public BurokControlador() {
        this.emprendedorDAO = new EmprendedorDAO();
        this.empresaDAO = new EmpresaDAO(emprendedorDAO);
        this.tramiteDAO = new TramiteCatalogoDAO();
        this.expedienteDAO = new ExpedienteDAO(empresaDAO, tramiteDAO);
        this.liquidacionDAO = new LiquidacionDAO(expedienteDAO);
    }

    // --- MÓDULO 1: EMPRENDEDORES ---
    public void registrarEmprendedor(String codigo, String nombre, String dni, String tel, String correo, String dir) throws SQLException {
        validarNoVacio(codigo, "Código");
        validarNoVacio(nombre, "Nombre completo");
        validarNoVacio(dni, "DNI");
        if (emprendedorDAO.existe(codigo)) {
            throw new IllegalArgumentException("Ya existe un emprendedor con el código: " + codigo);
        }
        emprendedorDAO.guardar(new Emprendedor(codigo.trim(), nombre.trim(), dni.trim(), tel.trim(), correo.trim(), dir.trim()));
    }

    public List<Emprendedor> listarEmprendedores() throws SQLException {
        return emprendedorDAO.obtenerTodos();
    }

    public void eliminarEmprendedor(String codigo) throws SQLException {
        emprendedorDAO.eliminar(codigo);
    }

    public String deshacerUltimoEmprendedor() throws SQLException {
        return emprendedorDAO.deshacerUltimoRegistro();
    }

    // --- MÓDULO 2: EMPRESAS Y CIIU ---
    public void registrarEmpresa(String codigo, String nombreComercial, String rtn, String formaJuridica,
                                 String ciiu, int categoriaRiesgo, String municipio, double capitalSemilla,
                                 Emprendedor propietario) throws SQLException {
        validarNoVacio(codigo, "Código de empresa");
        validarNoVacio(nombreComercial, "Nombre comercial");
        if (propietario == null) {
            throw new IllegalArgumentException("Debe seleccionar un emprendedor propietario.");
        }
        if (capitalSemilla <= 0) {
            throw new IllegalArgumentException("El capital semilla debe ser mayor a L. 0.00");
        }
        if (empresaDAO.existe(codigo)) {
            throw new IllegalArgumentException("Ya existe una empresa con el código: " + codigo);
        }
        Empresa emp = new Empresa(codigo.trim(), nombreComercial.trim(), rtn.trim(), formaJuridica, ciiu, categoriaRiesgo, municipio, capitalSemilla, propietario);
        empresaDAO.guardar(emp);
        expedienteDAO.inicializarDocumentosBaseEmpresa(emp.getCodigo());
    }

    public List<Empresa> listarEmpresas() throws SQLException {
        return empresaDAO.obtenerTodos();
    }

    public Empresa buscarEmpresa(String codigo) throws SQLException, EmpresaNoEncontradaException {
        Empresa emp = empresaDAO.buscarPorCodigo(codigo);
        if (emp == null) {
            throw new EmpresaNoEncontradaException("No se encontró ninguna empresa registrada con el código: " + codigo);
        }
        return emp;
    }

    public String deshacerUltimaEmpresa() throws SQLException {
        return empresaDAO.deshacerUltimoRegistro();
    }

    // --- MÓDULO 3: CATÁLOGO DE TRÁMITES ---
    public List<TramiteCatalogo> listarCatalogoTramites() throws SQLException {
        return tramiteDAO.obtenerTodos();
    }

    public void registrarTramiteCatalogo(String codigo, String entidad, String etapa, String nombre,
                                         int hrsTrad, int hrsBurok, double costoLps, int slaDias) throws SQLException {
        validarNoVacio(codigo, "Código de trámite");
        validarNoVacio(nombre, "Nombre del trámite");
        if (costoLps < 0) {
            throw new IllegalArgumentException("El costo oficial no puede ser negativo.");
        }
        if (tramiteDAO.existe(codigo)) {
            throw new IllegalArgumentException("El código de trámite ya existe: " + codigo);
        }
        tramiteDAO.guardar(new TramiteCatalogo(codigo.trim(), entidad, etapa, nombre.trim(), hrsTrad, hrsBurok, costoLps, slaDias));
    }

    // --- MÓDULO 4: EXPEDIENTE ÚNICO DIGITAL ---
    public List<RequisitoDocumento> listarDocumentosEmpresa(String empresaCodigo) throws SQLException {
        return expedienteDAO.obtenerDocumentosPorEmpresa(empresaCodigo);
    }

    public void cambiarEstadoDocumento(int idDoc, boolean validado) throws SQLException {
        expedienteDAO.actualizarValidacionDocumento(idDoc, validado);
    }

    public void verificarDocumentosCompletos(String empresaCodigo) throws SQLException, DocumentoFaltanteException {
        List<RequisitoDocumento> docs = expedienteDAO.obtenerDocumentosPorEmpresa(empresaCodigo);
        List<String> faltantes = new ArrayList<>();
        for (RequisitoDocumento d : docs) {
            if (!d.isValidado()) {
                faltantes.add(d.getNombreDocumento());
            }
        }
        if (!faltantes.isEmpty()) {
            throw new DocumentoFaltanteException(
                    "Faltan documentos validados en el Expediente Único:\n• " + String.join("\n• ", faltantes)
            );
        }
    }

    // --- MÓDULO 5 Y 10: EXPEDIENTES, RUTA CRÍTICA Y MULTIHILO ---
    public List<ExpedienteFormalizacion> listarExpedientes() throws SQLException {
        return expedienteDAO.obtenerTodos();
    }

    public ExpedienteFormalizacion obtenerExpedientePorEmpresa(String empresaCodigo) throws SQLException {
        for (ExpedienteFormalizacion exp : expedienteDAO.obtenerTodos()) {
            if (exp.getEmpresa().getCodigo().equalsIgnoreCase(empresaCodigo)) {
                return exp;
            }
        }
        return null;
    }

    public ExpedienteFormalizacion generarRutaCritica(Empresa empresa) throws SQLException {
        ExpedienteFormalizacion existente = obtenerExpedientePorEmpresa(empresa.getCodigo());
        if (existente != null) {
            return existente;
        }
        List<TramiteCatalogo> catalogo = tramiteDAO.obtenerTodos();
        int hrsTrad = 0;
        int hrsBurok = 0;
        String numExp = String.format("EXP-%03d", expedienteDAO.obtenerTodos().size() + 1);
        ExpedienteFormalizacion exp = new ExpedienteFormalizacion(
                numExp,
                LocalDate.now().toString(),
                empresa,
                0,
                0,
                EstadoTramite.PENDIENTE,
                false
        );
        for (TramiteCatalogo tc : catalogo) {
            hrsTrad += tc.getHorasTradicionales();
            hrsBurok += tc.getHorasBurok();
            exp.getTramites().add(new DetalleTramiteExpediente(0, tc, 0, EstadoTramite.PENDIENTE, false));
        }
        exp.setHorasTradicionalesEst(hrsTrad);
        exp.setHorasBurokEst(hrsBurok);
        expedienteDAO.guardar(exp);
        return exp;
    }

    public void agregarTramiteAExpediente(ExpedienteFormalizacion exp, TramiteCatalogo tramite) throws SQLException {
        for (DetalleTramiteExpediente d : exp.getTramites()) {
            if (d.getTramite().getCodigo().equals(tramite.getCodigo())) {
                throw new IllegalArgumentException("El trámite " + tramite.getCodigo() + " ya forma parte del expediente.");
            }
        }
        exp.getTramites().add(new DetalleTramiteExpediente(0, tramite, 0, EstadoTramite.PENDIENTE, false));
        exp.setHorasTradicionalesEst(exp.getHorasTradicionalesEst() + tramite.getHorasTradicionales());
        exp.setHorasBurokEst(exp.getHorasBurokEst() + tramite.getHorasBurok());
        expedienteDAO.actualizar(exp);
    }

    public void simularVentanillaConHilo(ExpedienteFormalizacion exp, ProcesoVentanillaHilo.Listener listener)
            throws SQLException, DocumentoFaltanteException {
        if (exp.isEnProceso()) {
            throw new IllegalStateException("Este expediente ya se encuentra en procesamiento por un hilo activo.");
        }
        verificarDocumentosCompletos(exp.getEmpresa().getCodigo());
        exp.setEnProceso(true);
        ProcesoVentanillaHilo tarea = new ProcesoVentanillaHilo(exp, listener, () -> {
            try {
                expedienteDAO.actualizar(exp);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        new Thread(tarea, "Hilo-Ventanilla-" + exp.getNumeroExpediente()).start();
    }

    // --- MÓDULO 6 Y 9: LIQUIDACIÓN FINANCIERA ---
    public LiquidacionCostos emitirLiquidacion(ExpedienteFormalizacion exp, double montoRecibido)
            throws SQLException, PresupuestoInsuficienteException {
        if (exp.getEstadoGlobal() != EstadoTramite.APROBADO && exp.getEstadoGlobal() != EstadoTramite.SILENCIO_POSITIVO) {
            throw new IllegalStateException("Solo se pueden liquidar expedientes Aprobados o con Silencio Positivo.");
        }
        double subMunicipal = 0;
        double subCentral = 0;
        for (DetalleTramiteExpediente d : exp.getTramites()) {
            double costo = d.getTramite().getCostoOficialLps();
            if (d.getTramite().getEntidad().toLowerCase().contains("alcaldía") ||
                d.getTramite().getEntidad().toLowerCase().contains("bomberos")) {
                subMunicipal += costo;
            } else {
                subCentral += costo;
            }
        }
        double total = subMunicipal + subCentral;
        if (montoRecibido < total) {
            throw new PresupuestoInsuficienteException(
                    String.format("Monto recibido (L. %,.2f) es insuficiente para cubrir las tasas oficiales (L. %,.2f). Faltan L. %,.2f.",
                            montoRecibido, total, (total - montoRecibido))
            );
        }
        double cambio = montoRecibido - total;
        String numLiq = String.format("LIQ-%03d", liquidacionDAO.obtenerTodos().size() + 1);
        LiquidacionCostos liq = new LiquidacionCostos(
                numLiq,
                LocalDate.now().toString(),
                exp,
                subMunicipal,
                subCentral,
                0.0,
                total,
                montoRecibido,
                cambio
        );
        liquidacionDAO.guardar(liq);
        exp.setLiquidado(true);
        expedienteDAO.actualizar(exp);
        return liq;
    }

    public List<LiquidacionCostos> listarLiquidaciones() throws SQLException {
        return liquidacionDAO.obtenerTodos();
    }

    // --- MÓDULO 7: DATOS PARA REPORTES JAVA 2D ---
    public Map<String, Integer> obtenerHorasTradicionalesPorEntidad() throws SQLException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (TramiteCatalogo t : tramiteDAO.obtenerTodos()) {
            mapa.merge(t.getEntidad(), t.getHorasTradicionales(), Integer::sum);
        }
        return mapa;
    }

    public Map<String, Integer> obtenerHorasBurokPorEntidad() throws SQLException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (TramiteCatalogo t : tramiteDAO.obtenerTodos()) {
            mapa.merge(t.getEntidad(), t.getHorasBurok(), Integer::sum);
        }
        return mapa;
    }

    public Map<String, Integer> obtenerExpedientesPorEstado() throws SQLException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        mapa.put("Pendiente", 0);
        mapa.put("En Revisión", 0);
        mapa.put("Silencio Positivo", 0);
        mapa.put("Aprobado", 0);
        for (ExpedienteFormalizacion exp : expedienteDAO.obtenerTodos()) {
            String key = exp.getEstadoGlobal().getEtiqueta();
            mapa.merge(key, 1, Integer::sum);
        }
        return mapa;
    }

    private void validarNoVacio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
