package gestorgastos.services;


import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;

import java.util.*;

public class CuentaService {
    private static CuentaService instancia;
    private Map<Usuario, List<Cuenta>> cuentasPorUsuario = new HashMap<>();

    private CuentaService() {}

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return cuentasPorUsuario.getOrDefault(usuario, new ArrayList<>());
    }

    public void crearCuenta(Usuario usuario, String nombreCuenta) {
        cuentasPorUsuario.computeIfAbsent(usuario, k -> new ArrayList<>()).add(new Cuenta(nombreCuenta));
    }
}
