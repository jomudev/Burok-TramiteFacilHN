package modelo;

public class TramiteCatalogo {
    private String codigo;
    private String entidad;
    private String etapa;
    private String nombre;
    private int horasTradicionales;
    private int horasBurok;
    private double costoOficialLps;
    private int plazoSlaDias;

    public TramiteCatalogo(String codigo, String entidad, String etapa, String nombre,
                           int horasTradicionales, int horasBurok, double costoOficialLps, int plazoSlaDias) {
        this.codigo = codigo;
        this.entidad = entidad;
        this.etapa = etapa;
        this.nombre = nombre;
        this.horasTradicionales = horasTradicionales;
        this.horasBurok = horasBurok;
        this.costoOficialLps = costoOficialLps;
        this.plazoSlaDias = plazoSlaDias;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public String getEtapa() { return etapa; }
    public void setEtapa(String etapa) { this.etapa = etapa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getHorasTradicionales() { return horasTradicionales; }
    public void setHorasTradicionales(int horasTradicionales) { this.horasTradicionales = horasTradicionales; }

    public int getHorasBurok() { return horasBurok; }
    public void setHorasBurok(int horasBurok) { this.horasBurok = horasBurok; }

    public double getCostoOficialLps() { return costoOficialLps; }
    public void setCostoOficialLps(double costoOficialLps) { this.costoOficialLps = costoOficialLps; }

    public int getPlazoSlaDias() { return plazoSlaDias; }
    public void setPlazoSlaDias(int plazoSlaDias) { this.plazoSlaDias = plazoSlaDias; }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
