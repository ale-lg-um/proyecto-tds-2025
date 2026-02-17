package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AlertaController {
    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private TextField txtLimite;
    @FXML private ListView<Alerta> listaAlertasConfiguradas;
    @FXML private ListView<Notificacion> listaHistorial;


	private Cuenta cuentaActual;
    private final ServicioAlertas servicioAlertas = ServicioAlertas.getInstancia();
    private final SesionService sesionService = SesionService.getInstancia();

    @FXML
    public void initialize() {
        this.cuentaActual = SesionService.getInstancia().getCuentaActiva();
        comboTipo.setItems(FXCollections.observableArrayList("SEMANAL", "MENSUAL"));
        comboTipo.getSelectionModel().selectFirst();
        listaAlertasConfiguradas.setCellFactory(lv -> new ListCell<Alerta>() {
            @Override
            protected void updateItem(Alerta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(servicioAlertas.obtenerTextoDescriptivo(item));
                }
            }
        });
        
        cargarDatosPantalla();
    }

    private void cargarDatosPantalla() {
        Cuenta cuentaActual = sesionService.getCuentaActiva();
        if (cuentaActual != null) {
            comboCategoria.setItems(FXCollections.observableArrayList(cuentaActual.getCategorias()));
            listaAlertasConfiguradas.setItems(FXCollections.observableArrayList(cuentaActual.getAlertas()));
            listaHistorial.setItems(FXCollections.observableArrayList(cuentaActual.getNotificaciones()));
        }
    }

    @FXML
    private void guardarAlertas() {
        try {
            servicioAlertas.guardarNuevaAlerta(
                this.cuentaActual,
                comboTipo.getValue(),
                txtLimite.getText(),
                comboCategoria.getValue()
            );
            
            cargarDatosPantalla();
            txtLimite.clear();
            comboCategoria.getSelectionModel().clearSelection();
            
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de formato");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, introduce un límite numérico válido.");
            alert.showAndWait();
        }
    }

    @FXML
    private void borrarAlertas() {
        Alerta seleccionada = listaAlertasConfiguradas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            servicioAlertas.eliminarAlerta(cuentaActual, seleccionada);
            cargarDatosPantalla();
        }
    }

    @FXML
    private void limpiarHistorial() {
        servicioAlertas.limpiarHistorial(cuentaActual);
        cargarDatosPantalla();
    }
}