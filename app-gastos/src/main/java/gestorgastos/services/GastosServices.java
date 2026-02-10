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
}

