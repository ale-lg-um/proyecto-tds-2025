package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;

// Estrategia que se sigue para las alertas mensuales
public class EstrategiaMensual implements InterfaceAlerta {

    @Override
    public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto nuevoGasto) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaGasto = nuevoGasto.getFecha();

        // Verificamos si el gasto se ha hecho en el mes actual
        boolean esEsteMes = (fechaGasto.getMonth() == hoy.getMonth()) && (fechaGasto.getYear() == hoy.getYear());
        
        if (!esEsteMes) {
            return false; // Es un gasto antiguo o futuro, no afecta al límite mensual actual.
        }

        // Comporbar categoría
        if(alerta.getCategoria() != null) {
            if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                return false;
            }
        }
        
        double limite = alerta.getLimite();
        
        // Sumamos el importe de los gastos del mes actual (incluyendo el nuevo gasto)
        double totalGastado = nuevoGasto.getImporte(); 
        for (Gasto g : cuenta.getGastos()) {
            boolean coincideMesActual = (g.getFecha().getMonth() == hoy.getMonth()) && (g.getFecha().getYear() == hoy.getYear());
            
            boolean mismaCategoria = true;
            if(alerta.getCategoria() != null) {
                mismaCategoria = alerta.getCategoria().getNombre().equalsIgnoreCase(g.getCategoria().getNombre());
            }
            
            if(coincideMesActual && mismaCategoria) {
                totalGastado += g.getImporte();
            }
        }
        
        return totalGastado > limite; // Comprobamos si se ha sobrepasado el límite
    }
}