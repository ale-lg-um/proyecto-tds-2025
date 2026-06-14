package gestorgastos.services;

import gestorgastos.dto.GastoTemporal;
import gestorgastos.model.*;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class CuentaService {

	private static CuentaService instancia;
	private CuentaRepository repositorio;
	/*
	 * private CategoriasService categoriasService =
	 * CategoriasService.getInstancia(); private GastosServices gastosService =
	 * GastosServices.getInstancia();
	 */

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

			//validarPorcentajes(porcentajes);
			nuevaCuenta = new CuentaProporcional(nombre, new ArrayList<>(porcentajes.keySet()),
					new HashMap<>(porcentajes));
			break;

		default:
			throw new Exception("Tipo de cuenta desconocido: " + tipo);
		}

		repositorio.save(nuevaCuenta);
	}

	/*private void validarPorcentajes(Map<String, Double> porcentajes) throws Exception {
		if (porcentajes == null || porcentajes.isEmpty()) {
			throw new Exception("Debe añadir miembros con sus porcentajes.");
		}

		double sumaTotal = porcentajes.values().stream().mapToDouble(Double::doubleValue).sum();

		// Margen de error para decimales
		if (Math.abs(sumaTotal - 100.0) > 0.01) {
			throw new Exception(String.format("Los porcentajes deben sumar 100%% (Actual: %.2f%%)", sumaTotal));
		}
	}*/

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
		SesionService.getInstancia().setCuentaActiva(cuenta);
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
	
	public void eliminarGasto(Cuenta cuenta, Gasto gasto) {
		cuenta.eliminarGasto(gasto);
		repositorio.save(cuenta);
		SesionService.getInstancia().setCuentaActiva(cuenta);
	}

	public void actualizarGastoEditado(Cuenta cuenta, Gasto antiguo, Gasto nuevo) {
		int index = cuenta.getGastos().indexOf(antiguo);
		if (index != -1) {
			cuenta.getGastos().set(index, nuevo);
			repositorio.save(cuenta);
			SesionService.getInstancia().setCuentaActiva(cuenta);
		}
	}

	/////////////////////////////////////////// SERVICIO PANTALLA CATEGORIAS
	
	/*
	public List<Categoria> obtenerCategorias(Cuenta cuenta) {
		return cuenta.getCategorias();
	}
	*/
	
	public void anadirCat(Cuenta cuenta, Categoria categoria) {
		cuenta.getCategorias().add(categoria);
		repositorio.save(cuenta);
		SesionService.getInstancia().setCuentaActiva(cuenta);
	}
	
	/*
	public void cambiarCategoriaGastos(Cuenta cuenta, Categoria categoria) {

		CategoriasService categoriasService = CategoriasService.getInstancia();
		GastosServices gastosService = GastosServices.getInstancia();

		Categoria generalCat = cuenta.getCategorias().stream()
				.filter(c -> "General".equals(c.getNombre())).findFirst().orElse(null);
		
		//if (generalCat != null) {
		//	gastosService.establecerCat(cuenta, categoria, generalCat);
		//}

		//cuenta.getCategorias().remove(categoria);
		

		// 2. Si existe General, movemos los gastos de la categoría vieja a la nueva
		if (generalCat != null && !categoria.getNombre().equalsIgnoreCase("General")) {
			gastosService.establecerCat(cuenta, categoria, generalCat);
		}

		// 3. AHORA SÍ, eliminamos la categoría de la lista de la cuenta
		// Usamos removeIf para asegurar que la borramos por nombre también
		cuenta.getCategorias().removeIf(c -> c.getNombre().equalsIgnoreCase(categoria.getNombre()));		
	}
	*/
	
	// Sustiuimos el metodo para cambiar la categoria de los gastos pidiendoselo a la cuenta, no aqui directamente
	public void cambiarCategoriaGastos(Cuenta cuenta, Categoria categoria) {
		// El servicio delega toda la lógica de listas al EXPERTO (La propia cuenta)
		cuenta.eliminarCategoriaYReasignarGastos(categoria);
		repositorio.save(cuenta);
		SesionService.getInstancia().setCuentaActiva(cuenta); 	
	}
	
	
	/*
	public List<Gasto> obtenerGastos(Cuenta cuenta) {
		return cuenta.getGastos();
	}

	public String obtenerNombre(Cuenta cuenta) {
		return cuenta.getNombre();
	}
	*/
	
	/////////////////////////////////////// PANTALLA  TERMINAL

	/*public Categoria procesarCatTerminal(String nombreCat, Cuenta cuenta) {
		CategoriasService categoriasService = CategoriasService.getInstancia();
		Categoria cat = cuenta.getCategorias().stream()
				.filter(c -> c.getNombre().equalsIgnoreCase(nombreCat))
				.findFirst()
				.orElse(cuenta.getCategorias().get(0));
		return cat;
	}
	
	public Gasto quitarGastoTerminal(int id, Cuenta cuenta) {
		return cuenta.getGastos().remove(id);
	}*/
	
	///////////////////////////////////////// PANTALLA VISUALIZACION GRAFICOS
	
	public List<Gasto> filtrarGastos(List<Gasto> todos, LocalDate desde, LocalDate hasta, List<Month> meses, List<Categoria> categorias) {
	    GastosServices gastosServices = GastosServices.getInstancia();
	    CategoriasService categoriasService = CategoriasService.getInstancia();

	    return todos.stream()
	        // 1. Filtro por rango de fechas
	        .filter(g -> {
	            LocalDate fechaGasto = g.getFecha();
	            if (desde != null && fechaGasto.isBefore(desde)) return false;
	            if (hasta != null && fechaGasto.isAfter(hasta)) return false;
	            return true;
	        })
	        // 2. Filtro por meses
	        .filter(g -> {
	            if (meses == null || meses.isEmpty()) return true;
	            return meses.contains(g.getFecha().getMonth());
	        })
	        // 3. Filtro por categorías
	        .filter(g -> {
	            if (categorias == null || categorias.isEmpty()) return true;
	            String nombreCatGasto = g.getCategoria().getNombre();
	            return categorias.stream()
	                .anyMatch(c -> c.getNombre().equals(nombreCatGasto));
	        })
	        .collect(Collectors.toList());
	}
	
	/**
	 * Agrupa los gastos por nombre de categoría y suma sus importes
	 */
	public Map<String, Double> agruparGastosPorCategoria(List<Gasto> gastos) {
	    return gastos.stream()
	            .collect(Collectors.groupingBy(
	                    g -> g.getCategoria().getNombre(),
	                    Collectors.summingDouble(Gasto::getImporte)
	            ));
	}

	/**
	 * Crea un mapa de nombres de categorías y sus colores hexadecimales.
	 */

	public Map<String, String> obtenerMapaColoresCategorias(Cuenta cuenta) {
	    return cuenta.getCategorias().stream()
	        .collect(Collectors.toMap(
	            Categoria::getNombre,
	            Categoria::getColorHex,
	            (a, b) -> a
	        ));
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