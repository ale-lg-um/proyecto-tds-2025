package gestorgastos.strategies;

import gestorgastos.model.Alerta;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;

public interface InterfaceAlerta {
	boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto);
	
	
}
