package gestorgastos.model;

public class Usuario {
	private String nombre;

	// Constructor vacío para JSON
	public Usuario() {
	}

	public Usuario(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
