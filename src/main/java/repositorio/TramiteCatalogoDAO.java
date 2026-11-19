package repositorio;

import conexion.ConexionMySQL;
import modelo.TramiteCatalogo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TramiteCatalogoDAO implements RepositorioDAO<TramiteCatalogo> {

    @Override
    public void guardar(TramiteCatalogo t) throws SQLException {
        String sql = "INSERT INTO tramites_catalogo (codigo, entidad, etapa, nombre, horas_tradicionales, horas_burok, costo_oficial_lps, plazo_sla_dias) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, t.getCodigo());
            ps.setString(2, t.getEntidad());
            ps.setString(3, t.getEtapa());
            ps.setString(4, t.getNombre());
            ps.setInt(5, t.getHorasTradicionales());
            ps.setInt(6, t.getHorasBurok());
            ps.setDouble(7, t.getCostoOficialLps());
            ps.setInt(8, t.getPlazoSlaDias());
            ps.executeUpdate();
        }
    }

    @Override
    public List<TramiteCatalogo> obtenerTodos() throws SQLException {
        List<TramiteCatalogo> lista = new ArrayList<>();
        String sql = "SELECT codigo, entidad, etapa, nombre, horas_tradicionales, horas_burok, costo_oficial_lps, plazo_sla_dias FROM tramites_catalogo ORDER BY codigo";
        try (Statement st = ConexionMySQL.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public TramiteCatalogo buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo, entidad, etapa, nombre, horas_tradicionales, horas_burok, costo_oficial_lps, plazo_sla_dias FROM tramites_catalogo WHERE codigo = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void actualizar(TramiteCatalogo t) throws SQLException {
        String sql = "UPDATE tramites_catalogo SET entidad = ?, etapa = ?, nombre = ?, horas_tradicionales = ?, horas_burok = ?, costo_oficial_lps = ?, plazo_sla_dias = ? WHERE codigo = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, t.getEntidad());
            ps.setString(2, t.getEtapa());
            ps.setString(3, t.getNombre());
            ps.setInt(4, t.getHorasTradicionales());
            ps.setInt(5, t.getHorasBurok());
            ps.setDouble(6, t.getCostoOficialLps());
            ps.setInt(7, t.getPlazoSlaDias());
            ps.setString(8, t.getCodigo());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM tramites_catalogo WHERE codigo = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existe(String codigo) throws SQLException {
        return buscarPorCodigo(codigo) != null;
    }

    private TramiteCatalogo mapear(ResultSet rs) throws SQLException {
        return new TramiteCatalogo(
                rs.getString("codigo"),
                rs.getString("entidad"),
                rs.getString("etapa"),
                rs.getString("nombre"),
                rs.getInt("horas_tradicionales"),
                rs.getInt("horas_burok"),
                rs.getDouble("costo_oficial_lps"),
                rs.getInt("plazo_sla_dias")
        );
    }
}
