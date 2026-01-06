package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

public class EstrategiaSemanal implements InterfaceAlerta {

    @Override
    public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto nuevoGasto) {
        // 1. Definimos la semana ACTUAL (basada en el reloj del sistema, no en el gasto)
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemanaActual = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemanaActual = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        // 2. FILTRO VITAL: Si el gasto que intentas meter NO es de esta semana,
        // la alerta no tiene sentido. Devolvemos false (no bloquea/no avisa).
        LocalDate fechaGasto = nuevoGasto.getFecha();
        if (fechaGasto.isBefore(inicioSemanaActual) || fechaGasto.isAfter(finSemanaActual)) {
            return false; 
        }

        // 3. Comprobación de Categoría (igual que antes)
        if(alerta.getCategoria() != null) {
            if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                return false;
            }
        }
        
        double limite = alerta.getLimite();
        
        // 4. Sumamos. Empezamos con el importe del nuevo gasto (porque ya sabemos que es de esta semana)
        double totalGastado = nuevoGasto.getImporte();
        
        // 5. Recorremos los gastos YA guardados que coincidan con ESTA semana actual
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
        
        return totalGastado > limite;
    }
}
/*
package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

public class EstrategiaSemanal implements InterfaceAlerta{

	
	@Override
	public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto nuevoGasto) {
		double limite = alerta.getLimite();
		LocalDate fechaGasto = nuevoGasto.getFecha();
		
		// Si la alerta tiene una categoría que no coincide con la del gasto, devolver falso para no bloquear
		if(alerta.getCategoria() != null) {
			if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
				return false;
			}
		}
		
		LocalDate inicioSemana = fechaGasto.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate finSemana = fechaGasto.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		
		double totalGastado = nuevoGasto.getImporte();
		for(Gasto g: cuenta.getGastos()) {
			boolean enSemana = !g.getFecha().isBefore(inicioSemana) && !g.getFecha().isAfter(finSemana);
			
			boolean mismaCategoria = true;
			if(alerta.getCategoria() != null) {
				mismaCategoria = alerta.getCategoria().getNombre().equalsIgnoreCase(g.getCategoria().getNombre());
			}
			
			if(enSemana && mismaCategoria) {
				totalGastado += g.getImporte();
			}
		}
		
		return totalGastado > limite;
	}
}
*/