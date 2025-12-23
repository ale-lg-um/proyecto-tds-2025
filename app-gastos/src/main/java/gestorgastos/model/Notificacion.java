package gestorgastos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacion {
    
    private String mensaje;
    
    // CAMBIO 1: La fecha ahora es un String
    private String fecha; 
    
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String mensaje) {
        this.mensaje = mensaje;
        // CAMBIO 2: Convertimos la fecha a texto al crearla
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.leida = false;
    }
    
    // CONSTRUCTOR EXTRA (Útil si creas la notificación desde fuera con fecha)
    public Notificacion(String titulo, String mensaje, java.time.LocalDate fechaIgnorada) {
         this.mensaje = mensaje;
         this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         this.leida = false;
    }

    // Getters y Setters
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    // CAMBIO 3: Getter y Setter de tipo String
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    // CAMBIO 4: Arreglo del bucle infinito que tenías antes
    public boolean isLeida() { return this.leida; } 
    public void setLeida(boolean leida) { this.leida = leida; }

    @Override
    public String toString() {
        return "[" + fecha + "] " + mensaje;
    }
}
