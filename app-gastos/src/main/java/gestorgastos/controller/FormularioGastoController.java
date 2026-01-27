package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.ServicioAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.LocalDate;

public class FormularioGastoController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtConcepto;
    @FXML private TextField txtImporte;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private VBox panelPagador;
    @FXML private ComboBox<String> comboPagador;
    @FXML private Label lblError;
    @FXML private Spinner<Integer> spinHora;
    @FXML private Spinner<Integer> spinMinuto;

    private Gasto gastoResultado;
    private boolean esEdicion = false;
    private Cuenta cuentaAsociada;
    private boolean guardadoConfirmado = false;

    @FXML
    public void initialize() {
        dateFecha.setValue(LocalDate.now());
        spinHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        spinMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    }

    public void initAttributes(Cuenta cuenta, Gasto gastoAEditar) {
        this.cuentaAsociada = cuenta;
        comboCategoria.setItems(FXCollections.observableArrayList(cuenta.getCategorias()));
        if (!comboCategoria.getItems().isEmpty()) comboCategoria.getSelectionModel().selectFirst();

        if (cuenta instanceof CuentaCompartida) {
            panelPagador.setVisible(true);
            panelPagador.setManaged(true);
            comboPagador.setItems(FXCollections.observableArrayList(((CuentaCompartida) cuenta).getMiembros()));
        } else {
            panelPagador.setVisible(false);
            panelPagador.setManaged(false);
        }

        if (gastoAEditar != null) {
            this.esEdicion = true;
            this.gastoResultado = gastoAEditar;
            lblTitulo.setText("Editar Gasto");
            txtConcepto.setText(gastoAEditar.getConcepto());
            txtImporte.setText(String.valueOf(gastoAEditar.getImporte()));
            dateFecha.setValue(gastoAEditar.getFecha());
            spinHora.getValueFactory().setValue(gastoAEditar.getHora().getHour());
            spinMinuto.getValueFactory().setValue(gastoAEditar.getHora().getMinute());
            comboCategoria.setValue(gastoAEditar.getCategoria());
            if (cuenta instanceof CuentaCompartida) comboPagador.setValue(gastoAEditar.getPagador());
        } else {
            lblTitulo.setText("Nuevo Gasto");
            dateFecha.setValue(LocalDate.now());
            LocalTime ahora = LocalTime.now();
            spinHora.getValueFactory().setValue(ahora.getHour());
            spinMinuto.getValueFactory().setValue(ahora.getMinute());
            if (cuenta instanceof CuentaCompartida && !comboPagador.getItems().isEmpty()) {
                comboPagador.getSelectionModel().selectFirst();
            }
        }
    }
    
    public Gasto getGastoResultado() { return gastoResultado; }
    public boolean isGuardadoConfirmado() { return guardadoConfirmado; }

    @FXML
    private void guardar() {
        String concepto = txtConcepto.getText();
        if (concepto.isEmpty()) { lblError.setText("El concepto es obligatorio"); return; }

        double importe;
        try {
            importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
        } catch (NumberFormatException e) { lblError.setText("El importe debe ser un número válido"); return; }

        LocalDate fecha = dateFecha.getValue();
        if (fecha == null) { lblError.setText("La fecha es obligatoria"); return; }

        Categoria categoria = comboCategoria.getValue();
        if (categoria == null) { lblError.setText("Debes tener al menos una categoría"); return; }

        String pagador = "Yo";
        if (cuentaAsociada instanceof CuentaCompartida) {
            pagador = comboPagador.getValue();
            if (pagador == null) { lblError.setText("Debes seleccionar quién pagó"); return; }
        }

        if (esEdicion) {
            gastoResultado.setConcepto(concepto);
            gastoResultado.setImporte(importe);
            gastoResultado.setFecha(fecha);
            gastoResultado.setCategoria(categoria);
            gastoResultado.setPagador(pagador);
        } else {
            gastoResultado = new Gasto(concepto, importe, fecha, categoria, pagador);
        }
    
        gastoResultado.setHora(LocalTime.of(spinHora.getValue(), spinMinuto.getValue()));

        ServicioAlertas servicioAlertas = new ServicioAlertas();
        Alerta alertaSaltada = servicioAlertas.comprobarAlertas(cuentaAsociada, gastoResultado);
        
        if (alertaSaltada != null) {
            String tipo = alertaSaltada.getTipo(); 
            double limite = alertaSaltada.getLimite();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gasto Bloqueado");
            alert.setHeaderText("Límite superado");
            // Mensaje dinámico con el valor correcto
            alert.setContentText("Has superado el límite " + tipo + " de " + limite + "€ definido en tu alerta.\n\nSe ha generado una notificación aunque el gasto se guardará.");
            alert.showAndWait();
        }

        this.guardadoConfirmado = true;
        cerrarVentana();
    }

    @FXML
    private void cancelar() {
        gastoResultado = null;
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) txtConcepto.getScene().getWindow()).close();
    }
}