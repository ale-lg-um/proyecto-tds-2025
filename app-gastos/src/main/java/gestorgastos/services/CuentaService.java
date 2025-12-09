package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;

import java.util.List;

public class CuentaService {

    private static CuentaService instancia;
    
    // Ya no hay lista global de categorías aquí.
    
    private CuentaRepository repositorio;

    private CuentaService() {
        this.repositorio = new CuentaRepositoryJson();
    }

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return repositorio.findAll();
    }
    
    // Guardar (sirve para Crear y para Actualizar)
    public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        repositorio.save(cuenta);
    }
    
    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
}