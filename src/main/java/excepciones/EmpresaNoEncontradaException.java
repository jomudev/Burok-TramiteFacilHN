package excepciones;

public class EmpresaNoEncontradaException extends Exception {
    public EmpresaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
