package gestorgastos.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipo", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = CuentaPersonal.class, name = "PERSONAL"),
		@JsonSubTypes.Type(value = CuentaCompartida.class, name = "COMPARTIDA"),
		@JsonSubTypes.Type(value = CuentaProporcional.class, name = "ESPECIAL") })

// Clase abstracta que se usará para implementar las cuentas personales y compartidas
public abstract class Cuenta {

	protected String id;
	protected String nombre;
	protected List<Gasto> gastos = new ArrayList<>();;
	private java.util.List<Alerta> alertas = new java.util.ArrayList<>();
	// En Cuenta.java


	private List<Notificacion> notificaciones = new ArrayList<>();

	// Cada cuenta tiene una lista de categorías
	protected List<Categoria> categorias = new ArrayList<>();

	public Cuenta() {
		this.id = UUID.randomUUID().toString();
		//this.gastos = new ArrayList<>();
		//this.categorias = new ArrayList<>();
		// Inicializamos siempre con General para evitar errores
		this.categorias.add(new Categoria("General", "Gastos varios", "#D3D3D3"));
	}

	public Cuenta(String nombre) {
		//this.id = UUID.randomUUID().toString();
		this();
		this.nombre = nombre;
		//this.gastos = new ArrayList<>();
		//this.categorias = new ArrayList<>();
		// Categoría por defecto al crear cuenta nueva
		//this.categorias.add(new Categoria("General", "Gastos varios", "#D3D3D3"));
	}

	// Obtener y establecer tipo de la cuenta
	public abstract String getTipo();

	public void setTipo(String tipo) {
	}

	public void agregarGasto(Gasto gasto) {
		this.gastos.add(gasto);
	}

	public void eliminarGasto(Gasto gasto) {
		this.gastos.remove(gasto);
	}

	// Getters y Setters
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
	}

	public List<Gasto> getGastos() {
		return gastos;
	}

	/*public void setGastos(List<Gasto> gastos) {
		this.gastos = gastos;
	}*/
	
	public List<Categoria> getCategorias() {
		return categorias;
	}

	/*public void setCategorias(List<Categoria> categorias) {
		this.categorias = categorias;
	}*/
	
	public void agregarCategoria(Categoria categoria) {
		this.categorias.add(categoria);
	}

	public java.util.List<Alerta> getAlertas() {
		return alertas;
	}

	/*public void setAlertas(java.util.List<Alerta> alertas) {
		this.alertas = alertas;
	}*/
	
	public void agregarAlerta(Alerta alerta) {
		this.alertas.add(alerta);
	}

	public java.util.List<Notificacion> getNotificaciones() {
		return notificaciones;
	}

	/*public void setNotificaciones(java.util.List<Notificacion> notificaciones) {
		this.notificaciones = notificaciones;
	}*/
	
	public void agregarNotificacion(Notificacion notificacion) {
		this.notificaciones.add(notificacion);
	}
	
	// Añadir notificaciones cuando salta una alerta
	public void anadirNotificacion(String notificacion) {
		
		if (this.notificaciones == null) {
	        this.notificaciones = new ArrayList<>();
	        
	    }

		Notificacion n = new Notificacion(notificacion);
		
		notificaciones.add(n);
	    
	}

	@Override
	public String toString() {
		return nombre;
	}
	

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuenta cuenta = (Cuenta) o;
        return id != null && id.equals(cuenta.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
