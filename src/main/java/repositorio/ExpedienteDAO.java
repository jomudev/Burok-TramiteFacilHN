package repositorio;

import conexion.ConexionMySQL;
import modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpedienteDAO implements RepositorioDAO<ExpedienteFormalizacion> {
    private final EmpresaDAO empresaDAO;
    private final TramiteCatalogoDAO tramiteDAO;

    public ExpedienteDAO(EmpresaDAO empresaDAO, TramiteCatalogoDAO tramiteDAO) {
        this.empresaDAO = empresaDAO;
        this.tramiteDAO = tramiteDAO;
    }

    @Override
    public void guardar(ExpedienteFormalizacion exp) throws SQLException {
        String sql = "INSERT INTO expedientes (numero_expediente, fecha_creacion, empresa_codigo, horas_tradicionales_est, horas_burok_est, estado_global, liquidado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, exp.getNumeroExpediente());
            ps.setString(2, exp.getFechaCreacion());
            ps.setString(3, exp.getEmpresa().getCodigo());
            ps.setInt(4, exp.getHorasTradicionalesEst());
            ps.setInt(5, exp.getHorasBurokEst());
            ps.setString(6, exp.getEstadoGlobal().name());
            ps.setBoolean(7, exp.isLiquidado());
            ps.executeUpdate();
        }
        guardarDetallesTramites(exp);
    }

    private void guardarDetallesTramites(ExpedienteFormalizacion exp) throws SQLException {
        String sqlDel = "DELETE FROM expediente_tramites WHERE numero_expediente = ?";
        try (PreparedStatement psDel = ConexionMySQL.obtenerConexion().prepareStatement(sqlDel)) {
            psDel.setString(1, exp.getNumeroExpediente());
            psDel.executeUpdate();
        }
        String sqlIns = "INSERT INTO expediente_tramites (numero_expediente, tramite_codigo, dias_transcurridos, estado, silencio_positivo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sqlIns)) {
            for (DetalleTramiteExpediente d : exp.getTramites()) {
                ps.setString(1, exp.getNumeroExpediente());
                ps.setString(2, d.getTramite().getCodigo());
                ps.setInt(3, d.getDiasTranscurridos());
                ps.setString(4, d.getEstado().name());
                ps.setBoolean(5, d.isSilencioPositivoAplicado());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<ExpedienteFormalizacion> obtenerTodos() throws SQLException {
        List<ExpedienteFormalizacion> lista = new ArrayList<>();
        Map<String, Empresa> cacheEmpresas = new HashMap<>();
        for (Empresa e : empresaDAO.obtenerTodos()) {
            cacheEmpresas.put(e.getCodigo(), e);
        }
        Map<String, TramiteCatalogo> cacheTramites = new HashMap<>();
        for (TramiteCatalogo t : tramiteDAO.obtenerTodos()) {
            cacheTramites.put(t.getCodigo(), t);
        }

        String sql = "SELECT numero_expediente, fecha_creacion, empresa_codigo, horas_tradicionales_est, horas_burok_est, estado_global, liquidado FROM expedientes ORDER BY numero_expediente";
        try (Statement st = ConexionMySQL.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Empresa emp = cacheEmpresas.get(rs.getString("empresa_codigo"));
                if (emp == null) continue;
                ExpedienteFormalizacion exp = new ExpedienteFormalizacion(
                        rs.getString("numero_expediente"),
                        rs.getString("fecha_creacion"),
                        emp,
                        rs.getInt("horas_tradicionales_est"),
                        rs.getInt("horas_burok_est"),
                        EstadoTramite.valueOf(rs.getString("estado_global")),
                        rs.getBoolean("liquidado")
                );
                exp.setTramites(cargarDetalles(exp.getNumeroExpediente(), cacheTramites));
                lista.add(exp);
            }
        }
        return lista;
    }

    private List<DetalleTramiteExpediente> cargarDetalles(String numExp, Map<String, TramiteCatalogo> cacheTramites) throws SQLException {
        List<DetalleTramiteExpediente> detalles = new ArrayList<>();
        String sql = "SELECT id, tramite_codigo, dias_transcurridos, estado, silencio_positivo FROM expediente_tramites WHERE numero_expediente = ? ORDER BY id";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, numExp);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TramiteCatalogo tc = cacheTramites.get(rs.getString("tramite_codigo"));
                    if (tc != null) {
                        detalles.add(new DetalleTramiteExpediente(
                                rs.getInt("id"),
                                tc,
                                rs.getInt("dias_transcurridos"),
                                EstadoTramite.valueOf(rs.getString("estado")),
                                rs.getBoolean("silencio_positivo")
                        ));
                    }
                }
            }
        }
        return detalles;
    }

    @Override
    public ExpedienteFormalizacion buscarPorCodigo(String numeroExpediente) throws SQLException {
        for (ExpedienteFormalizacion exp : obtenerTodos()) {
            if (exp.getNumeroExpediente().equalsIgnoreCase(numeroExpediente)) {
                return exp;
            }
        }
        return null;
    }

    @Override
    public void actualizar(ExpedienteFormalizacion exp) throws SQLException {
        String sql = "UPDATE expedientes SET estado_global = ?, liquidado = ?, horas_tradicionales_est = ?, horas_burok_est = ? WHERE numero_expediente = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, exp.getEstadoGlobal().name());
            ps.setBoolean(2, exp.isLiquidado());
            ps.setInt(3, exp.getHorasTradicionalesEst());
            ps.setInt(4, exp.getHorasBurokEst());
            ps.setString(5, exp.getNumeroExpediente());
            ps.executeUpdate();
        }
        guardarDetallesTramites(exp);
    }

    @Override
    public void eliminar(String numeroExpediente) throws SQLException {
        String sql = "DELETE FROM expedientes WHERE numero_expediente = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existe(String numeroExpediente) throws SQLException {
        return buscarPorCodigo(numeroExpediente) != null;
    }

    public List<RequisitoDocumento> obtenerDocumentosPorEmpresa(String empresaCodigo) throws SQLException {
        List<RequisitoDocumento> docs = new ArrayList<>();
        String sql = "SELECT id, empresa_codigo, codigo_doc, nombre_documento, entidad_emisora, validado FROM expediente_documentos WHERE empresa_codigo = ? ORDER BY codigo_doc";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, empresaCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    docs.add(new RequisitoDocumento(
                            rs.getInt("id"),
                            rs.getString("empresa_codigo"),
                            rs.getString("codigo_doc"),
                            rs.getString("nombre_documento"),
                            rs.getString("entidad_emisora"),
                            rs.getBoolean("validado")
                    ));
                }
            }
        }
        return docs;
    }

    public void inicializarDocumentosBaseEmpresa(String empresaCodigo) throws SQLException {
        if (!obtenerDocumentosPorEmpresa(empresaCodigo).isEmpty()) return;
        String sql = "INSERT INTO expediente_documentos (empresa_codigo, codigo_doc, nombre_documento, entidad_emisora, validado) VALUES (?, ?, ?, ?, ?)";
        Object[][] base = {
                {"DOC-01", "DNI del Representante Legal", "RNP", true},
                {"DOC-02", "Testimonio de Escritura Pública", "Notario / IP", true},
                {"DOC-03", "Contrato de Arrendamiento Local", "Privado / Notariado", false},
                {"DOC-04", "Croquis de Ubicación Catastral", "Propietario", false}
        };
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            for (Object[] row : base) {
                ps.setString(1, empresaCodigo);
                ps.setString(2, (String) row[0]);
                ps.setString(3, (String) row[1]);
                ps.setString(4, (String) row[2]);
                ps.setBoolean(5, (Boolean) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void actualizarValidacionDocumento(int idDoc, boolean validado) throws SQLException {
        String sql = "UPDATE expediente_documentos SET validado = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setBoolean(1, validado);
            ps.setInt(2, idDoc);
            ps.executeUpdate();
        }
    }
}
