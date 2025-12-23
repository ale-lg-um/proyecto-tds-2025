package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;


public class AlertaController {
    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private TextField txtLimite;
    @FXML private ListView<Alerta> listaAlertasConfiguradas;

    @FXML private ListView<Notificacion> listaHistorial;

    private Cuenta cuentaActual;
    private CuentaService cuentaService = CuentaService.getInstancia();

    public void setCuenta(Cuenta cuenta){
        this.cuentaActual = cuenta;

        if( cuenta.getAlertas() == null) cuenta.setAlertas(new ArrayList<>());
        if(cuenta.getNotificaciones() == null) cuenta.setNotificaciones(new ArrayList<>());

        cargarDatos();

    }

    @FXML
    public void initialize() {
    	comboTipo.setItems(FXCollections.observableArrayList("SEMANAL","MENSUAL"));
        comboTipo.getSelectionModel().selectFirst();
    }
    
    @FXML
    public void cargarDatos() {
    	if(cuentaActual != null){
            comboCategoria.getItems().clear();
            // Si no se selecciona categoria, es una alerta sin categoria

            comboCategoria.getItems().addAll(cuentaActual.getCategorias());
            listaAlertasConfiguradas.setItems(FXCollections.observableArrayList(cuentaActual.getAlertas()));
            listaHistorial.setItems((FXCollections.observableArrayList(cuentaActual.getNotificaciones())));
        }
    }
    
    @FXML
    public void guardarAlertas() {
    	try {
    		String tipoAlerta = comboTipo.getValue();
    		double limite = Double.parseDouble(txtLimite.getText().replace(",", "."));
    		Categoria categoria = comboCategoria.getValue();
    		Alerta alerta = new Alerta(tipoAlerta, limite, categoria);
    		cuentaActual.getAlertas().add(alerta);
    		cuentaService.agregarCuenta(null, cuentaActual);
    		this.cargarDatos();
    		this.txtLimite.clear();
    		this.comboCategoria.getSelectionModel().clearSelection();
    	} catch (NumberFormatException e) {
    		System.err.println("Error en el formato numérico del campo límite");
    	}
    }
    
    @FXML
    public void borrarAlertas() {
    	Alerta alertaBorrar;
    	alertaBorrar = listaAlertasConfiguradas.getSelectionModel().getSelectedItem();
    	if(alertaBorrar != null) {
    		cuentaActual.getAlertas().remove(alertaBorrar);
    		cuentaService.agregarCuenta(null, cuentaActual);
    		this.cargarDatos();
    	}
    }
    
    @FXML
    public void limpiarHistorial() {
    	cuentaActual.getNotificaciones().clear();
    	cuentaService.agregarCuenta(null, cuentaActual);
    	this.cargarDatos();
    }
}
