package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;

import java.util.List;

public class CuentaService {

    private static CuentaService instancia;
    
    // Aquí guardamos la referencia al repositorio (el que maneja el archivo)
    private CuentaRepository repositorio;

    private CuentaService() {
        // Inicializamos el repositorio con la implementación JSON
        this.repositorio = new CuentaRepositoryJson();
    }

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    /**
     * Recupera todas las cuentas del archivo JSON.
     * Al ser una app monousuario, ignoramos el parámetro 'usuario' 
     * y devolvemos todo lo que hay guardado.
     */
    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return repositorio.findAll();
    }

    /**
     * Guarda una nueva cuenta en el archivo JSON.
     */
    public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        repositorio.save(cuenta);
    }
    
    /**
     * Elimina una cuenta del archivo JSON (lo usarás más adelante).
     */
    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
}