package gestorgastos.model;

public class Categoria {
    
    private String nombre;
    private String descripcion;
    private String colorHex; // Nuevo campo para el color (ej: "#FFFFFF")

    public Categoria() {
        // Constructor vacío para Jackson
    }
    
    // Constructor simple (por defecto gris/blanco)
    public Categoria(String nombre) {
        this(nombre, "", "#FFFFFF"); 
    }

    public Categoria(String nombre, String descripcion, String colorHex) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.colorHex = colorHex == null ? "#FFFFFF" : colorHex;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categoria categoria = (Categoria) obj;
        return nombre != null && nombre.equals(categoria.nombre);
    }
}