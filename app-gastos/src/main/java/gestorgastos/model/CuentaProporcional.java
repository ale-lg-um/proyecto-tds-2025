package gestorgastos.model;

import java.util.HashMap;
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
    
    
    // Sobrescribimos el método del padre
    @Override
    public Map<String, Double> calcularSaldos() {
        double totalGastado = gastos.stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();

        Map<String, Double> saldos = new HashMap<>();
        Map<String, Double> porcentajes = getPorcentajes(); // Asumo que tienes este getter

        for (String miembro : super.miembros) {
            double pagado = gastos.stream()
                                  .filter(g -> miembro.equals(g.getPagador()))
                                  .mapToDouble(Gasto::getImporte)
                                  .sum();

            // En la proporcional, mi cuota depende de mi porcentaje
            double miPorcentaje = porcentajes.getOrDefault(miembro, 0.0);
            double miCuota = totalGastado * (miPorcentaje / 100.0);

            saldos.put(miembro, pagado - miCuota);
        }
        return saldos;
    }
}