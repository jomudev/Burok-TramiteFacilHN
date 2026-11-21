package repositorio;

import conexion.ConexionSQLServer;
import modelo.Emprendedor;
import modelo.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class EmpresaDAO implements RepositorioDAO<Empresa> {
    private final EmprendedorDAO emprendedorDAO;
    private final Stack<String> historialCodigosInsertados = new Stack<>();

    public EmpresaDAO(EmprendedorDAO emprendedorDAO) {
        this.emprendedorDAO = emprendedorDAO;
    }

    @Override
    public void guardar(Empresa emp) throws SQLException {
        String sql = "INSERT INTO empresas (codigo, nombre_comercial, rtn, forma_juridica, codigo_ciiu, categoria_riesgo, municipio, capital_semilla, emprendedor_codigo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getCodigo());
            ps.setString(2, emp.getNombreComercial());
            ps.setString(3, emp.getRtn());
            ps.setString(4, emp.getFormaJuridica());
            ps.setString(5, emp.getCodigoCIIU());
            ps.setInt(6, emp.getCategoriaRiesgo());
            ps.setString(7, emp.getMunicipio());
            ps.setDouble(8, emp.getCapitalSemilla());
            ps.setString(9, emp.getPropietario().getCodigo());
            ps.executeUpdate();
            historialCodigosInsertados.push(emp.getCodigo());
        }
    }

    @Override
    public List<Empresa> obtenerTodos() throws SQLException {
        List<Empresa> lista = new ArrayList<>();
        Map<String, Emprendedor> cachePropietarios = new HashMap<>();
        for (Emprendedor e : emprendedorDAO.obtenerTodos()) {
            cachePropietarios.put(e.getCodigo(), e);
        }

        String sql = "SELECT codigo, nombre_comercial, rtn, forma_juridica, codigo_ciiu, categoria_riesgo, municipio, capital_semilla, emprendedor_codigo FROM empresas ORDER BY codigo";
        try (Statement st = ConexionSQLServer.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Emprendedor prop = cachePropietarios.get(rs.getString("emprendedor_codigo"));
                lista.add(mapear(rs, prop));
            }
        }
        return lista;
    }

    @Override
    public Empresa buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo, nombre_comercial, rtn, forma_juridica, codigo_ciiu, categoria_riesgo, municipio, capital_semilla, emprendedor_codigo FROM empresas WHERE codigo = ?";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Emprendedor prop = emprendedorDAO.buscarPorCodigo(rs.getString("emprendedor_codigo"));
                    return mapear(rs, prop);
                }
            }
        }
        return null;
    }

    @Override
    public void actualizar(Empresa emp) throws SQLException {
        String sql = "UPDATE empresas SET nombre_comercial = ?, rtn = ?, forma_juridica = ?, codigo_ciiu = ?, categoria_riesgo = ?, municipio = ?, capital_semilla = ?, emprendedor_codigo = ? WHERE codigo = ?";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombreComercial());
            ps.setString(2, emp.getRtn());
            ps.setString(3, emp.getFormaJuridica());
            ps.setString(4, emp.getCodigoCIIU());
            ps.setInt(5, emp.getCategoriaRiesgo());
            ps.setString(6, emp.getMunicipio());
            ps.setDouble(7, emp.getCapitalSemilla());
            ps.setString(8, emp.getPropietario().getCodigo());
            ps.setString(9, emp.getCodigo());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM empresas WHERE codigo = ?";
        try (PreparedStatement ps = ConexionSQLServer.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existe(String codigo) throws SQLException {
        return buscarPorCodigo(codigo) != null;
    }

    public boolean puedeDeshacer() {
        return !historialCodigosInsertados.isEmpty();
    }

    public String deshacerUltimoRegistro() throws SQLException {
        if (historialCodigosInsertados.isEmpty()) return null;
        String ultimo = historialCodigosInsertados.pop();
        eliminar(ultimo);
        return ultimo;
    }

    private Empresa mapear(ResultSet rs, Emprendedor prop) throws SQLException {
        return new Empresa(
                rs.getString("codigo"),
                rs.getString("nombre_comercial"),
                rs.getString("rtn"),
                rs.getString("forma_juridica"),
                rs.getString("codigo_ciiu"),
                rs.getInt("categoria_riesgo"),
                rs.getString("municipio"),
                rs.getDouble("capital_semilla"),
                prop
        );
    }
}
