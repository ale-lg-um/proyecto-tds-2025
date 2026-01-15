package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.ServicioAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalTime; // <--- Importar

import java.time.LocalDate;

public class FormularioGastoController {

	@FXML
	private Label lblTitulo;
	@FXML
	private TextField txtConcepto;
	@FXML
	private TextField txtImporte;
	@FXML
	private DatePicker dateFecha;
	@FXML
	private ComboBox<Categoria> comboCategoria;

	@FXML
	private VBox panelPagador;
	@FXML
	private ComboBox<String> comboPagador;
	@FXML
	private Label lblError;

	@FXML
	private Spinner<Integer> spinHora;
	@FXML
	private Spinner<Integer> spinMinuto;

	private Gasto gastoResultado;
	private boolean esEdicion = false;
	private Cuenta cuentaAsociada; // Necesario para saber los miembros y las categorías
	private boolean guardadoConfirmado = false;

	@FXML
	public void initialize() {
		// En initialize SOLO configuramos cosas que no dependen de la cuenta
		dateFecha.setValue(LocalDate.now());
		spinHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
		spinMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
	}

	// Método para configurar la ventana según si es CREAR o EDITAR
	public void initAttributes(Cuenta cuenta, Gasto gastoAEditar) {
		this.cuentaAsociada = cuenta;

		// 1. CARGAR LAS CATEGORÍAS
		comboCategoria.setItems(FXCollections.observableArrayList(cuenta.getCategorias()));

		if (!comboCategoria.getItems().isEmpty()) {
			comboCategoria.getSelectionModel().selectFirst();
		}

		// Visibilidad del pagador del gasto (no es necesario si la cuenta es personal)
		if (cuenta instanceof CuentaCompartida) {
			panelPagador.setVisible(true);
			panelPagador.setManaged(true);
			comboPagador.setItems(FXCollections.observableArrayList(((CuentaCompartida) cuenta).getMiembros()));
		} else {
			panelPagador.setVisible(false);
			panelPagador.setManaged(false);
		}

		// Rellenamos los datos del gasto
		if (gastoAEditar != null) {
			// Si el gasto ya existe, entonces lo estamos editando
			this.esEdicion = true;
			this.gastoResultado = gastoAEditar;
			lblTitulo.setText("Editar Gasto");

			txtConcepto.setText(gastoAEditar.getConcepto());
			txtImporte.setText(String.valueOf(gastoAEditar.getImporte()));
			dateFecha.setValue(gastoAEditar.getFecha());

			spinHora.getValueFactory().setValue(gastoAEditar.getHora().getHour());
			spinMinuto.getValueFactory().setValue(gastoAEditar.getHora().getMinute());

			comboCategoria.setValue(gastoAEditar.getCategoria());

			if (cuenta instanceof CuentaCompartida) {
				comboPagador.setValue(gastoAEditar.getPagador());
			}
		} else {
			// Si el gasto no existía, entonces estamos creando un nuevo gasto
			lblTitulo.setText("Nuevo Gasto");

			dateFecha.setValue(java.time.LocalDate.now()); // Ponemos fecha de hoy

			java.time.LocalTime ahora = java.time.LocalTime.now(); // Hora de ahora mismo
			spinHora.getValueFactory().setValue(ahora.getHour());
			spinMinuto.getValueFactory().setValue(ahora.getMinute());

			if (cuenta instanceof CuentaCompartida && !comboPagador.getItems().isEmpty()) {
				comboPagador.getSelectionModel().selectFirst();
			}
		}
	}

	public Gasto getGastoResultado() {
		return gastoResultado;
	}

	@FXML
	private void guardar() {
		// Guardamos el gasto comprobando los valores de los campos
		String concepto = txtConcepto.getText();
		if (concepto.isEmpty()) {
			lblError.setText("El concepto es obligatorio");
			return;
		}

		double importe;
		try {
			importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
		} catch (NumberFormatException e) {
			lblError.setText("El importe debe ser un número válido");
			return;
		}

		LocalDate fecha = dateFecha.getValue();
		if (fecha == null) {
			lblError.setText("La fecha es obligatoria");
			return;
		}

		Categoria categoria = comboCategoria.getValue();
		if (categoria == null) {
			lblError.setText("Debes tener al menos una categoría");
			return;
		}

		String pagador = "Yo";
		if (cuentaAsociada instanceof CuentaCompartida) { // Esto si estamos en una cuenta compartida o especial
			pagador = comboPagador.getValue();
			if (pagador == null) {
				lblError.setText("Debes seleccionar quién pagó");
				return;
			}
		}

		
		// Si estamos editango un gasto, actualizamos sus valores
		if (esEdicion) {
			gastoResultado.setConcepto(concepto);
			gastoResultado.setImporte(importe);
			gastoResultado.setFecha(fecha);
			gastoResultado.setCategoria(categoria);
			gastoResultado.setPagador(pagador);
		} else {
			gastoResultado = new Gasto(concepto, importe, fecha, categoria, pagador);
		}
	
		int h = spinHora.getValue();
		int m = spinMinuto.getValue();
		gastoResultado.setHora(java.time.LocalTime.of(h, m));

		// Comprobamos si debe saltar alguna alerta
		gestorgastos.services.ServicioAlertas servicioAlertas = new gestorgastos.services.ServicioAlertas();

		// Si hay alerta, se guarda la notificación y se guarda el texto de la alerta.
		String mensajeError = servicioAlertas.comprobarAlertas(cuentaAsociada, gastoResultado);

		// Si mensajeError NO es null, significa que nos hemos pasado del límite
		if (mensajeError != null) {
			// Mostramos una ventana con el mensaje
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Gasto Bloqueado");
			alert.setHeaderText("Límite superado");
			alert.setContentText(mensajeError + "\n\nSe ha generado una notificación aunque el gasto se guardará.");
			alert.showAndWait();
		}

	
		this.guardadoConfirmado = true; // 

		cerrarVentana();
	}

	@FXML
	private void cancelar() {
		gastoResultado = null; // Indicamos que no se hizo nada
		cerrarVentana();
	}

	private void cerrarVentana() {
		Stage stage = (Stage) txtConcepto.getScene().getWindow();
		stage.close();
	}

	public boolean isGuardadoConfirmado() {
		return guardadoConfirmado;
	}
}