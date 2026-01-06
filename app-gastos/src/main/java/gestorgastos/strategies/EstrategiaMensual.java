package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;

public class EstrategiaMensual implements InterfaceAlerta {

    @Override
    public boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto nuevoGasto) {
        // 1. Referencia: HOY
        LocalDate hoy = LocalDate.now();
        LocalDate fechaGasto = nuevoGasto.getFecha();

        // 2. FILTRO VITAL: Si el gasto no es de este mes Y de este año, adiós.
        boolean esEsteMes = (fechaGasto.getMonth() == hoy.getMonth()) && (fechaGasto.getYear() == hoy.getYear());
        
        if (!esEsteMes) {
            return false; // Es un gasto antiguo o futuro, no afecta al límite mensual actual.
        }

        // 3. Comprobación de Categoría
        if(alerta.getCategoria() != null) {
            if(!alerta.getCategoria().getNombre().equalsIgnoreCase(nuevoGasto.getCategoria().getNombre())) {
                return false;
            }
        }
        
        double limite = alerta.getLimite();
        double totalGastado = nuevoGasto.getImporte(); 
        
        // 4. Sumar gastos que pertenezcan al MES ACTUAL
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
        
        return totalGastado > limite;
    }
}
/*
 
package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;


public class EstrategiaMensual implements InterfaceAlerta{

	
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
*/
