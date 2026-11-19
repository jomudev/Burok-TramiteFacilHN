package repositorio;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica DAO/Repositorio para persistencia en MySQL.
 */
public interface RepositorioDAO<T> {
    void guardar(T entidad) throws SQLException;
    List<T> obtenerTodos() throws SQLException;
    T buscarPorCodigo(String codigo) throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(String codigo) throws SQLException;
    boolean existe(String codigo) throws SQLException;
}
