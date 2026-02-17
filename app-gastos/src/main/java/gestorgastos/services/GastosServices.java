package gestorgastos.services;

import java.time.LocalDate;
import java.time.LocalTime;

import gestorgastos.model.Alerta;
import gestorgastos.model.Categoria;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;

public class GastosServices {
	private static GastosServices instancia;
	// private CuentaService cuentaService = CuentaService.getInstancia();
	
	public static GastosServices getInstancia() {
		if(instancia == null) {
			instancia = new GastosServices();
		}
		return instancia;
	}
	
	
	public Gasto crearEditarGasto(Spinner<Integer>spinMinuto, Spinner<Integer>spinHora, Gasto gastoResultado, String concepto, double importe, LocalDate fecha, Categoria categoria, String pagador, boolean esEdicion) {
		if(esEdicion) {
			gastoResultado.setConcepto(concepto);
			gastoResultado.setImporte(importe);
			gastoResultado.setFecha(fecha);
			gastoResultado.setCategoria(categoria);
			gastoResultado.setPagador(pagador);
		} else {
			gastoResultado = Gasto.crearGasto(concepto, importe, fecha, categoria, pagador);
		}
		
		gastoResultado.setHora(LocalTime.of(spinHora.getValue(), spinMinuto.getValue()));
		
		return gastoResultado;
	}
	
	public void establecerCat(Cuenta cuenta, Categoria categoria, Categoria generalCat) {

		CuentaService cuentaService = CuentaService.getInstancia();
		CategoriasService categoriasService = CategoriasService.getInstancia();

		//for (Gasto g : cuentaService.obtenerGastos(cuenta)) {
			/*
            if (g.getCategoria().equals(categoria)) {
                g.setCategoria(generalCat);
            }
			*/
			// Recorremos los gastos
    	for (Gasto g : cuentaService.obtenerGastos(cuenta)) {
        // ERROR ANTERIOR: g.getCategoria().equals(categoria) 
        // SOLUCIÓN: Comparar por el nombre de la categoría
    		if (g.getCategoria().getNombre().equalsIgnoreCase(categoriasService.getNombre(categoria))) {
    			g.setCategoria(generalCat);
    		}
    	}
	}
	
	public double obtenerImporte(Gasto gasto) {
		return gasto.getImporte();
	}
	
	public LocalDate obtenerFecha(Gasto gasto) {
		return gasto.getFecha();
	}
	
	public LocalTime obtenerHora(Gasto gasto) {
		return gasto.getHora();
	}
	
	public String obtenerConcepto(Gasto gasto) {
		return gasto.getConcepto();
	}
	
	public Categoria obtenerCategoria(Gasto gasto) {
		return gasto.getCategoria();
	}

	public Gasto crearGasto(String concepto, double importe, LocalDate fecha, Categoria cat, String pagador, LocalTime hora){
		Gasto gasto = Gasto.crearGasto(concepto, importe, fecha, cat, pagador);
		gasto.setHora(hora);
		return gasto;
	}
}

