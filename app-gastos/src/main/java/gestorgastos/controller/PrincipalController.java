package gestorgastos.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import gestorgastos.model.Usuario;
import gestorgastos.model.Cuenta;
import gestorgastos.services.SesionService;
import gestorgastos.services.CuentaService;


public class PrincipalController {

	@FXML
	private Label saludoLabel;
	@FXML
	private ListView<Cuenta> listaCuentas;
	@FXML
	private Button btnCrearCuenta;

	@FXML
	private Button btnEntrarCuenta;
	@FXML
	private Button btnConfirmarCuentaCompartida;
	@FXML
	private ComboBox<String> tipoCuentaCompartidaBox;


	private CuentaService cuentaService = CuentaService.getInstancia();

	@FXML
	public void initialize() {
		Usuario usuario = SesionService.getInstancia().getUsuarioActivo();
		if (usuario != null) {
			saludoLabel.setText("BIENVENIDO, " + usuario.getNombre());
			listaCuentas.getItems().setAll(cuentaService.getCuentasDe(usuario));
		} else {
			saludoLabel.setText("BIENVENIDO");
			listaCuentas.getItems().clear();
		}

		// Aquí inicializas el ComboBox con las opciones
	    tipoCuentaCompartidaBox.getItems().addAll(
	        "Cuenta compartida",
	        "Cuenta compartida especial"
	    );
	    
		btnEntrarCuenta.setOnAction(e -> {
			Cuenta seleccionada = listaCuentas.getSelectionModel().getSelectedItem();
			if (seleccionada != null) {
				System.out.println("Entrando en cuenta: " + seleccionada.getNombre());
				// Aquí cargarías la vista de la cuenta
			} else {
				System.out.println("⚠️ Selecciona una cuenta primero");
			}
		});

		btnCrearCuenta.setOnAction(e -> {
		    try {
		        FXMLLoader loader = new FXMLLoader(
		            getClass().getResource("/gestorgastos/app_gastos/CrearCuentaView.fxml")
		        );
		        Scene scene = new Scene(loader.load());

		        // Obtener controlador y pasar callback
		        CrearCuentaController crearController = loader.getController();
		        crearController.setOnCuentaCreada(() -> {
		            Usuario usuario2 = SesionService.getInstancia().getUsuarioActivo();
		            if (usuario2 != null) {
		                listaCuentas.getItems().setAll(cuentaService.getCuentasDe(usuario2));
		                listaCuentas.refresh();
		            } else {
		                System.out.println("⚠️ No hay usuario activo al refrescar cuentas");
		            }
		        });

		        // Crear ventana modal
		        Stage stage = new Stage();
		        stage.setTitle("Crear Nueva Cuenta");
		        stage.setScene(scene);
		        stage.initOwner(btnCrearCuenta.getScene().getWindow());
		        stage.initModality(Modality.WINDOW_MODAL);
		        stage.show();

		    } catch (Exception ex) {
		        ex.printStackTrace();
		        System.out.println("❌ Error al abrir la ventana de creación de cuenta: " + ex.getMessage());
		    }
		});


		
		
		btnConfirmarCuentaCompartida.setOnAction(e -> {
	        String seleccion = tipoCuentaCompartidaBox.getValue();
	        Usuario usuario3 = SesionService.getInstancia().getUsuarioActivo();

	        if (seleccion == null) {
	            System.out.println("⚠️ No se ha seleccionado tipo de cuenta");
	        } else {
	            System.out.println("📝 Tipo de cuenta seleccionada: " + seleccion);
	        }

	        listaCuentas.getItems().setAll(cuentaService.getCuentasDe(usuario));
	        listaCuentas.refresh();
	    });
		
		
	}

}
