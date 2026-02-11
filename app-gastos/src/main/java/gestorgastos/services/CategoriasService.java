package gestorgastos.services;

import gestorgastos.model.Categoria;

public class CategoriasService {
	private static CategoriasService instancia;
	
	public static CategoriasService getInstancia() {
		if (instancia == null)
			instancia = new CategoriasService();
		return instancia;
	}
	
	public static Categoria crearCategorias(String nombre, String st,String hex) {
		return new Categoria(nombre, st, hex);
	}

}
