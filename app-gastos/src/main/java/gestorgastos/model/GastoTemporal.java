package gestorgastos.model;

import java.time.LocalDate;
import java.time.LocalTime;

// Clase para mover los gastos de los ficheros al controlador  antes de validarlos
public class GastoTemporal {
	 public String nombreCuenta; // Account
	 public String concepto; // Note
	 public double importe;
	 public String categoria; // Subcategory
	 public String pagador;
	 public LocalDate fecha;
	 public LocalTime hora;
	 
	 public GastoTemporal(String nombreCuenta, String concepto, double importe, String categoria, String pagador, LocalDate fecha, LocalTime hora) {
		 this.nombreCuenta = nombreCuenta;
		 this.concepto = concepto;
		 this.importe = importe;
		 this.categoria = categoria;
		 this.pagador = pagador;
		 this.fecha = fecha;
		 this.hora = hora;
	 }
}
