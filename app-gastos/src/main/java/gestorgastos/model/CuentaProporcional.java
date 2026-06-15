
package gestorgastos.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ESPECIAL")
public class CuentaProporcional extends CuentaCompartida {

    private Map<String, Double> porcentajes; 
    
    public CuentaProporcional() { super(); }

    public CuentaProporcional(String nombre, List<String> miembros, Map<String, Double> porcentajes) {
        super(nombre, new ArrayList<>(porcentajes.keySet()));
        validarPorcentajes(porcentajes);
        this.porcentajes = new HashMap<>(porcentajes);
    }
    
    @Override
    public String getTipo() { return "ESPECIAL"; }

    
    @Override
    protected double calcularCuotaTeorica(String miembro, double totalGastado) {
        // En la proporcional, la cuota depende del porcentaje
        double miPorcentaje = porcentajes.getOrDefault(miembro, 0.0);
        return totalGastado * (miPorcentaje / 100.0);
    }
    
	private void validarPorcentajes(Map<String, Double> porcentajes) {
		if (porcentajes == null || porcentajes.isEmpty()) {
			throw new IllegalArgumentException("Debe añadir miembros con sus porcentajes.");
		}

		double sumaTotal = porcentajes.values().stream().mapToDouble(Double::doubleValue).sum();

		// Margen de error para decimales
		if (Math.abs(sumaTotal - 100.0) > 0.01) {
			throw new IllegalArgumentException(String.format("Los porcentajes deben sumar 100%% (Actual: %.2f%%)", sumaTotal));
		}
	}
    
    // Getter
    public Map<String, Double> getPorcentajes() { return porcentajes; }
}