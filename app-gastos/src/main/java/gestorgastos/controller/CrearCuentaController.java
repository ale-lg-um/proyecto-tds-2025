package gestorgastos.controller;

import gestorgastos.model.*; // Importamos Cuenta, CuentaPersonal, etc.
import gestorgastos.services.CuentaService;
import gestorgastos.services.SesionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrearCuentaController {

	// --- ELEMENTOS DE LA UI ---
	@FXML
	private TextField nombreCuentaField;
	@FXML
	private Button btnCrear;

	// NUEVO: Selector de tipo de cuenta
	@FXML
	private ComboBox<String> comboTipo;

	// NUEVO: Contenedor para ocultar/mostrar la parte de añadir amigos
	@FXML
	private VBox panelMiembros;

	// NUEVO: Campos para añadir gente al grupo
	@FXML
	private TextField txtNuevoMiembro;
	@FXML
	private TextField txtPorcentaje; // Solo visible para "Especial"
	@FXML
	private Button btnAnadirMiembro;
	@FXML
	private ListView<String> listaMiembrosView;
	@FXML
	private Label lblError;

	// --- LÓGICA INTERNA ---
	private CuentaService cuentaService = CuentaService.getInstancia();
	private Runnable onCuentaCreada;

	// Listas temporales para guardar los datos mientras el usuario los mete
	private ObservableList<String> miembrosVisuales = FXCollections.observableArrayList();
	private Map<String, Double> mapaPorcentajes = new HashMap<>();

	public void setOnCuentaCreada(Runnable callback) {
		this.onCuentaCreada = callback;
	}

	@FXML
	public void initialize() {
		// 1. Configurar el ComboBox con las opciones del enunciado
		comboTipo.setItems(FXCollections.observableArrayList("Personal", "Compartida", "Especial"));
		comboTipo.getSelectionModel().select("Personal");

		// 2. Vincular la ListView con nuestros datos
		listaMiembrosView.setItems(miembrosVisuales);

		// 3. Estado inicial: Ocultar panel de miembros (porque empieza en Personal)
		panelMiembros.setVisible(false);
		txtPorcentaje.setManaged(false); // Ocultar campo %
		txtPorcentaje.setVisible(false);

		// 4. LISTENER: Si cambia el tipo, mostramos u ocultamos cosas
		comboTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			actualizarInterfaz(newVal);
		});

		// 5. Acción botón añadir miembro
		btnAnadirMiembro.setOnAction(e -> agregarMiembro());

		// 6. Acción botón Crear (Tu lógica original mejorada)
		btnCrear.setOnAction(e -> crearLaCuenta());
	}

	private void actualizarInterfaz(String tipo) {
		// Limpiamos datos viejos al cambiar de tipo
		miembrosVisuales.clear();
		mapaPorcentajes.clear();
		lblError.setText("");

		if ("Personal".equals(tipo)) {
			panelMiembros.setVisible(false);
		} else {
			// Si es Compartida o Especial, mostramos el panel para añadir gente
			panelMiembros.setVisible(true);

			if ("Especial".equals(tipo)) {
				// Si es Especial, necesitamos el campo de porcentaje
				txtPorcentaje.setManaged(true);
				txtPorcentaje.setVisible(true);
				txtPorcentaje.setPromptText("% (Ej: 30)");
			} else {
				// Si es Compartida normal (equitativa), ocultamos el % [cite: 29]
				txtPorcentaje.setManaged(false);
				txtPorcentaje.setVisible(false);
			}
		}
	}

	private void agregarMiembro() {
		String nombrePersona = txtNuevoMiembro.getText().trim();
		if (nombrePersona.isEmpty())
			return;

		String tipo = comboTipo.getValue();

		// Lógica para cuenta Especial (con porcentajes)
		if ("Especial".equals(tipo)) {
			try {
				String porcText = txtPorcentaje.getText().replace(",", ".");
				double porcentaje = Double.parseDouble(porcText);

				// Guardamos en el mapa y en la lista visual
				mapaPorcentajes.put(nombrePersona, porcentaje);
				miembrosVisuales.add(nombrePersona + " (" + porcentaje + "%)");

			} catch (NumberFormatException ex) {
				lblError.setText("Error: El porcentaje debe ser un número");
				return;
			}
		} else {
			// Compartida Normal
			miembrosVisuales.add(nombrePersona);
		}

		// Limpiar campos
		txtNuevoMiembro.clear();
		txtPorcentaje.clear();
		txtNuevoMiembro.requestFocus();
	}

	private void crearLaCuenta() {
		String nombre = nombreCuentaField.getText();
		if (nombre == null || nombre.isBlank()) {
			lblError.setText("⚠️ El nombre de la cuenta es obligatorio");
			return;
		}

		String tipo = comboTipo.getValue();
		Cuenta nuevaCuenta = null;

		// --- AQUI SE APLICA EL POLIMORFISMO DE TU JERARQUÍA ---
		try {
			if ("Personal".equals(tipo)) {
				nuevaCuenta = new CuentaPersonal(nombre);

			} else if ("Compartida".equals(tipo)) {
				if (miembrosVisuales.isEmpty()) {
					lblError.setText("Añade al menos una persona al grupo.");
					return;
				}
				// Convertimos ObservableList a ArrayList normal para el modelo
				nuevaCuenta = new CuentaCompartida(nombre, new ArrayList<>(miembrosVisuales));

			} else if ("Especial".equals(tipo)) {

				// 1. Sumamos todos los valores del mapa
				double sumaTotal = mapaPorcentajes.values().stream().mapToDouble(Double::doubleValue).sum();

				// 2. Validamos con un margen de error muy pequeño (0.01)
				// Si la diferencia entre la suma y 100 es mayor a 0.01, es un error.
				if (Math.abs(sumaTotal - 100.0) > 0.01) {
					lblError.setText("Error: Los porcentajes suman " + String.format("%.2f", sumaTotal)
							+ "%. Deben sumar exactamente 100%.");
					return; // IMPORTANTE: Detenemos la creación aquí
				}

				// Si pasa la validación, creamos la cuenta
				nuevaCuenta = new CuentaProporcional(nombre, new ArrayList<>(mapaPorcentajes.keySet()),
						mapaPorcentajes);
			}

// --- GUARDADO ---

			// 1. Obtenemos el usuario que está logueado actualmente
			Usuario usuarioActivo = SesionService.getInstancia().getUsuarioActivo();

			// 2. Verificamos que no sea null (por seguridad)
			if (usuarioActivo == null) {
				lblError.setText("Error: No hay usuario en sesión.");
				return;
			}

			// 3. Guardamos la cuenta asociándola a ese usuario
			// El servicio espera (Usuario, Cuenta)
			cuentaService.agregarCuenta(usuarioActivo, nuevaCuenta);

			if (onCuentaCreada != null) {
				onCuentaCreada.run();
			}

			Stage stage = (Stage) btnCrear.getScene().getWindow();
			stage.close();

		} catch (Exception ex) {
			lblError.setText("Error al crear: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	@FXML private Label lblProgresoPorcentaje; // Acuérdate de declararlo arriba

	// Método auxiliar para actualizar el texto
	private void actualizarProgreso() {
	    if ("Especial".equals(comboTipo.getValue())) {
	        double suma = mapaPorcentajes.values().stream().mapToDouble(d -> d).sum();
	        lblProgresoPorcentaje.setVisible(true);
	        lblProgresoPorcentaje.setText("Total asignado: " + suma + "% (Falta: " + (100.0 - suma) + "%)");
	        
	        // Cambio de color visual: Verde si está bien, Rojo si se pasa o falta
	        if (Math.abs(suma - 100.0) < 0.01) {
	             lblProgresoPorcentaje.setStyle("-fx-text-fill: green;");
	        } else {
	             lblProgresoPorcentaje.setStyle("-fx-text-fill: red;");
	        }
	    } else {
	        lblProgresoPorcentaje.setVisible(false);
	    }
	}

	// Y llamas a este método 'actualizarProgreso()' al final de 'agregarMiembro()' 
	// y también al cambiar el tipo de cuenta en 'actualizarInterfaz()'.
}