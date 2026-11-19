package modelo;

public enum EstadoTramite {
    PENDIENTE("Pendiente"),
    EN_REVISION("En Revisión"),
    SUBSANACION("Subsanación"),
    SILENCIO_POSITIVO("Silencio Positivo"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String etiqueta;

    EstadoTramite(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
