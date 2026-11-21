package hilos;

import modelo.DetalleTramiteExpediente;
import modelo.EstadoTramite;
import modelo.ExpedienteFormalizacion;

import javax.swing.SwingUtilities;

/**
 * Hilo en segundo plano (Runnable) que simula el flujo interinstitucional
 * (IP -> SAR -> Alcaldía -> MiAmbiente/ARSA) y aplica Silencio Administrativo Positivo
 * cuando una entidad excede el plazo SLA en empresas de Categoría 1.
 */
public class ProcesoVentanillaHilo implements Runnable {

    public interface Listener {
        void onProgreso(int porcentaje, String mensajeEtapa);
        void onFinalizado(ExpedienteFormalizacion expediente);
        void onError(String mensaje);
    }

    private final ExpedienteFormalizacion expediente;
    private final Listener listener;
    private final Runnable accionPersistencia;

    public ProcesoVentanillaHilo(ExpedienteFormalizacion expediente, Listener listener, Runnable accionPersistencia) {
        this.expediente = expediente;
        this.listener = listener;
        this.accionPersistencia = accionPersistencia;
    }

    @Override
    public void run() {
        String[] etapas = {
                "1/10 · Validando Expediente Único Digital (RNP / DNI)...",
                "2/10 · Verificando Escritura Pública y Matrícula Mercantil (IP)...",
                "3/10 · Validando RTN Numérico y Régimen CAI en SAR...",
                "4/10 · Consultando compatibilidad de uso de suelo en Catastro...",
                "5/10 · Evaluando tasa municipal y dictamen de Bomberos...",
                "6/10 · Verificando reloj SLA en ventanilla municipal...",
                "7/10 · Evaluando categorización ambiental MiAmbiente (Cat. 1)...",
                "8/10 · Verificando licencia sanitaria en plataforma ARSA...",
                "9/10 · Aplicando Silencio Administrativo Positivo donde excede SLA...",
                "10/10 · Consolidando expediente y guardando en SQL Server..."
        };

        try {
            for (int i = 0; i < etapas.length; i++) {
                int porcentaje = (i + 1) * 10;
                String msg = etapas[i];
                SwingUtilities.invokeLater(() -> listener.onProgreso(porcentaje, msg));
                Thread.sleep(580); // 10 * 580ms = 5.8 segundos (> 5 seg requeridos)
            }

            boolean esBajoRiesgo = expediente.getEmpresa().getCategoriaRiesgo() == 1;

            for (DetalleTramiteExpediente d : expediente.getTramites()) {
                if (!d.estaCompletado()) {
                    int sla = d.getTramite().getPlazoSlaDias();
                    if (esBajoRiesgo && (d.getTramite().getEntidad().contains("Alcaldía") || d.getTramite().getEntidad().contains("MiAmbiente"))) {
                        d.setDiasTranscurridos(sla + 1);
                        d.setEstado(EstadoTramite.SILENCIO_POSITIVO);
                        d.setSilencioPositivoAplicado(true);
                    } else {
                        d.setDiasTranscurridos(Math.max(1, sla - 1));
                        d.setEstado(EstadoTramite.APROBADO);
                        d.setSilencioPositivoAplicado(false);
                    }
                }
            }

            expediente.setEstadoGlobal(EstadoTramite.APROBADO);
            if (accionPersistencia != null) {
                accionPersistencia.run();
            }

            SwingUtilities.invokeLater(() -> {
                expediente.setEnProceso(false);
                listener.onFinalizado(expediente);
            });
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            SwingUtilities.invokeLater(() -> {
                expediente.setEnProceso(false);
                listener.onError("Simulación interrumpida: " + ex.getMessage());
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                expediente.setEnProceso(false);
                listener.onError("Error al persistir avance en SQL Server: " + ex.getMessage());
            });
        }
    }
}
