    package modelo;

public class Empresa {
    private String codigo;
    private String nombreComercial;
    private String rtn;
    private String formaJuridica;
    private String codigoCIIU;
    private int categoriaRiesgo;
    private String municipio;
    private double capitalSemilla;
    private Emprendedor propietario;

    public Empresa(String codigo, String nombreComercial, String rtn, String formaJuridica,
                   String codigoCIIU, int categoriaRiesgo, String municipio,
                   double capitalSemilla, Emprendedor propietario) {
        this.codigo = codigo;
        this.nombreComercial = nombreComercial;
        this.rtn = rtn;
        this.formaJuridica = formaJuridica;
        this.codigoCIIU = codigoCIIU;
        this.categoriaRiesgo = categoriaRiesgo;
        this.municipio = municipio;
        this.capitalSemilla = capitalSemilla;
        this.propietario = propietario;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getRtn() { return rtn; }
    public void setRtn(String rtn) { this.rtn = rtn; }

    public String getFormaJuridica() { return formaJuridica; }
    public void setFormaJuridica(String formaJuridica) { this.formaJuridica = formaJuridica; }

    public String getCodigoCIIU() { return codigoCIIU; }
    public void setCodigoCIIU(String codigoCIIU) { this.codigoCIIU = codigoCIIU; }

    public int getCategoriaRiesgo() { return categoriaRiesgo; }
    public void setCategoriaRiesgo(int categoriaRiesgo) { this.categoriaRiesgo = categoriaRiesgo; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public double getCapitalSemilla() { return capitalSemilla; }
    public void setCapitalSemilla(double capitalSemilla) { this.capitalSemilla = capitalSemilla; }

    public Emprendedor getPropietario() { return propietario; }
    public void setPropietario(Emprendedor propietario) { this.propietario = propietario; }

    public String getIniciales() {
        String[] partes = nombreComercial.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return nombreComercial.length() >= 2 ? nombreComercial.substring(0, 2).toUpperCase() : "PY";
    }

    @Override
    public String toString() {
        return nombreComercial + " (" + municipio + ")";
    }
}
