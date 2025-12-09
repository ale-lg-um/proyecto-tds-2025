package gestorgastos.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.EXISTING_PROPERTY, 
    property = "tipo", 
    visible = true
)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = CuentaPersonal.class, name = "PERSONAL"),
    @JsonSubTypes.Type(value = CuentaCompartida.class, name = "COMPARTIDA"),
    @JsonSubTypes.Type(value = CuentaProporcional.class, name = "ESPECIAL") 
})
public abstract class Cuenta {

    protected String id;
    protected String nombre;
    protected List<Gasto> gastos;
    
    // NUEVO: Cada cuenta tiene su propia lista de categorías
    protected List<Categoria> categorias;

    public Cuenta() {
        this.gastos = new ArrayList<>();
        this.categorias = new ArrayList<>();
        // Inicializamos siempre con General para evitar errores
        this.categorias.add(new Categoria("General", "Gastos varios", "#D3D3D3"));
    }

    public Cuenta(String nombre) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.gastos = new ArrayList<>();
        this.categorias = new ArrayList<>();
        // Categoría por defecto al crear cuenta nueva
        this.categorias.add(new Categoria("General", "Gastos varios", "#D3D3D3"));
    }

    public abstract String getTipo();

    public void setTipo(String tipo) { }

    public void agregarGasto(Gasto gasto) {
        this.gastos.add(gasto);
    }

    public void eliminarGasto(Gasto gasto) {
        this.gastos.remove(gasto);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Gasto> getGastos() { return gastos; }
    public void setGastos(List<Gasto> gastos) { this.gastos = gastos; }

    // NUEVOS GETTERS Y SETTERS PARA CATEGORÍAS (Vital para Jackson)
    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }

    @Override
    public String toString() { return nombre; }
}