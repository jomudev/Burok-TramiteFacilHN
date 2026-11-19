package modelo;

import java.util.ArrayList;
import java.util.List;

public class ExpedienteFormalizacion {
    private String numeroExpediente;
    private String fechaCreacion;
    private Empresa empresa;
    private List<DetalleTramiteExpediente> tramites;
    private int horasTradicionalesEst;
    private int horasBurokEst;
    private EstadoTramite estadoGlobal;
    private boolean enProceso;
    private boolean liquidado;

    public ExpedienteFormalizacion(String numeroExpediente, String fechaCreacion, Empresa empresa,
                                   int horasTradicionalesEst, int horasBurokEst,
                                   EstadoTramite estadoGlobal, boolean liquidado) {
        this.numeroExpediente = numeroExpediente;
        this.fechaCreacion = fechaCreacion;
        this.empresa = empresa;
        this.tramites = new ArrayList<>();
        this.horasTradicionalesEst = horasTradicionalesEst;
        this.horasBurokEst = horasBurokEst;
        this.estadoGlobal = estadoGlobal;
        this.enProceso = false;
        this.liquidado = liquidado;
    }

    public String getNumeroExpediente() { return numeroExpediente; }
    public void setNumeroExpediente(String numeroExpediente) { this.numeroExpediente = numeroExpediente; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public List<DetalleTramiteExpediente> getTramites() { return tramites; }
    public void setTramites(List<DetalleTramiteExpediente> tramites) { this.tramites = tramites; }

    public int getHorasTradicionalesEst() { return horasTradicionalesEst; }
    public void setHorasTradicionalesEst(int horasTradicionalesEst) { this.horasTradicionalesEst = horasTradicionalesEst; }

    public int getHorasBurokEst() { return horasBurokEst; }
    public void setHorasBurokEst(int horasBurokEst) { this.horasBurokEst = horasBurokEst; }

    public EstadoTramite getEstadoGlobal() { return estadoGlobal; }
    public void setEstadoGlobal(EstadoTramite estadoGlobal) { this.estadoGlobal = estadoGlobal; }

    public boolean isEnProceso() { return enProceso; }
    public void setEnProceso(boolean enProceso) { this.enProceso = enProceso; }

    public boolean isLiquidado() { return liquidado; }
    public void setLiquidado(boolean liquidado) { this.liquidado = liquidado; }

    public int getCantidadCompletados() {
        int count = 0;
        for (DetalleTramiteExpediente d : tramites) {
            if (d.estaCompletado()) count++;
        }
        return count;
    }

    public int getPorcentajeAvance() {
        if (tramites.isEmpty()) return 0;
        return (int) Math.round((getCantidadCompletados() * 100.0) / tramites.size());
    }

    public int getPorcentajePorEtapa(String etapa) {
        int total = 0;
        int completados = 0;
        for (DetalleTramiteExpediente d : tramites) {
            if (d.getTramite().getEtapa().equalsIgnoreCase(etapa)) {
                total++;
                if (d.estaCompletado()) completados++;
            }
        }
        if (total == 0) return 0;
        return (int) Math.round((completados * 100.0) / total);
    }

    public double calcularCostoTotalLps() {
        double suma = 0;
        for (DetalleTramiteExpediente d : tramites) {
            suma += d.getTramite().getCostoOficialLps();
        }
        return suma;
    }

    @Override
    public String toString() {
        return numeroExpediente + " - " + empresa.getNombreComercial();
    }
}
