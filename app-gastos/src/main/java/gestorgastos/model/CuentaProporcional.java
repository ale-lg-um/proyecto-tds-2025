package gestorgastos.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ESPECIAL")

// Esta clase se refiere a las cuentas compartidas en las que podemos establecer qué porcentaje de la cuenta pertenece a cada uno de los usuarios que participan en ella
// Deriva de las cuentas compartidas equitativas
public class CuentaProporcional extends CuentaCompartida {

    // Mapa que relaciona a cada usuario dentro de la cuenta con el porcentaje de la cuenta que pertenece a ese usuario
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
    
    // Redefinición de métodos de la clase padre para tener en cuenta los porcentajes desiguales
    @Override
    public String getTipo() {
        return "ESPECIAL";
    }
    
    @Override
    public Map<String, Double> calcularSaldos() {
        double totalGastado = gastos.stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();

        Map<String, Double> saldos = new HashMap<>();
        Map<String, Double> porcentajes = getPorcentajes();

        for (String miembro : super.miembros) {
            double pagado = gastos.stream()
                                  .filter(g -> miembro.equals(g.getPagador()))
                                  .mapToDouble(Gasto::getImporte)
                                  .sum();

            // En la cuenta compartida especial, mi cuota depende de mi porcentaje
            double miPorcentaje = porcentajes.getOrDefault(miembro, 0.0);
            double miCuota = totalGastado * (miPorcentaje / 100.0);

            saldos.put(miembro, pagado - miCuota);
        }
        return saldos;
    }
}