package gestorgastos.strategies;

import gestorgastos.model.Alerta;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;

// Interfaz que utilizarán las estrategias diseñadas para los distintos tipos de alerta.
public interface InterfaceAlerta {
	boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto);
}
