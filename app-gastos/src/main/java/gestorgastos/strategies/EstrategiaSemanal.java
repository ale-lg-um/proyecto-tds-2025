package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

public class EstrategiaSemanal implements InterfaceAlerta{
	/*@Override
	public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto) {
		double limite = alerta.getLimite();
        LocalDate fechaGasto = gasto.getFecha();

        LocalDate inicioSemana = fechaGasto.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = fechaGasto.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        double totalGastado = 0;

        for(Gasto g : cuenta.getGastos()){
            // Primero vemos que si coinciden en fecha
            boolean enSemana = !g.getFecha().isBefore(inicioSemana) && !g.getFecha().isAfter(finSemana);
            if (enSemana){
                if((alerta.getCategoria() == null) || (alerta.getCategoria().getNombre().equals(gasto.getCategoria().getNombre()))){
                       totalGastado += g.getImporte(); 
                }
            }
        }
        
        return totalGastado > limite;
	}*/
	
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
