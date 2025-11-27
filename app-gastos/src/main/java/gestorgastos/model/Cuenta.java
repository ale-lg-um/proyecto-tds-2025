package gestorgastos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Para tener un ID único interno

public abstract class Cuenta {
    
    // "protected" para que las clases hijas (Personal/Compartida) puedan acceder
    protected String id;
    protected String nombre;
    protected List<Gasto> gastos; 

    public Cuenta(String nombre) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.gastos = new ArrayList<>();
    }

    // Métodos comunes
    public void agregarGasto(Gasto gasto) {
        this.gastos.add(gasto);
    }

    public void eliminarGasto(Gasto gasto) {
        this.gastos.remove(gasto);
    }

    public String getNombre() { return nombre; }
    public List<Gasto> getGastos() { return gastos; }

    // Este toString es el que usará tu ListView para mostrar el nombre
    @Override
    public String toString() {
        return nombre; 
    }
}