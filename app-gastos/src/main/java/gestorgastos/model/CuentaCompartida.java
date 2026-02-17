
package gestorgastos.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("COMPARTIDA")
public class CuentaCompartida extends Cuenta {

    protected List<String> miembros;
    
    public CuentaCompartida() { super(); }

    public CuentaCompartida(String nombre, List<String> miembrosIniciales) {
        super(nombre);
        this.miembros = new ArrayList<>(miembrosIniciales);
    }
    
    public List<String> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }
    
    @Override
    public String getTipo() { return "COMPARTIDA"; }
    
   
    
    // Este método contiene la lógica común (el bucle, la suma, etc.)
    // NO SE TOCA en la clase hija.
    public Map<String, Double> calcularSaldos() {
        double totalGastado = gastos.stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();

        Map<String, Double> saldos = new HashMap<>();
        
        for (String miembro : miembros) {
            // 1. Calculamos lo pagado (Lógica común)
            double pagado = gastos.stream()
                                  .filter(g -> miembro.equals(g.getPagador()))
                                  .mapToDouble(Gasto::getImporte)
                                  .sum();
            
            // 2. Pedimos la cuota (Esta parte es polimórfica, cambia según la clase)
            double cuota = calcularCuotaTeorica(miembro, totalGastado);
            
            saldos.put(miembro, pagado - cuota);
        }
        return saldos;
    }

    // Este es el método que define el comportamiento por defecto (equitativo)
    // Protected para que el hijo pueda acceder/sobreescribir si quiere
    protected double calcularCuotaTeorica(String miembro, double totalGastado) {
        if (miembros.isEmpty()) return 0.0;
        return totalGastado / miembros.size();
    }
}