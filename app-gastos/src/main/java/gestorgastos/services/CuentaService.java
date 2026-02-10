package gestorgastos.services;

import gestorgastos.dto.GastoTemporal;
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
		if (instancia == null)
			instancia = new CuentaService();
		return instancia;
	}

	//////////////////////////////////////////////////// SERVICIOS PANTALLA CREAR
	//////////////////////////////////////////////////// CUENTA
	//////////////////////////////////////////////////// /////////////////////////////////
	public void procesarNuevaCuenta(String nombre, String tipo, List<String> miembros, Map<String, Double> porcentajes)
			throws Exception {

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
			nuevaCuenta = new CuentaProporcional(nombre, new ArrayList<>(porcentajes.keySet()),
					new HashMap<>(porcentajes));
			break;

		default:
			throw new Exception("Tipo de cuenta desconocido: " + tipo);
		}

		repositorio.save(nuevaCuenta);
	}

	private void validarPorcentajes(Map<String, Double> porcentajes) throws Exception {
		if (porcentajes == null || porcentajes.isEmpty()) {
			throw new Exception("Debe añadir miembros con sus porcentajes.");
		}

		double sumaTotal = porcentajes.values().stream().mapToDouble(Double::doubleValue).sum();

		// Margen de error para decimales
		if (Math.abs(sumaTotal - 100.0) > 0.01) {
			throw new Exception(String.format("Los porcentajes deben sumar 100%% (Actual: %.2f%%)", sumaTotal));
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////// SERVICIO PANTALLA PRINCIPAL CUENTA
	//////////////////////////////////////////// //////////////////////////////////////

	public static Map<String, Double> calcularSaldos(CuentaCompartida cuenta) {
		return cuenta.calcularSaldos();
	}

	public Alerta agregarGasto(Cuenta cuenta, Gasto nuevo) {
		cuenta.agregarGasto(nuevo);

		ServicioAlertas servicio = ServicioAlertas.getInstancia();
		Alerta saltada = servicio.comprobarAlertas(cuenta, nuevo);

		if (saltada != null) {
			String mensaje = "Límite superado: " + saltada.getLimite() + "€ en "
					+ (saltada.getCategoria() != null ? saltada.getCategoria().getNombre() : "General");
			cuenta.anadirNotificacion(mensaje);
		}

		repositorio.save(cuenta);
		return saltada;
	}
	
	public int[] importarGastos(List<GastoTemporal> temporales) {
		int[] resultados = new int[3];

		List<Cuenta> cuentasUsuario = this.getCuentasUsuarioActual();

		for (GastoTemporal t : temporales) {
			Optional<Cuenta> cMatch = cuentasUsuario.stream()
					.filter(c -> c.getNombre().equalsIgnoreCase(t.nombreCuenta)).findFirst();

			if (cMatch.isEmpty()) {
				resultados[1]++;
				continue;
			}

			Cuenta destino = cMatch.get();
			boolean valido = true;
 
			if (destino instanceof CuentaCompartida) {
				List<String> miembros = ((CuentaCompartida) destino).getMiembros();
				if (t.pagador != null && !t.pagador.equalsIgnoreCase("Yo")
						&& miembros.stream().noneMatch(m -> m.equalsIgnoreCase(t.pagador))) {
					valido = false;
				}
			}

			if (valido) {
				Categoria catReal = destino.getCategorias().stream()
						.filter(c -> c.getNombre().equalsIgnoreCase(t.categoria)).findFirst()
						.orElse(destino.getCategorias().get(0));

				Gasto nuevo = Gasto.crearGasto(t.concepto, t.importe, t.fecha, catReal, t.pagador);
				if (t.hora != null)
					nuevo.setHora(t.hora);

				Alerta alertaSaltada = this.agregarGasto(destino, nuevo);

				if (alertaSaltada != null) {
					resultados[2]++;
				}
				resultados[0]++;
			} else {
				resultados[1]++;
			}
		}

		return resultados;
	}

	//////////////////////////////////////////////////////////////////////////////////

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