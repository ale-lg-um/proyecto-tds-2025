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
    private CuentaService cuentaService = CuentaService.getInstancia();
    private Cuenta cuentaActual;

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        
        // Si por alguna razón la cuenta viene sin categorías, inicializamos
        if (this.cuentaActual.getCategorias() == null) {
            this.cuentaActual.setCategorias(new ArrayList<>()); // Aseguramos que la lista exista
        }
        if (this.cuentaActual.getCategorias().isEmpty()) {
            this.cuentaActual.getCategorias().add(new Categoria("General", "Defecto", "#D3D3D3"));
        }

        // --- ESTA ES LA LÍNEA QUE TE FALTABA ---
        // Ahora que ya tenemos la cuenta, ¡cargamos la lista visualmente!
        cargarCategorias();
        // ---------------------------------------
    }

    @FXML
    public void initialize() {
        // CellFactory para mostrar el círculo de color
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
        
        // Esperamos a tener la cuenta para cargar (se hace en setCuenta o al mostrar)
        // Pero como initialize corre antes de setCuenta, cargaremos en un método aparte o usaremos un listener si fuera necesario.
        // En este caso, llamaremos a cargarCategorias() al final de setCuenta() mejor, 
        // pero por seguridad si ya está seteado:
        if (cuentaActual != null) cargarCategorias();
    }
    
    // Método auxiliar para refrescar la vista
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
                // 1. Añadimos a LA CUENTA ACTUAL (No al servicio global)
                cuentaActual.getCategorias().add(controller.getCategoriaResultado());
                
                // 2. Guardamos la cuenta en el JSON para persistir la nueva categoría
                cuentaService.agregarCuenta(null, cuentaActual);
                
                // 3. Refrescamos
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
            // 1. Buscamos la categoría destino (General) dentro de ESTA cuenta
            Categoria catGeneral = cuentaActual.getCategorias().stream()
                    .filter(c -> "General".equals(c.getNombre()))
                    .findFirst()
                    .orElse(null);

            if (catGeneral != null) {
                // 2. Reasignamos los gastos de ESTA cuenta
                for (Gasto g : cuentaActual.getGastos()) {
                    if (g.getCategoria().equals(seleccionada)) {
                        g.setCategoria(catGeneral);
                    }
                }
            }

            // 3. Borramos la categoría de la lista de la cuenta
            cuentaActual.getCategorias().remove(seleccionada);
            
            // 4. Guardamos cambios en disco
            cuentaService.agregarCuenta(null, cuentaActual);
            
            cargarCategorias();
        }
    }

    // --- NAVEGACIÓN ---
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
    
    @FXML private void irAAlertas() { System.out.println("Ir a Alertas"); }
    @FXML private void irACMD() { System.out.println("Ir a Consola"); }
    @FXML private void irAVisualizacion() { System.out.println("Ir a Gráficos"); }
    
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