package gestorgastos.importacion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import gestorgastos.dto.GastoTemporal;

public class AdaptadorTXT extends Importador {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
	
	@Override
	public List<GastoTemporal> leerFichero(String ruta) throws Exception {
		List<GastoTemporal> lista = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea;
			while((linea = br.readLine()) != null) {
				if(linea.trim().isEmpty()) continue;
				try {
					GastoTemporal temp = parsearGasto(linea);
					if(temp != null) lista.add(temp);
				} catch (Exception e) {
					// Ignora errores
				}
			}
		}
		return lista;
	}
	
	@Override
	protected GastoTemporal parsearGasto(String linea) {
		String[] gasto = linea.split("\\|");
		if(gasto.length < 7) return null;
		LocalDateTime fechaHora = LocalDateTime.parse(gasto[0].trim(), formatter);
		String cuenta = gasto[1].trim();
		String cat = gasto[3].trim();
		String concepto = gasto[4].trim();
		String pagador = gasto[5].trim().equalsIgnoreCase("Me") ? "Yo" : gasto[5].trim();
		double importe = Double.parseDouble(gasto[6].trim().replace(",", "."));
		
		if(pagador.equalsIgnoreCase("Me")) {
			pagador = "Yo";
		}
		
		return new GastoTemporal(cuenta, concepto, importe, cat, pagador, fechaHora.toLocalDate(), fechaHora.toLocalTime());
	}
}
