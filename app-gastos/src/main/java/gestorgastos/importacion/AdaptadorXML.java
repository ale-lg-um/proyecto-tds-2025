package gestorgastos.importacion;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gestorgastos.dto.GastoTemporal;

public class AdaptadorXML extends Importador{
private final List<DateTimeFormatter> formatters = Arrays.asList(
		DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),
		DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),
		DateTimeFormatter.ofPattern("yyyy-mm-dd H:mm:ss"),
		DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"),
		DateTimeFormatter.ofPattern("M/d/yy H:mm"),
		DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),
		DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"),
		DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss")
		);
	
	@Override
	public List<GastoTemporal> leerFichero(String ruta) throws Exception {
		List<GastoTemporal> lista = new ArrayList<>();
		File archivo = new File(ruta);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document doc = builder.parse(archivo);
		doc.getDocumentElement().normalize();
		
		NodeList nodos = doc.getDocumentElement().getChildNodes();
		
		for(int i = 0; i < nodos.getLength(); i++) {
			Node nodo = nodos.item(i);
			if(nodo.getNodeType() == Node.ELEMENT_NODE) {
				Element elemento = (Element) nodo;
				try {
					GastoTemporal temporal = parsearElemento(elemento);
					if(temporal != null) {
						lista.add(temporal);
					}
				} catch (Exception e) {
					System.err.println("Error al extraer XML: " + e.getMessage());
				}
			}
		}
		return lista;
	}
	
	@Override
	protected GastoTemporal parsearGasto(String linea) throws Exception {
		return null;
	}
	
	private GastoTemporal parsearElemento(Element elemento) throws Exception {
		String fecha = obtenerValor("Date", elemento);
		String cuenta = obtenerValor("Account", elemento);
		String cat = obtenerValor("Subcategory", elemento);
		String concepto = obtenerValor("Note", elemento);
		String pagador = obtenerValor("Payer", elemento);
		String importe = obtenerValor("Amount", elemento);
		
		if(fecha.isEmpty() || cuenta.isEmpty()) {
			return null;
		}
		
		if(pagador.equalsIgnoreCase("Me")) {
			pagador = "Yo";
		}
		
		LocalDateTime fechaHora = parsearFechaRobusta(fecha);
		double importeDouble = 0.0;
		if(!importe.isEmpty()) {
			importeDouble = Double.parseDouble(importe.replace(",", "."));
		}
		
		return new GastoTemporal(cuenta, concepto, importeDouble, cat, pagador, fechaHora.toLocalDate(), fechaHora.toLocalTime());
	}
	
	private String obtenerValor(String etiqueta, Element elemento) {
		NodeList nodos = elemento.getElementsByTagName(etiqueta);
		if(nodos != null && nodos.getLength() > 0) {
			return nodos.item(0).getTextContent().trim();
		}
		return "";
	}
	
	private LocalDateTime parsearFechaRobusta(String fechaStr) throws Exception {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        String fechaLimpia = fechaStr.trim();

        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(fechaLimpia, fmt);
            } catch (Exception e) {
                // Ignorar y probar el siguiente
            }
        }
        throw new Exception("Formato de fecha desconocido: " + fechaStr);
    }
}
