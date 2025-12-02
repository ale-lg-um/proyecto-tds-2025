package gestorgastos.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("COMPARTIDA")
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

    // Devolvemos lista inmodificable para cumplir requisito de no editar miembros
    public List<String> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }
    
    @Override
    public String getTipo() {
        return "COMPARTIDA";
    }
    
    public Map<String, Double> calcularSaldos() {
        // 1. Calcular el total gastado en la cuenta usando Streams
        double totalGastado = gastos.stream()
                                    .mapToDouble(Gasto::getImporte)
                                    .sum();

        // 2. Calcular cuánto le toca pagar a cada uno (Equitativo)
        int numMiembros = miembros.size();
        if (numMiembros == 0) return new HashMap<>();
        double cuotaPorPersona = totalGastado / numMiembros;

        // 3. Calcular el saldo de cada miembro
        Map<String, Double> saldos = new HashMap<>();
        
        for (String miembro : miembros) {
            // Cuánto ha pagado este miembro realmente
            double pagado = gastos.stream()
                                  .filter(g -> miembro.equals(g.getPagador()))
                                  .mapToDouble(Gasto::getImporte)
                                  .sum();
            
            // Saldo = Lo que puse - Lo que debía poner
            // Positivo = Me deben dinero / Negativo = Debo dinero
            saldos.put(miembro, pagado - cuotaPorPersona);
        }
        return saldos;
    }
}