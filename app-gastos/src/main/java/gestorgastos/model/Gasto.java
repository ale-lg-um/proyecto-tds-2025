package gestorgastos.model;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime; // <--- Importar

public class Gasto {

	private String id;
	private String concepto; // Ej: "Cena", "Gasolina"
	private double importe; // Ej: 50.0
	private LocalDate fecha; // Ej: 2025-09-29
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime hora = LocalTime.now();
	

	
	private Categoria categoria; // El gasto pertenece a una categoría

	// IMPORTANTE: Necesario para cuentas compartidas
	// Guardamos el nombre de la persona que pagó.
	private String pagador;

	public Gasto(String concepto, double importe, LocalDate fecha, Categoria categoria, String pagador) {
		this.id = UUID.randomUUID().toString(); // ID único para poder borrarlo luego
		this.concepto = concepto;
		this.importe = importe;
		this.fecha = fecha;
		this.categoria = categoria;
		this.pagador = pagador;
	}

	// Constructor vacío para Jackson
	public Gasto() {
	}

	// Constructor simplificado para Cuentas Personales (sin pagador explícito)
	public Gasto(String concepto, double importe, LocalDate fecha, Categoria categoria) {
		this(concepto, importe, fecha, categoria, "Yo");
	}

	// Getters y Setters necesarios para editar
	public String getId() {
		return id;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public double getImporte() {
		return importe;
	}

	public void setImporte(double importe) {
		this.importe = importe;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public String getPagador() {
		return pagador;
	}

	public void setPagador(String pagador) {
		this.pagador = pagador;
	}

	@Override
	public String toString() {
		return fecha + ": " + concepto + " (" + importe + "€) - " + pagador;
	}
	
	// --- NUEVOS GETTER Y SETTER (Necesarios para el calendario) ---
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    // --------------------------------------------------------------
}