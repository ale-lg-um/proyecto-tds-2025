package gestorgastos.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("PERSONAL")
public class CuentaPersonal extends Cuenta {

	public CuentaPersonal() {
	    super(); // Llama al constructor vacío de Cuenta
	}
	
    public CuentaPersonal(String nombre) {
        super(nombre);
    }
    
    @Override
    public String getTipo() {
        return "PERSONAL";
    }
    // Aquí no necesitamos lista de amigos ni porcentajes.
}