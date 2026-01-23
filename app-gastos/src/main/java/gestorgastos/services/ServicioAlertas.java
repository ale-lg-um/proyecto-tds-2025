package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.model.Alerta; // Asumo que tienes esta clase
import gestorgastos.model.Notificacion; // Asumo que tienes esta clase
import gestorgastos.strategies.EstrategiaMensual;
import gestorgastos.strategies.EstrategiaSemanal;
import gestorgastos.strategies.FactoriaAlertas;
import gestorgastos.strategies.InterfaceAlerta;

import java.time.LocalDate;

public class ServicioAlertas {

    /**
     * Devuelve el mensaje de error si salta una alerta, o null si todo está bien.
     * Si salta alerta, este método se encarga de guardar la notificación.
     */	
	// Corrección tercer PR
	public Alerta comprobarAlertas(Cuenta cuenta, Gasto nuevoGasto) {
		if(cuenta.getAlertas() == null) {
			return null;
		}
		
		for(Alerta alerta : cuenta.getAlertas()) {
			// Si la alerta es de una categoría, y el gasto es de otra categoría, comprobammos con otra alerta
			/*if(alerta.getCategoria() != null) {
				String catAlerta = alerta.getCategoria().getNombre();
				String catGasto = nuevoGasto.getCategoria().getNombre();
				
				// Si las categorias no son iguales, pasamos
				if(!catAlerta.equalsIgnoreCase(catGasto)) {
					continue;
				}
			}*/
			
			// Corrección cuarto PR
			if(!alerta.esAplicablePara(nuevoGasto)) {
				continue;
			}
			
			/*InterfaceAlerta estrategia;
			if("SEMANAL".equalsIgnoreCase(alerta.getTipo())) {
				estrategia = new EstrategiaSemanal();
			} else {
				estrategia = new EstrategiaMensual();
			}*/
			
			InterfaceAlerta estrategia = FactoriaAlertas.getEstrategia(alerta.getTipo());
			
			if(estrategia.verificarLimite(alerta, cuenta, nuevoGasto)) {
				// Corrección tercer PR
				/*String nombreCat = (alerta.getCategoria() != null) ? alerta.getCategoria().getNombre() : "General";
				
				String mensaje = "Has superado el límite de " + alerta.getLimite() + "€ definido en tu alerta.";
				
				cuenta.anadirNotificacion(mensaje);
				return mensaje;*/
				
				return alerta;
			}
		}
		return null; // No salta alerta
	}
}