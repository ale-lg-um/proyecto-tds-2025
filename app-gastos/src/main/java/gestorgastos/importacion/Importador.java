package gestorgastos.importacion;

import java.util.List;

import gestorgastos.dto.GastoTemporal;

// Clase abstracta de la que derivan los adaptadores
public abstract class Importador {
	public abstract List<GastoTemporal> leerFichero(String ruta) throws Exception;
	
	protected abstract GastoTemporal parsearGasto(String linea) throws Exception;
}
