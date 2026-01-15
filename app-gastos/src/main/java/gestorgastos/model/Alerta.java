package gestorgastos.model;

import gestorgastos.strategies.EstrategiaSemanal;
import gestorgastos.strategies.InterfaceAlerta;

public class Alerta {
	private String tipo;
	private double limite;
	private Categoria categoria;

	public Alerta(){}

	public Alerta(String tipo, double limite, Categoria categoria){
		this.tipo = tipo;
		this.limite = limite;
		this.categoria = categoria;
	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public double getLimite() {
		return this.limite;
	}
	
	public void setLimite(int limite) {
		this.limite = limite;
	}
	
	public Categoria getCategoria() {
		return this.categoria;
	}
	
	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}
	
	public String comprobarAlertas(Alerta alerta) {
		if("SEMANAL".equals(alerta.getTipo())) {
			return "SEMANAL";
		}
		return "MENSUAL";
	}

	@Override
	public String toString(){
		String categ = (categoria == null)? "Total" : categoria.getNombre();
		return tipo + " > " + limite + "€ (" + categ + ")";
	}
}
