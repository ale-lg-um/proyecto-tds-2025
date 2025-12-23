package gestorgastos.services;

import gestorgastos.model.*;
import gestorgastos.strategies.*;


public class ServicioAlertas {

		public void comprobarAlertas(Cuenta cuenta , Gasto nuevoGasto){
            if ( cuenta.getAlertas() == null) return;

            InterfaceAlerta estrategia = null;

            for(Alerta alerta : cuenta.getAlertas()){
                String tipo = alerta.comprobarAlertas(alerta);
                if (tipo == "SEMANAL"){
                    estrategia = new EstrategiaSemanal();
                } else if(tipo == "MENSUAL") {
                	estrategia = new EstrategiaMensual();
                }
                
                boolean superado = estrategia.verificarLimite(alerta, cuenta, nuevoGasto);
                
                if(superado){
                    String cat = (alerta.getCategoria() == null) ? "Total" : alerta.getCategoria().getNombre();
                    String msg = "⚠️ Límite "+ alerta.getTipo() + " superado en " + cat +
                    " (Tope: "+ alerta.getLimite() + " €)";
                    
                    cuenta.anadirNotificacion(msg);
                        
                
                }
            }

            
        }
}
