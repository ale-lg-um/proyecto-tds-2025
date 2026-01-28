package gestorgastos.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("COMPARTIDA")

// Esta clase se refiere a las cuentas compartidas en las que el procentaje de la cuenta es el mismo para todos los usuarios que están dentro de la cuenta.
public class CuentaCompartida extends Cuenta {

    protected List<String> miembros; // Nombres de las personas del grupo
    
    public CuentaCompartida() {
        super();
    }

    public CuentaCompartida(String nombre, List<String> miembrosIniciales) {
        super(nombre);
        // Creamos una copia nueva para evitar problemas externos
        this.miembros = new ArrayList<>(miembrosIniciales);
    }
    public List<String> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }
    
    @Override
    public String getTipo() {
        return "COMPARTIDA";
    }
    
    public Map<String, Double> calcularSaldos() {
        // Total de gastos en la cuenta
        double totalGastado = gastos.stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();

        // Calcular lo que debe pagar cada miembro
        int numMiembros = miembros.size();
        if (numMiembros == 0) return new HashMap<>();
        double cuotaPorPersona = totalGastado / numMiembros;

        // Calcular el saldo de cada miembro
        Map<String, Double> saldos = new HashMap<>();
        
        for (String miembro : miembros) {
            // Cuánto ha pagado este miembro realmente
            double pagado = gastos.stream()
                                  .filter(g -> miembro.equals(g.getPagador()))
                                  .mapToDouble(Gasto::getImporte)
                                  .sum();
            saldos.put(miembro, pagado - cuotaPorPersona);
        }
        return saldos;
    }
}