package gestorgastos.controller;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;
import gestorgastos.services.CuentaService;
import gestorgastos.services.SesionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PrincipalController {

    // Coinciden con tu último PrincipalView.fxml
    @FXML private Label saludoLabel;
    @FXML private ListView<Cuenta> listaCuentas; // OJO: En tu FXML se llama "listaCuentas"
    @FXML private Button btnCrearCuenta;
    @FXML private Button btnEntrarCuenta;

    private CuentaService cuentaService = CuentaService.getInstancia();
    private SesionService sesionService = SesionService.getInstancia();

    @FXML
    public void initialize() {
        // 1. Poner el saludo con el nombre del usuario
        Usuario usuario = sesionService.getUsuarioActivo();
        if (usuario != null) {
            saludoLabel.setText("Hola, " + usuario.getNombre());
            cargarCuentas(); // Cargar la lista al iniciar
        }

        // 2. Configurar acciones de los botones
        // Como en tu FXML no pusiste onAction="#...", lo definimos aquí:
        btnCrearCuenta.setOnAction(e -> abrirVentanaCrearCuenta());
        btnEntrarCuenta.setOnAction(e -> entrarEnCuenta());
    }

    private void cargarCuentas() {
        Usuario usuario = sesionService.getUsuarioActivo();
        if (usuario != null) {
            List<Cuenta> cuentas = cuentaService.getCuentasDe(usuario);
            listaCuentas.getItems().setAll(cuentas);
        }
    }

    private void abrirVentanaCrearCuenta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/CrearCuentaView.fxml"));
            Parent root = loader.load();

            // Obtenemos el controlador de la ventana emergente para pasarle el "callback"
            CrearCuentaController controller = loader.getController();
            controller.setOnCuentaCreada(this::cargarCuentas); // Al cerrar, recarga la lista

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Crear Nueva Cuenta");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir la ventana de creación: " + e.getMessage());
        }
    }

    private void entrarEnCuenta() {
        Cuenta cuentaSeleccionada = listaCuentas.getSelectionModel().getSelectedItem();

        if (cuentaSeleccionada == null) {
            mostrarAlerta("Por favor, selecciona una cuenta de la lista primero.");
            return;
        }

        try {
            // 1. Cargar el FXML de detalle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/DetalleCuentaView.fxml"));
            Parent root = loader.load();

            // 2. Obtener el controlador y PASARLE LA CUENTA
            DetalleCuentaController controller = loader.getController();
            controller.setCuenta(cuentaSeleccionada);

            // 3. Cambiar la escena (o abrir nueva ventana, depende de tu gusto)
            // Opción A: Abrir nueva ventana (más fácil de gestionar ahora)
            Stage stage = new Stage();
            stage.setTitle("Gestión de: " + cuentaSeleccionada.getNombre());
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // Se ve mejor en grande
            stage.show();
            
            // Opcional: Cerrar la ventana principal actual si quieres
            ((Stage) btnEntrarCuenta.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir los detalles de la cuenta.");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}