package gestorgastos.strategies;

// Corrección segundo PR
public class FactoriaAlertas {
	public static InterfaceAlerta getEstrategia(String tipo) {
		if(tipo == null) return null;
		
		if("SEMANAL".equalsIgnoreCase(tipo)) {
			return new EstrategiaSemanal();
		} else if("MENSUAL".equalsIgnoreCase(tipo)) {
			return new EstrategiaMensual();
		}
		
		// Por defecto, estrategias mensuales
		return new EstrategiaMensual();
	}
}
