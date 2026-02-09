package gestorgastos.services;

import gestorgastos.model.*;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;
import java.util.*;

public class CuentaService {

    private static CuentaService instancia;
    private CuentaRepository repositorio;

    private CuentaService() {
        this.repositorio = new CuentaRepositoryJson();
    }

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    

    /*
     * El controlador solo pasa info y el servicio fabrica la cuenta.
     */
    public void procesarNuevaCuenta(String nombre, String tipo, List<String> miembros, Map<String, Double> porcentajes) throws Exception {
        
        // 1. Validaciones básicas 
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("El nombre de la cuenta es obligatorio");
        }

        Cuenta nuevaCuenta;

        
        switch (tipo) {
            case "Personal":
                nuevaCuenta = new CuentaPersonal(nombre);
                break;

            case "Compartida":
                if (miembros == null || miembros.isEmpty()) {
                    throw new Exception("Añade al menos una persona a la cuenta compartida.");
                }
                nuevaCuenta = new CuentaCompartida(nombre, new ArrayList<>(miembros));
                break;

            case "Especial":
                
                validarPorcentajes(porcentajes);
                nuevaCuenta = new CuentaProporcional(nombre, new ArrayList<>(porcentajes.keySet()), new HashMap<>(porcentajes));
                break;

            default:
                throw new Exception("Tipo de cuenta desconocido: " + tipo);
        }

        // Guardar en el repositorio
        repositorio.save(nuevaCuenta);
    }

    /**
     * El servicio es el único que conoce 
     * la regla de negocio del 100% de los porcentajes.
     */
    private void validarPorcentajes(Map<String, Double> porcentajes) throws Exception {
        if (porcentajes == null || porcentajes.isEmpty()) {
            throw new Exception("Debe añadir miembros con sus porcentajes.");
        }
        
        double sumaTotal = porcentajes.values().stream()
                                      .mapToDouble(Double::doubleValue)
                                      .sum();

        // Margen de error para decimales
        if (Math.abs(sumaTotal - 100.0) > 0.01) {
            throw new Exception(String.format("Los porcentajes deben sumar 100%% (Actual: %.2f%%)", sumaTotal));
        }
    }

    

    /**
     * Añade un gasto y comprobación de alertas.
     */
    public Alerta agregarGasto(Cuenta cuenta, Gasto nuevo) {
        // 1. Añadir el gasto al objeto 
        cuenta.agregarGasto(nuevo);
        
        // 2. Usar el servicio de alertas 
        ServicioAlertas servicio = ServicioAlertas.getInstancia();
        Alerta saltada = servicio.comprobarAlertas(cuenta, nuevo);
        
        // 3. Si hay alerta, generar notificación 
        if (saltada != null) {
            String mensaje = "Límite superado: " + saltada.getLimite() + "€ en " + 
                             (saltada.getCategoria() != null ? saltada.getCategoria().getNombre() : "General");
            cuenta.anadirNotificacion(mensaje);
        }

        // 4. Persistencia 
        repositorio.save(cuenta);
        return saltada;
    }

   

    public List<Cuenta> getCuentasUsuarioActual() {
        
        return repositorio.findAll();
    }

    public void agregarCuenta(Cuenta cuenta) {
        repositorio.save(cuenta);
    }

    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
}