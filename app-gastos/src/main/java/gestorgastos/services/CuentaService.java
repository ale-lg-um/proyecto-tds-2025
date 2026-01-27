package gestorgastos.services;

import gestorgastos.model.Alerta;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.model.Usuario;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;

import java.util.List;

public class CuentaService {

    private static CuentaService instancia;
    
    // Inicialización del repositorio
    private CuentaRepository repositorio;

    // Constructor
    private CuentaService() {
        this.repositorio = new CuentaRepositoryJson();
    }

    // Obtener una instancia de CuentaService
    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    // Obtener todas las cuentas de un usuario
    public List<Cuenta> getCuentasUsuarioActual() {
    	Usuario activo = SesionService.getInstancia().getUsuarioActivo();
    	return repositorio.findAll();
    }
    
    // Corrección primer PR
    /*public List<Cuenta> getCuentasDe(Usuario usuario) {
        return repositorio.findAll();
    }*/
    
    // Guardar una cuenta nueva
    public void  agregarCuenta(Cuenta cuenta) {
    	Usuario activo = SesionService.getInstancia().getUsuarioActivo();
    	repositorio.save(cuenta);
    }
    
    /*public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        repositorio.save(cuenta);
    }*/
    
    // Eliminar una cuenta
    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
    
    // Corrección séptimo PR
    public Alerta agregarGasto(Cuenta cuenta, Gasto nuevo) {
    	cuenta.agregarGasto(nuevo);
    	
    	ServicioAlertas servicio = new ServicioAlertas();
    	Alerta saltada = servicio.comprobarAlertas(cuenta, nuevo);
    	
    	if(saltada != null) {
    		String mensaje = "Has superado el límite de " + saltada.getLimite() + "€ establecido en la alerta";
    		cuenta.anadirNotificacion(mensaje);
    	}
    	agregarCuenta(cuenta);
    	return saltada;
    }
}