package gestorgastos.strategies;

import gestorgastos.model.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

public class EstrategiaSemanal implements InterfaceAlerta{
	@Override
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
	}
}
