package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class DetalleCuentaController {

    // --- UI: TABLA ---
    @FXML private Label lblTituloCuenta;
    @FXML private TableView<Gasto> tablaGastos;
    @FXML private TableColumn<Gasto, LocalDate> colFecha;
    @FXML private TableColumn<Gasto, String> colConcepto;
    @FXML private TableColumn<Gasto, String> colCategoria;
    @FXML private TableColumn<Gasto, Double> colImporte;
    @FXML private TableColumn<Gasto, String> colPagador;

    // --- UI: BOTONES DERECHA ---
    @FXML private Button btnAnadirGasto;
    @FXML private Button btnEditarGasto;
    @FXML private Button btnBorrarGasto;
    @FXML private Button btnImportar;

    // --- UI: PANEL SALDOS ---
    @FXML private javafx.scene.layout.VBox panelSaldos;
    @FXML private ListView<String> listaSaldos;

    // --- MODELO ---
    private Cuenta cuentaActual;
    private CuentaService cuentaService = CuentaService.getInstancia();

    @FXML
    public void initialize() {
        // 1. Configurar QUÉ datos van en cada columna (ValueFactory)
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
        colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcepto()));
        colImporte.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImporte()));
        colPagador.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPagador()));
        
        // El valor base de esta columna es el NOMBRE de la categoría
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria().getNombre()));

        // --- LO NUEVO: Configurar CÓMO se ve la celda (CellFactory) para el COLOR ---
        colCategoria.setCellFactory(column -> new TableCell<Gasto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); // Obligatorio llamar a super

                if (empty || item == null) {
                    // Si la fila está vacía, limpiamos texto y gráficos
                    setText(null);
                    setGraphic(null);
                } else {
                    // 1. Ponemos el nombre de la categoría (texto)
                    setText(item);

                    // 2. Intentamos obtener el objeto Gasto de esta fila para sacar el color
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Gasto gasto = (Gasto) getTableRow().getItem();
                        Categoria cat = gasto.getCategoria();

                        // 3. Creamos el círculo visual
                        Circle circle = new Circle(8); // Radio de 8px
                        try {
                            // Convertimos el String Hex (#FF0000) a Color de JavaFX
                            circle.setFill(Color.web(cat.getColorHex()));
                            circle.setStroke(Color.DARKGRAY); // Un borde fino queda mejor
                        } catch (Exception e) {
                            // Si el color falla o es nulo, ponemos gris por defecto
                            circle.setFill(Color.LIGHTGRAY);
                        }
                        
                        // Añadimos el círculo a la celda
                        setGraphic(circle);
                    }
                }
            }
        });
        // ---------------------------------------------------------------------------

        // 2. Configurar acciones de los botones
        btnAnadirGasto.setOnAction(e -> abrirCrearGasto());
        btnEditarGasto.setOnAction(e -> abrirEditarGasto());
        btnBorrarGasto.setOnAction(e -> borrarGasto());
        
        // Funcionalidades futuras
        btnImportar.setOnAction(e -> System.out.println("Funcionalidad Importar pendiente..."));
        
        // Acciones del menú superior (asegúrate de que estos métodos existan en tu controller)
        // Ejemplo: btnCategorias.setOnAction(e -> irACategorias());
    }

    /**
     * Recibe la cuenta desde el controlador anterior.
     */
    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        lblTituloCuenta.setText("Gastos: " + cuenta.getNombre());

        actualizarTabla();

        // Gestión del Panel de Saldos (Solo para Compartidas/Especiales)
        if (cuenta instanceof CuentaCompartida) {
            panelSaldos.setVisible(true);
            calcularYMostrarSaldos((CuentaCompartida) cuenta);
        } else {
            panelSaldos.setVisible(false);
        }
    }

    // --- LÓGICA CRUD (Create, Read, Update, Delete) ---

    private void abrirCrearGasto() {
        // null indica que estamos creando uno nuevo
        abrirFormularioGasto(null);
    }

    private void abrirEditarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un gasto de la tabla para editarlo.");
            return;
        }
        // Pasamos el gasto seleccionado para editarlo
        abrirFormularioGasto(seleccionado);
    }

    private void abrirFormularioGasto(Gasto gastoEdicion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/FormularioGastoView.fxml"));
            Parent root = loader.load();

            FormularioGastoController controller = loader.getController();
            // Le pasamos la cuenta (para saber quiénes son los miembros) y el gasto (si es edición)
            controller.initAttributes(cuentaActual, gastoEdicion);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.setTitle(gastoEdicion == null ? "Nuevo Gasto" : "Editar Gasto");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Espera a que se cierre

            // Al volver, verificamos si hubo resultado
            Gasto resultado = controller.getGastoResultado();
            if (resultado != null) {
                if (gastoEdicion == null) {
                    // Si era nuevo, lo añadimos a la lista
                    cuentaActual.agregarGasto(resultado);
                }
                // Si era edición, el objeto ya se modificó por referencia en el otro controlador.
                
                // GUARDAMOS Y REFRESCAMOS TODO
                guardarCambiosYRefrescar();
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir el formulario de gasto.");
        }
    }

    private void borrarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un gasto para borrar.");
            return;
        }

        // Confirmación
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Borrar Gasto");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres borrar: " + seleccionado.getConcepto() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cuentaActual.eliminarGasto(seleccionado);
            guardarCambiosYRefrescar();
        }
    }

    /**
     * Método centralizado para guardar en JSON y actualizar la vista.
     */
    private void guardarCambiosYRefrescar() {
        // 1. Persistencia: Sobrescribimos la cuenta en el archivo JSON
        cuentaService.agregarCuenta(null, cuentaActual);

        // 2. Vista: Refrescamos la tabla
        actualizarTabla();

        // 3. Saldos: Recalculamos si es necesario
        if (cuentaActual instanceof CuentaCompartida) {
            calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
        }
    }

    private void actualizarTabla() {
        tablaGastos.setItems(FXCollections.observableArrayList(cuentaActual.getGastos()));
        tablaGastos.refresh();
    }

    private void calcularYMostrarSaldos(CuentaCompartida cuentaComp) {
        listaSaldos.getItems().clear();

        // Cálculo usando Streams (definido en el Modelo)
        Map<String, Double> saldos = cuentaComp.calcularSaldos();

        saldos.forEach((persona, cantidad) -> {
            String texto = String.format("%s: %.2f €", persona, cantidad);
            listaSaldos.getItems().add(texto);
        });

        // Formato visual: Rojo si debe, Verde si le deben
        listaSaldos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("-")) setStyle("-fx-text-fill: red;");
                    else setStyle("-fx-text-fill: green;");
                }
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- NAVEGACIÓN ---

    @FXML
    private void volverInicio() {
        try {
            // 1. Cargar de nuevo la vista Principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/PrincipalView.fxml"));
            Parent root = loader.load();

            // 2. Crear la nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setTitle("Gestor de Gastos - Mis Cuentas");
            stage.setScene(new Scene(root));
            stage.show();

            // 3. Cerrar la ventana actual (Detalle de Gastos)
            Stage currentStage = (Stage) lblTituloCuenta.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 // 1. Método para navegar a Categorías
    @FXML
    private void irACategorias() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/GestionCategoriasView.fxml"));
            Parent root = loader.load();
            
            GestionCategoriasController controller = loader.getController();
            controller.setCuenta(cuentaActual); // Le pasamos la cuenta para poder volver luego

            Stage stage = (Stage) lblTituloCuenta.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    @FXML private void irAVisualizacion() { System.out.println("Ir a Gráficos"); }
    @FXML private void irAAlertas() { System.out.println("Ir a Alertas"); }
    @FXML private void irACMD() { System.out.println("Ir a Terminal"); }
}
