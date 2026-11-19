package modelo;

public class DetalleTramiteExpediente {
    private int id;
    private TramiteCatalogo tramite;
    private int diasTranscurridos;
    private EstadoTramite estado;
    private boolean silencioPositivoAplicado;

    public DetalleTramiteExpediente(int id, TramiteCatalogo tramite, int diasTranscurridos,
                                    EstadoTramite estado, boolean silencioPositivoAplicado) {
        this.id = id;
        this.tramite = tramite;
        this.diasTranscurridos = diasTranscurridos;
        this.estado = estado;
        this.silencioPositivoAplicado = silencioPositivoAplicado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TramiteCatalogo getTramite() { return tramite; }
    public void setTramite(TramiteCatalogo tramite) { this.tramite = tramite; }

    public int getDiasTranscurridos() { return diasTranscurridos; }
    public void setDiasTranscurridos(int diasTranscurridos) { this.diasTranscurridos = diasTranscurridos; }

    public EstadoTramite getEstado() { return estado; }
    public void setEstado(EstadoTramite estado) { this.estado = estado; }

    public boolean isSilencioPositivoAplicado() { return silencioPositivoAplicado; }
    public void setSilencioPositivoAplicado(boolean silencioPositivoAplicado) {
        this.silencioPositivoAplicado = silencioPositivoAplicado;
    }

    public boolean estaCompletado() {
        return estado == EstadoTramite.APROBADO || estado == EstadoTramite.SILENCIO_POSITIVO;
    }
}
