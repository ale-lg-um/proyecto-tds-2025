package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;


public class EstrategiaMensual implements InterfaceAlerta{
	/*@Override
	public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto) {
		double limite = alerta.getLimite();
        LocalDate fechaGasto = gasto.getFecha();

        double totalGastado = 0;

        for(Gasto g : cuenta.getGastos()){
            // Primero vemos que si coinciden en fecha
            boolean mismoMes = g.getFecha().getMonth() == fechaGasto.getMonth() &&
                               g.getFecha().getYear() == fechaGasto.getYear();
            if (mismoMes){
                // Miramos si hay categoria o si coincide 
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
		
		// Si la alerta tiene categoría, pero no coincide con la del gasto, devuelve false
		if(alerta.getCategoria() != null) {
			if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
				return false;
			}
		}
		
		double totalGastado = nuevoGasto.getImporte(); // Empezamos sumando lo que ya se ha gastado
		
		for (Gasto g : cuenta.getGastos()) {
			boolean mismoMes = g.getFecha().getMonth() == fechaGasto.getMonth() && g.getFecha().getYear() == fechaGasto.getYear();
			boolean mismaCategoria = true;
			if(alerta.getCategoria() != null) {
				mismaCategoria = alerta.getCategoria().getNombre().equalsIgnoreCase(g.getCategoria().getNombre());
			}
			
			if(mismoMes && mismaCategoria) {
				totalGastado += g.getImporte();
			}
		}
		return totalGastado > limite;
	}
}

