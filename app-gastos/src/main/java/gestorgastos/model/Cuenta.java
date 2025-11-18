package gestorgastos.model;


public class Cuenta {
    private String nombre;

    public Cuenta(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}
