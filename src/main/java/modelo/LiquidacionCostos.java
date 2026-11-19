package modelo;

public class LiquidacionCostos {
    private String numeroLiquidacion;
    private String fecha;
    private ExpedienteFormalizacion expediente;
    private double subtotalMunicipal;
    private double subtotalCentral;
    private double timbresHonorarios;
    private double totalLps;
    private double montoRecibido;
    private double cambio;

    public LiquidacionCostos(String numeroLiquidacion, String fecha, ExpedienteFormalizacion expediente,
                             double subtotalMunicipal, double subtotalCentral, double timbresHonorarios,
                             double totalLps, double montoRecibido, double cambio) {
        this.numeroLiquidacion = numeroLiquidacion;
        this.fecha = fecha;
        this.expediente = expediente;
        this.subtotalMunicipal = subtotalMunicipal;
        this.subtotalCentral = subtotalCentral;
        this.timbresHonorarios = timbresHonorarios;
        this.totalLps = totalLps;
        this.montoRecibido = montoRecibido;
        this.cambio = cambio;
    }

    public String getNumeroLiquidacion() { return numeroLiquidacion; }
    public String getFecha() { return fecha; }
    public ExpedienteFormalizacion getExpediente() { return expediente; }
    public double getSubtotalMunicipal() { return subtotalMunicipal; }
    public double getSubtotalCentral() { return subtotalCentral; }
    public double getTimbresHonorarios() { return timbresHonorarios; }
    public double getTotalLps() { return totalLps; }
    public double getMontoRecibido() { return montoRecibido; }
    public double getCambio() { return cambio; }
}
