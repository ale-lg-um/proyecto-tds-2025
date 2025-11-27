package gestorgastos.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// CAMBIO CLAVE: Usamos EXISTING_PROPERTY. 
// Esto dice: "El tipo no es mágico, es un método real de la clase llamado 'tipo'".
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipo", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = CuentaPersonal.class, name = "PERSONAL"),
		@JsonSubTypes.Type(value = CuentaCompartida.class, name = "COMPARTIDA"),
		@JsonSubTypes.Type(value = CuentaProporcional.class, name = "ESPECIAL") })
public abstract class Cuenta {

	protected String id;
	protected String nombre;
	protected List<Gasto> gastos;

	public Cuenta() {
		this.gastos = new ArrayList<>();
	}

	public Cuenta(String nombre) {
		this.id = UUID.randomUUID().toString();
		this.nombre = nombre;
		this.gastos = new ArrayList<>();
	}

	// --- EL TRUCO MAESTRO ---
	// Obligamos a todas las cuentas a decir qué tipo son.
	// Al ser un "getTipo", Jackson lo guardará en el JSON sí o sí.
	public abstract String getTipo();
	// ------------------------

	public void agregarGasto(Gasto gasto) {
		this.gastos.add(gasto);
	}

	public void eliminarGasto(Gasto gasto) {
		this.gastos.remove(gasto);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	} // Setter necesario

	public List<Gasto> getGastos() {
		return gastos;
	}

	public void setGastos(List<Gasto> gastos) {
		this.gastos = gastos;
	} // Setter necesario

	@Override
	public String toString() {
		return nombre;
	}

	// --- AÑADE ESTO PARA QUE JACKSON NO SE QUEJE AL LEER ---
	public void setTipo(String tipo) {
		// No hacemos nada con este dato, pero el método tiene que existir
		// para que Jackson pueda "escribir" la propiedad al leer el JSON.
	}

	// ...
}