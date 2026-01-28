
package gestorgastos.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ESPECIAL")
public class CuentaProporcional extends CuentaCompartida {

    private Map<String, Double> porcentajes; 
    
    public CuentaProporcional() { super(); }

    public CuentaProporcional(String nombre, List<String> miembros, Map<String, Double> porcentajes) {
        super(nombre, miembros);
        this.porcentajes = porcentajes;
    }
    
    @Override
    public String getTipo() { return "ESPECIAL"; }

    
    @Override
    protected double calcularCuotaTeorica(String miembro, double totalGastado) {
        // En la proporcional, la cuota depende del porcentaje
        double miPorcentaje = porcentajes.getOrDefault(miembro, 0.0);
        return totalGastado * (miPorcentaje / 100.0);
    }
    
    // Getter
    public Map<String, Double> getPorcentajes() { return porcentajes; }
}