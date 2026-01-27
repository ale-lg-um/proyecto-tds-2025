package gestorgastos.importacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gestorgastos.dto.GastoTemporal;

public class AdaptadorJSON extends Importador{
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
	
	@Override
	public List<GastoTemporal> leerFichero(String ruta) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<GastoTemporal> listaResultado = new ArrayList<>();
		
		JsonNode rootNode = mapper.readTree(new File(ruta));
		
		if(rootNode.isArray()) {
			for(JsonNode node : rootNode) {
				try {
					String dateStr = node.get("Date").asText();
					String account = node.get("Account").asText();
					String note = node.get("Note").asText();
					String payer = node.get("Payer").asText();
					String subcat = node.get("Subcategory").asText();
					double amount = node.get("Amount").asDouble();
					
					if(payer.equalsIgnoreCase("Me")) {
						payer= "Yo";
					}
					
					LocalDateTime fechaCompleta = LocalDateTime.parse(dateStr, formatter);
					
					GastoTemporal temporal = new GastoTemporal(account, note, amount, subcat, payer, fechaCompleta.toLocalDate(), fechaCompleta.toLocalTime());
					
					listaResultado.add(temporal);
				} catch (Exception e) {
					System.err.println("Error JSON: " + e.getMessage());
				}
			}
		}
		return listaResultado;
	}
	
	@Override
	protected GastoTemporal parsearGasto(String linea) {
		return null;
	}
}
