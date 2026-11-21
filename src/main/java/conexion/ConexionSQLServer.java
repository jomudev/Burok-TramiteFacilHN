package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexión JDBC hacia Microsoft SQL Server (burok_db).
 */
public class ConexionSQLServer {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=burok_db;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "TuPassword1234#";

    private static Connection instancia;

    private ConexionSQLServer() {
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
