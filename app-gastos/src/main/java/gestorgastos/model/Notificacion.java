package gestorgastos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Notificacion {
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion(){}

    public Notificacion(String mensaje){
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    // Getters y Setters

    public String getMensaje(){
        return mensaje;
    }
    public void setMensaje(String mensaje){
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha(){
        return fecha;
    }
    public void setFecha(LocalDateTime fecha){
        this.fecha = fecha;
    }

    public boolean isLeida(){
        return isLeida();
    }
    public void setLeida(boolean leida){
        this.leida = leida;
    }

    @Override
    public String toString(){
        return "[" + fecha.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + "] "+ mensaje;
    }




}
