package gestorgastos.model;

public class Categoria {

	private String nombre;
	private String descripcion; // Opcional, pero útil

	// Constructor vacío para Jackson
	public Categoria() {
	}

	public Categoria(String nombre) {
		this.nombre = nombre;
		this.descripcion = "";
	}

	public Categoria(String nombre, String descripcion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	public String getNombre() {
		return nombre;
	}

	// El toString es vital para que salga bien el nombre en los desplegables
	// (ComboBox) de la vista
	@Override
	public String toString() {
		return nombre;
	}

	// Importante para comparar si dos categorías son iguales
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Categoria categoria = (Categoria) obj;
		return nombre.equals(categoria.nombre);
	}
}