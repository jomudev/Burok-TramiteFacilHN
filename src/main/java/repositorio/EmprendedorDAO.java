package repositorio;

import conexion.ConexionMySQL;
import modelo.Emprendedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class EmprendedorDAO implements RepositorioDAO<Emprendedor> {
    private final Stack<String> historialCodigosInsertados = new Stack<>();

    @Override
    public void guardar(Emprendedor e) throws SQLException {
        String sql = "INSERT INTO emprendedores (codigo, nombre_completo, dni, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, e.getCodigo());
            ps.setString(2, e.getNombreCompleto());
            ps.setString(3, e.getDni());
            ps.setString(4, e.getTelefono());
            ps.setString(5, e.getCorreo());
            ps.setString(6, e.getDireccion());
            ps.executeUpdate();
            historialCodigosInsertados.push(e.getCodigo());
        }
    }

    @Override
    public List<Emprendedor> obtenerTodos() throws SQLException {
        List<Emprendedor> lista = new ArrayList<>();
        String sql = "SELECT codigo, nombre_completo, dni, telefono, correo, direccion FROM emprendedores ORDER BY codigo";
        try (Statement st = ConexionMySQL.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Emprendedor buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo, nombre_completo, dni, telefono, correo, direccion FROM emprendedores WHERE codigo = ?";
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
    public void actualizar(Emprendedor e) throws SQLException {
        String sql = "UPDATE emprendedores SET nombre_completo = ?, dni = ?, telefono = ?, correo = ?, direccion = ? WHERE codigo = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombreCompleto());
            ps.setString(2, e.getDni());
            ps.setString(3, e.getTelefono());
            ps.setString(4, e.getCorreo());
            ps.setString(5, e.getDireccion());
            ps.setString(6, e.getCodigo());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM emprendedores WHERE codigo = ?";
        try (PreparedStatement ps = ConexionMySQL.obtenerConexion().prepareStatement(sql)) {
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
        if (historialCodigosInsertados.isEmpty()) {
            return null;
        }
        String ultimo = historialCodigosInsertados.pop();
        eliminar(ultimo);
        return ultimo;
    }

    private Emprendedor mapear(ResultSet rs) throws SQLException {
        return new Emprendedor(
                rs.getString("codigo"),
                rs.getString("nombre_completo"),
                rs.getString("dni"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("direccion")
        );
    }
}
