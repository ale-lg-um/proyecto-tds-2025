package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;

import java.util.*;

public class CuentaService {
    
    // Patrón Singleton (Obligatorio según enunciado)
    private static CuentaService instancia;
    
    // Simulación de base de datos en memoria (más adelante esto se conecta al Repository)
    private Map<Usuario, List<Cuenta>> cuentasPorUsuario = new HashMap<>();

    private CuentaService() {}

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return cuentasPorUsuario.getOrDefault(usuario, new ArrayList<>());
    }

    /**
     * Guarda una cuenta nueva asociada al usuario.
     * Gracias al polimorfismo, 'cuenta' puede ser Personal, Compartida o Proporcional.
     */
    public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        cuentasPorUsuario.computeIfAbsent(usuario, k -> new ArrayList<>()).add(cuenta);
    }
    
    // He eliminado los métodos antiguos 'crearCuentaCompartida' etc.
    // porque ahora la lógica de creación compleja está en el Controlador,
    // y aquí solo recibimos el objeto ya creado para guardarlo.
}