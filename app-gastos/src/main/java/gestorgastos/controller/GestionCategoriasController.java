package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.stage.*;
import java.io.IOException;
import java.util.ArrayList;

public class GestionCategoriasController {

    @FXML private ListView<Categoria> listaCategorias;
    @FXML private Label lblTituloCuenta;

    private CuentaService cuentaService = CuentaService.getInstancia();
    private Cuenta cuentaActual;

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        
        // Inicialización de seguridad
        if (this.cuentaActual.getCategorias() == null) {
            this.cuentaActual.setCategorias(new ArrayList<>());
        }
        if (this.cuentaActual.getCategorias().isEmpty()) {
            this.cuentaActual.getCategorias().add(new Categoria("General", "Defecto", "#D3D3D3"));
        }

        cargarCategorias();
    }

    @FXML
    public void initialize() {
        // Configuración visual de la lista (Círculos de colores)
        listaCategorias.setCellFactory(lv -> new ListCell<Categoria>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNombre());
                    Circle circulo = new Circle(10);
                    try {
                        circulo.setFill(Color.web(item.getColorHex()));
                    } catch (Exception e) { circulo.setFill(Color.GREY); }
                    circulo.setStroke(Color.BLACK);
                    setGraphic(circulo);
                }
            }
        });
        
        if (cuentaActual != null) cargarCategorias();
    }
    
    public void cargarCategorias() {
        if (cuentaActual != null) {
            listaCategorias.setItems(FXCollections.observableArrayList(cuentaActual.getCategorias()));
        }
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
                cuentaActual.getCategorias().add(controller.getCategoriaResultado());
                cuentaService.agregarCuenta(null, cuentaActual);
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
            Categoria catGeneral = cuentaActual.getCategorias().stream()
                    .filter(c -> "General".equals(c.getNombre()))
                    .findFirst().orElse(null);

            if (catGeneral != null) {
                for (Gasto g : cuentaActual.getGastos()) {
                    if (g.getCategoria().equals(seleccionada)) {
                        g.setCategoria(catGeneral);
                    }
                }
            }

            cuentaActual.getCategorias().remove(seleccionada);
            cuentaService.agregarCuenta(null, cuentaActual);
            cargarCategorias();
        }
    }

    // Botones superiores
    @FXML
    private void irAGastos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/DetalleCuentaView.fxml"));
            Parent root = loader.load();
            DetalleCuentaController controller = loader.getController();
            controller.setCuenta(cuentaActual); // Mantenemos la cuenta

            Stage stage = new Stage();
            stage.setTitle("Gastos: " + cuentaActual.getNombre());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            
            ((Stage) listaCategorias.getScene().getWindow()).close();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    @FXML 
    private void irACMD() { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/TerminalView.fxml"));
            Parent root = loader.load();

            TerminalController controller = loader.getController();
            controller.setCuenta(cuentaActual);

            // Callback: Si pasa algo en la terminal, refrescamos la lista de categorías (por precaución)
            controller.setOnUpdate(() -> {
                cargarCategorias();
            });

            Stage stage = new Stage();
            stage.setTitle("Terminal - " + cuentaActual.getNombre());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir la terminal.");
        }
    }

    @FXML
    private void irAVisualizacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/VisualizacionView.fxml"));
            Parent root = loader.load();

            VisualizacionController controller = loader.getController();
            controller.setCuenta(cuentaActual); 

            Stage stage = (Stage) listaCategorias.getScene().getWindow();
            // -----------------------

            stage.setScene(new Scene(root, 1100, 750)); 
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void irAAlertas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/AlertaView.fxml"));
            Parent root = loader.load();

            
            AlertaController controller = loader.getController();
            controller.setCuenta(cuentaActual);

            Stage stage = new Stage();
            stage.setTitle("Configuración de Alertas - " + cuentaActual.getNombre());
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR, "Error al abrir la ventana de alertas.").show();
        }
    }

    @FXML
    private void volverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/PrincipalView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestor de Gastos - Mis Cuentas");
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) listaCategorias.getScene().getWindow()).close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setContentText(msg); a.showAndWait();
    }
}
