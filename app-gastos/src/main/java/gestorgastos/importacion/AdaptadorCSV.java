package gestorgastos.importacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import gestorgastos.model.GastoTemporal;

public class AdaptadorCSV extends Importador{
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
	
	@Override
	public List<GastoTemporal> leerFichero(String ruta) throws Exception {
		List<GastoTemporal> lista = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			boolean primera = true;
			while((linea = br.readLine())!= null) {
				if(primera) {
					primera = false;
					continue; // Saltamos la cabecera
				}
				
				if(linea.trim().isEmpty()) {
					continue;
				}
				
				try {
					GastoTemporal temp = parsearGasto(linea);
					if(temp != null) lista.add(temp);;
				} catch(Exception e) {
					System.err.println("Error en la líea: " + linea + " -> " + e.getMessage());
				}
			}
		}
		return lista;
	}
	
	@Override
	protected GastoTemporal parsearGasto(String linea) throws Exception {
		String[] datos = linea.split(",");
		if(datos.length < 7) { // Si los datos no tienen todos los campos
			return null;
		}
		
		LocalDateTime fechaHora = LocalDateTime.parse(datos[0].trim(), formatter);
		String cuenta = datos[1].trim();
		String cat = datos[3].trim();
		String concepto = datos[4].trim();
		String pagador = datos[5].trim();
		if(pagador.equalsIgnoreCase("Me")) {
			pagador = "Yo";
		}
		double importe = Double.parseDouble(datos[6].trim().replace(",", "."));
		
		return new GastoTemporal(cuenta, concepto, importe, cat, pagador, fechaHora.toLocalDate(), fechaHora.toLocalTime());
	}
}
