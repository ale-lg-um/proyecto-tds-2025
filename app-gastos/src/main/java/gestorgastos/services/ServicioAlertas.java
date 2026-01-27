package gestorgastos.services;

import gestorgastos.model.Alerta;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.strategies.FactoriaAlertas;
import gestorgastos.strategies.InterfaceAlerta;

public class ServicioAlertas {

    public Alerta comprobarAlertas(Cuenta cuenta, Gasto nuevoGasto) {
        if (cuenta.getAlertas() == null || cuenta.getAlertas().isEmpty()) return null;

        // PASO 1: Alertas específicas de Categoría (Prioridad Alta)
        for (Alerta alerta : cuenta.getAlertas()) {
            if (alerta.getCategoria() != null && 
                alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                
                if (verificarLimite(alerta, cuenta, nuevoGasto)) return alerta;
            }
        }

        // PASO 2: Alertas Globales/Generales (Prioridad Baja)
        // [IMPORTANTE] Asegúrate de que este bloque NO esté comentado
        for (Alerta alerta : cuenta.getAlertas()) {
            // Si la categoría es null o se llama "General", aplica a todo
            if (alerta.getCategoria() == null || "General".equalsIgnoreCase(alerta.getCategoria().getNombre())) {
                if (verificarLimite(alerta, cuenta, nuevoGasto)) return alerta;
            }
        }

        return null;
    }

    private boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto) {
        InterfaceAlerta estrategia = FactoriaAlertas.getEstrategia(alerta.getTipo());
        if (estrategia == null) return false;
        return estrategia.verificarLimite(alerta, cuenta, gasto);
    }
}