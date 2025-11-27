package gestorgastos.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ESPECIAL")
public class CuentaProporcional extends CuentaCompartida {

    // Mapa: "NombrePersona" -> 30.0 (porcentaje)
    private Map<String, Double> porcentajes; 
    
    public CuentaProporcional() {
        super();
    }

    public CuentaProporcional(String nombre, List<String> miembros, Map<String, Double> porcentajes) {
        super(nombre, miembros);
        this.porcentajes = porcentajes;
    }

    public Map<String, Double> getPorcentajes() {
        return porcentajes;
    }
    
    @Override
    public String getTipo() {
        return "ESPECIAL";
    }
    
    // Aquí podrías validar que los porcentajes sumen 100%
}