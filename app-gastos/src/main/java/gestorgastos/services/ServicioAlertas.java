package gestorgastos.services;

import gestorgastos.model.*;
import gestorgastos.strategies.FactoriaAlertas;
import gestorgastos.strategies.InterfaceAlerta;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

public class ServicioAlertas {
    private static ServicioAlertas instancia;
    private final CuentaService cuentaService = CuentaService.getInstancia();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ServicioAlertas() {}

    public static ServicioAlertas getInstancia() {
        if (instancia == null) instancia = new ServicioAlertas();
        return instancia;
    }


    public void guardarNuevaAlerta(Cuenta cuenta, String tipo, String limiteTxt, Categoria cat) throws NumberFormatException {
        if (cuenta == null) return;
        
        
        double limite = Double.parseDouble(limiteTxt.replace(",", "."));

        Alerta nuevaAlerta = Alerta.crearAlerta(tipo,limite,cat);
        
        
        cuenta.agregarAlerta(nuevaAlerta);
        cuentaService.agregarCuenta(cuenta);
    }

    public void eliminarAlerta(Cuenta cuenta, Alerta alerta) {
        if (cuenta != null && alerta != null) {
            cuenta.getAlertas().remove(alerta);
            cuentaService.agregarCuenta(cuenta);
        }
    }

    public void limpiarHistorial(Cuenta cuenta) {
        if (cuenta != null && cuenta.getNotificaciones() != null) {
            cuenta.getNotificaciones().clear();
            cuentaService.agregarCuenta(cuenta);
        }
    }

    

    public String obtenerTextoDescriptivo(Alerta item) {
        if (item == null) return null;
        
        LocalDate hoy = LocalDate.now();
        LocalDate inicio, fin;
        
        if ("SEMANAL".equalsIgnoreCase(item.getTipo())) {
            inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            fin = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        } else {
            inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
            fin = hoy.with(TemporalAdjusters.lastDayOfMonth());
        }
        
        String rango = "(" + inicio.format(fmt) + " al " + fin.format(fmt) + ")";
        String nombreCat = (item.getCategoria() != null) ? item.getCategoria().getNombre() : "General";
        
        return String.format("%s %s -> %.2f€ (%s)", 
                item.getTipo(), rango, item.getLimite(), nombreCat);
    }
   
    

public Alerta comprobarAlertas(Cuenta cuenta, Gasto nuevoGasto) {
    // 1. Validación inicial 
    if(Cuenta.noHayAlertas(cuenta)) return null;
    

    // 2. Prioridad: Categorías específicas
    for (Alerta alerta : cuenta.getAlertas()) {
        
        if (alerta.esMismaCategoria(nuevoGasto)) {
            if (verificarLimite(alerta, cuenta, nuevoGasto)) return alerta;
        }
    }

    // 3. Prioridad: Alertas generales
    for (Alerta alerta : cuenta.getAlertas()) {
        
        if (alerta.esGeneral()) {
            if (verificarLimite(alerta, cuenta, nuevoGasto)) return alerta;
        }
    }

    return null;
}


private boolean verificarLimite(Alerta alerta, Cuenta cuenta, Gasto gasto) {
    // El servicio es el que conoce la Factoría y las Estrategias
    InterfaceAlerta estrategia = FactoriaAlertas.getEstrategia(alerta.getTipo());
    return estrategia != null && estrategia.verificarLimite(alerta, cuenta, gasto);
}
}