package repositorio;

import conexion.ConexionSQLServer;
import modelo.ExpedienteFormalizacion;
import modelo.LiquidacionCostos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LiquidacionDAO implements RepositorioDAO<LiquidacionCostos> {
    private final ExpedienteDAO expedienteDAO;

    public LiquidacionDAO(ExpedienteDAO expedienteDAO) {
        this.expedienteDAO = expedienteDAO;
    }

    @Override
    public void guardar(LiquidacionCostos l) throws SQLException {
        String sql = "INSERT INTO liquidaciones (numero_liquidacion, fecha, numero_expediente, subtotal_municipal, subtotal_central, timbres_honorarios, total_lps, monto_recibido, cambio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, l.getNumeroLiquidacion());
            ps.setString(2, l.getFecha());
            ps.setString(3, l.getExpediente().getNumeroExpediente());
            ps.setDouble(4, l.getSubtotalMunicipal());
            ps.setDouble(5, l.getSubtotalCentral());
            ps.setDouble(6, l.getTimbresHonorarios());
            ps.setDouble(7, l.getTotalLps());
            ps.setDouble(8, l.getMontoRecibido());
            ps.setDouble(9, l.getCambio());
            ps.executeUpdate();
        }
    }

    @Override
    public List<LiquidacionCostos> obtenerTodos() throws SQLException {
        List<LiquidacionCostos> lista = new ArrayList<>();
        String sql = "SELECT numero_liquidacion, fecha, numero_expediente, subtotal_municipal, subtotal_central, timbres_honorarios, total_lps, monto_recibido, cambio FROM liquidaciones ORDER BY numero_liquidacion";
        try (Statement st = ConexionSQLServer.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ExpedienteFormalizacion exp = expedienteDAO.buscarPorCodigo(rs.getString("numero_expediente"));
                if (exp != null) {
                    lista.add(new LiquidacionCostos(
                            rs.getString("numero_liquidacion"),
                            rs.getString("fecha"),
                            exp,
                            rs.getDouble("subtotal_municipal"),
                            rs.getDouble("subtotal_central"),
                            rs.getDouble("timbres_honorarios"),
                            rs.getDouble("total_lps"),
                            rs.getDouble("monto_recibido"),
                            rs.getDouble("cambio")
                    ));
                }
            }
        }
        return lista;
    }

    @Override
    public LiquidacionCostos buscarPorCodigo(String codigo) throws SQLException {
        for (LiquidacionCostos l : obtenerTodos()) {
            if (l.getNumeroLiquidacion().equalsIgnoreCase(codigo)) return l;
        }
        return null;
    }

    @Override
    public void actualizar(LiquidacionCostos l) throws SQLException {
        // Liquidaciones emitidas son inmutables para auditoría
    }

    @Override
    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM liquidaciones WHERE numero_liquidacion = ?";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existe(String codigo) throws SQLException {
        return buscarPorCodigo(codigo) != null;
    }
}
