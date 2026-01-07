package gestorgastos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacion {
    
    private String mensaje;
    
   // Implementamos la fecha como una cadena para poder incluirla en la notificación
    private String fecha; 
    
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String mensaje) {
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.leida = false;
    }
    
    // Si la notificacióon es creada con título y fecha
    public Notificacion(String titulo, String mensaje, java.time.LocalDate fechaIgnorada) {
         this.mensaje = mensaje;
         this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         this.leida = false;
    }

    // Getters y Setters
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public boolean isLeida() { return this.leida; } 
    public void setLeida(boolean leida) { this.leida = leida; }

    @Override
    public String toString() {
        return "[" + fecha + "] " + mensaje;
    }
}
