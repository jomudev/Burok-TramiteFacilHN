package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexión JDBC dual: soporta Microsoft SQL Server y MySQL.
 * Cambie MOTOR_POR_DEFECTO o pase -Dburok.db=mysql / -Dburok.db=sqlserver al ejecutar.
 */
public class ConexionMySQL {

    public enum MotorBD {
        SQL_SERVER,
        MYSQL
    }

    // Motor activo por defecto (SQL_SERVER para Windows/SSMS, MYSQL para macOS/Linux)
    private static final MotorBD MOTOR_POR_DEFECTO = MotorBD.SQL_SERVER;

    // Configuración Microsoft SQL Server (TCP/IP puerto 1433)
    private static final String SQLSERVER_URL = "jdbc:sqlserver://localhost:1433;databaseName=burok_db;encrypt=true;trustServerCertificate=true;";
    private static final String SQLSERVER_USER = "sa";
    private static final String SQLSERVER_PASSWORD = "TuPasswordFuerte123!";

    // Configuración MySQL (puerto 3306)
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/burok_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

    private static Connection instancia;

    private ConexionMySQL() {
    }

    public static MotorBD getMotorActivo() {
        String prop = System.getProperty("burok.db", "").trim().toLowerCase();
        if ("mysql".equals(prop)) return MotorBD.MYSQL;
        if ("sqlserver".equals(prop)) return MotorBD.SQL_SERVER;
        return MOTOR_POR_DEFECTO;
    }

    public static Connection obtenerConexion() throws SQLException {
        if (instancia == null || instancia.isClosed()) {
            if (getMotorActivo() == MotorBD.SQL_SERVER) {
                instancia = DriverManager.getConnection(SQLSERVER_URL, SQLSERVER_USER, SQLSERVER_PASSWORD);
            } else {
                instancia = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            }
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

    public static String getNombreMotor() {
        return getMotorActivo() == MotorBD.SQL_SERVER ? "SQL Server" : "MySQL";
    }

    public static String getDetalleConexion() {
        return getMotorActivo() == MotorBD.SQL_SERVER
                ? "localhost:1433 / burok_db"
                : "localhost:3306 / burok_db";
    }
}
