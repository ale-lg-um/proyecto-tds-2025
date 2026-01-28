package gestorgastos.services;

import gestorgastos.model.Alerta;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.strategies.FactoriaAlertas;
import gestorgastos.strategies.InterfaceAlerta;

public class ServicioAlertas {

    public Alerta comprobarAlertas(Cuenta cuenta, Gasto nuevoGasto) {
        if (cuenta.getAlertas() == null || cuenta.getAlertas().isEmpty()) return null;

        for (Alerta alerta : cuenta.getAlertas()) {
            if (alerta.getCategoria() != null && 
                alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                
                if (verificarLimite(alerta, cuenta, nuevoGasto)) return alerta;
            }
        }

        for (Alerta alerta : cuenta.getAlertas()) {
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