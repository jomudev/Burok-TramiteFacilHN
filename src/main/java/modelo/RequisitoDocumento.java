package modelo;

public class RequisitoDocumento {
    private int id;
    private String empresaCodigo;
    private String codigoDoc;
    private String nombreDocumento;
    private String entidadEmisora;
    private boolean validado;

    public RequisitoDocumento(int id, String empresaCodigo, String codigoDoc, String nombreDocumento,
                              String entidadEmisora, boolean validado) {
        this.id = id;
        this.empresaCodigo = empresaCodigo;
        this.codigoDoc = codigoDoc;
        this.nombreDocumento = nombreDocumento;
        this.entidadEmisora = entidadEmisora;
        this.validado = validado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmpresaCodigo() { return empresaCodigo; }
    public void setEmpresaCodigo(String empresaCodigo) { this.empresaCodigo = empresaCodigo; }

    public String getCodigoDoc() { return codigoDoc; }
    public void setCodigoDoc(String codigoDoc) { this.codigoDoc = codigoDoc; }

    public String getNombreDocumento() { return nombreDocumento; }
    public void setNombreDocumento(String nombreDocumento) { this.nombreDocumento = nombreDocumento; }

    public String getEntidadEmisora() { return entidadEmisora; }
    public void setEntidadEmisora(String entidadEmisora) { this.entidadEmisora = entidadEmisora; }

    public boolean isValidado() { return validado; }
    public void setValidado(boolean validado) { this.validado = validado; }
}
