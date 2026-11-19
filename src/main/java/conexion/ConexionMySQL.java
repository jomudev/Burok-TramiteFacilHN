package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexión JDBC hacia MySQL (burok_db).
 */
public class ConexionMySQL {
    private static final String URL = "jdbc:mysql://localhost:3306/burok_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection instancia;

    private ConexionMySQL() {
    }

    public static Connection obtenerConexion() throws SQLException {
        if (instancia == null || instancia.isClosed()) {
            instancia = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instancia;
    }

    public static boolean verificarConexion() {
        try {
            Connection conn = obtenerConexion();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
