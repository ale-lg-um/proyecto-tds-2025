package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.model.Alerta; // Asumo que tienes esta clase
import gestorgastos.model.Notificacion; // Asumo que tienes esta clase
import java.time.LocalDate;

public class ServicioAlertas {

    /**
     * Devuelve el mensaje de error si salta una alerta, o null si todo está bien.
     * Si salta alerta, este método se encarga de guardar la notificación.
     */
    public String comprobarAlertas(Cuenta cuenta, Gasto nuevoGasto) {
        
        // Calculamos el total gastado actual de la cuenta
        double totalGastado = cuenta.getGastos().stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();
        
        double totalFuturo = totalGastado + nuevoGasto.getImporte();

        // Recorremos las alertas configuradas en la cuenta
        // (Asumiendo que cuenta.getAlertas() devuelve una lista de tus objetos Alerta)
        for (Alerta alerta : cuenta.getAlertas()) {
            
            // LÓGICA: Si el futuro total supera el límite de la alerta
            if (totalFuturo > alerta.getLimite()) {
                
                // 1. Preparar el mensaje
                String mensaje = "Has superado el límite de " + alerta.getLimite() + "€ definido en tu alerta.";

                // 2. CREAR Y GUARDAR LA NOTIFICACIÓN
                // Asumo que Notificacion tiene un constructor (titulo, mensaje, fecha)
                cuenta.anadirNotificacion(mensaje);
                
                // 3. Devolvemos el mensaje para que el controlador muestre el Pop-up
                return mensaje;
            }
        }

        return null; // No ha saltado ninguna alerta
    }
}