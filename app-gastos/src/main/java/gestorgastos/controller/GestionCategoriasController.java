package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.stage.*;
import java.io.IOException;

public class GestionCategoriasController {

    @FXML private Label lblTituloCuenta;

    @FXML private ListView<Categoria> listaCategorias;
    private CuentaService cuentaService = CuentaService.getInstancia();
    private Cuenta cuentaActual; // Para poder volver a la pantalla de gastos

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
    }

    @FXML
    public void initialize() {
        cargarCategorias();

        // CellFactory para mostrar el círculo de color en la lista
        listaCategorias.setCellFactory(lv -> new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNombre());
                    // Círculo de color
                    Circle circulo = new Circle(10);
                    circulo.setFill(Color.web(item.getColorHex()));
                    circulo.setStroke(Color.BLACK);
                    setGraphic(circulo);
                }
            }
        });
    }

    private void cargarCategorias() {
        listaCategorias.getItems().setAll(cuentaService.getCategorias());
    }

    @FXML
    private void crearCategoria() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/FormularioCategoriaView.fxml"));
            Parent root = loader.load();
            FormularioCategoriaController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.getCategoriaResultado() != null) {
                cuentaService.agregarCategoria(controller.getCategoriaResultado());
                cargarCategorias();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void borrarCategoria() {
        Categoria seleccionada = listaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;
        
        if ("General".equals(seleccionada.getNombre())) {
            mostrarAlerta("No puedes borrar la categoría General");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("¿Borrar '" + seleccionada.getNombre() + "'? Sus gastos pasarán a 'General'.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cuentaService.borrarCategoria(seleccionada);
            cargarCategorias();
        }
    }

    // NAVEGACIÓN
    @FXML
    private void irAGastos() {
        try {
            // Volvemos a DetalleCuentaView pasándole la cuenta que teníamos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/DetalleCuentaView.fxml"));
            Parent root = loader.load();
            DetalleCuentaController controller = loader.getController();
            controller.setCuenta(cuentaActual); // ¡Importante pasar la cuenta!

            Stage stage = (Stage) listaCategorias.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
    
 // En GestionCategoriasController.java añade al final:
    @FXML private void irAAlertas() { System.out.println("Ir a Alertas"); }
    @FXML private void irACMD() { System.out.println("Ir a Consola"); }
    @FXML private void irAVisualizacion() { System.out.println("Ir a Gráficos"); }
    @FXML
    private void volverInicio() {
        try {
            // 1. Cargar la vista Principal (Inicio)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/PrincipalView.fxml"));
            Parent root = loader.load();

            // 2. Crear y mostrar la ventana nueva
            Stage stage = new Stage();
            stage.setTitle("Gestor de Gastos - Mis Cuentas");
            stage.setScene(new Scene(root));
            stage.show();

            // 3. Cerrar la ventana actual (Categorías)
            // Usamos 'listaCategorias' para obtener la referencia a la ventana actual
            Stage currentStage = (Stage) listaCategorias.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setContentText(msg); a.showAndWait();
    }
}