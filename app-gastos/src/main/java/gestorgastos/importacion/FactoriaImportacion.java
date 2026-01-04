package gestorgastos.importacion;

public class FactoriaImportacion {
	public static Importador getImportador(String rutaArchivo) {
		if(rutaArchivo == null) return null;
		String ruta = rutaArchivo.toLowerCase();
		
		if(ruta.endsWith(".csv")) {
			return new AdaptadorCSV();
		} else if(ruta.endsWith(".txt")) {
			return new AdaptadorTXT();
		} else if(ruta.endsWith(".json")) {
			return new AdaptadorJSON();
		} else if(ruta.endsWith(".xlsx")) {
			return new AdaptadorExcel();
		} else if(ruta.endsWith(".xml")) {
			return new AdaptadorXML();
		}
		
		return null;
	}
}
