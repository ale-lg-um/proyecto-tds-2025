package gestorgastos.services;

import gestorgastos.model.Cuenta;
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
    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return repositorio.findAll();
    }
    
    // Guardar una cuenta nueva
    public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        repositorio.save(cuenta);
    }
    
    // Eliminar una cuenta
    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
}