package excepciones;

public class DocumentoFaltanteException extends Exception {
    public DocumentoFaltanteException(String mensaje) {
        super(mensaje);
    }
}
