package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

// Estrategia que se sigue para las alertas semanales
public class EstrategiaSemanal implements InterfaceAlerta {

    @Override
    public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto nuevoGasto) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemanaActual = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemanaActual = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        // Verificamos si el gasto se ha hecho en la semana actual
        LocalDate fechaGasto = nuevoGasto.getFecha();
        if (fechaGasto.isBefore(inicioSemanaActual) || fechaGasto.isAfter(finSemanaActual)) {
            return false; 
        }

        // Comprobamos la categoría
        if(alerta.getCategoria() != null) {
            if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                return false;
            }
        }
        
        double limite = alerta.getLimite();
        
        // Sumamos el importe de los gastos que se han hecho esta semana (incluyendo el nuevo gasto)
        double totalGastado = nuevoGasto.getImporte();
        for(Gasto g : cuenta.getGastos()) {
            boolean enSemanaActual = !g.getFecha().isBefore(inicioSemanaActual) && !g.getFecha().isAfter(finSemanaActual);
            
            boolean mismaCategoria = true;
            if(alerta.getCategoria() != null) {
                mismaCategoria = alerta.getCategoria().getNombre().equalsIgnoreCase(g.getCategoria().getNombre());
            }
            
            if(enSemanaActual && mismaCategoria) {
                totalGastado += g.getImporte();
            }
        }
        
        return totalGastado > limite; // Comprobamos si se ha sobrepasado el límite
    }
}