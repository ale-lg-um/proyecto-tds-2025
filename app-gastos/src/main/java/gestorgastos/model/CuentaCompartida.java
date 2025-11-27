package gestorgastos.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
}