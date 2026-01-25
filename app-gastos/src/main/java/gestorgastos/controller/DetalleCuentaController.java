package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import gestorgastos.importacion.*; // Asumiendo que tu lógica de importación está aquí
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DetalleCuentaController {

    // --- VISTA (@FXML) ---
    @FXML private Label lblTituloCuenta;
    @FXML private TableView<Gasto> tablaGastos;
    @FXML private TableColumn<Gasto, LocalDate> colFecha;
    @FXML private TableColumn<Gasto, String> colConcepto;
    @FXML private TableColumn<Gasto, String> colCategoria;
    @FXML private TableColumn<Gasto, Double> colImporte;
    @FXML private TableColumn<Gasto, String> colPagador;
    
    @FXML private Button btnAnadirGasto;
    @FXML private Button btnEditarGasto;
    @FXML private Button btnBorrarGasto;
    @FXML private Button btnImportar;
    
    @FXML private javafx.scene.layout.VBox panelSaldos;
    @FXML private ListView<String> listaSaldos;

    // --- MODELO ---
    private Cuenta cuentaActual;
    private final CuentaService cuentaService = CuentaService.getInstancia();

    @FXML
    public void initialize() {
        configurarTabla();
        
        // Eventos delegados a métodos privados
        btnAnadirGasto.setOnAction(e -> abrirFormularioGasto(null));
        btnEditarGasto.setOnAction(e -> abrirEditarGasto());
        btnBorrarGasto.setOnAction(e -> borrarGasto());
        btnImportar.setOnAction(e -> procesarImportacion());
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        lblTituloCuenta.setText("Gastos: " + cuenta.getNombre());
        
        // Asegurar categoría por defecto (Lógica de negocio simple)
        if (this.cuentaActual.getCategorias().isEmpty()) {
            this.cuentaActual.getCategorias().add(new Categoria("General", "Defecto", "#D3D3D3"));
        }

        actualizarVista();
    }

    // --- GESTIÓN DE LA VISTA (Responsabilidad del Controlador) ---
    
    private void configurarTabla() {
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
        colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcepto()));
        colImporte.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImporte()));
        colPagador.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPagador()));
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria().getNombre()));

        // Decoración de celdas (Círculos de color)
        colCategoria.setCellFactory(column -> new TableCell<Gasto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    setText(item);
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Gasto gasto = (Gasto) getTableRow().getItem();
                        Circle circle = new Circle(8);
                        try {
                            circle.setFill(Color.web(gasto.getCategoria().getColorHex()));
                            circle.setStroke(Color.DARKGRAY);
                        } catch (Exception e) { circle.setFill(Color.LIGHTGRAY); }
                        setGraphic(circle);
                    }
                }
            }
        });
    }

    private void actualizarVista() {
        // Refrescar Tabla
        tablaGastos.setItems(FXCollections.observableArrayList(cuentaActual.getGastos()));
        tablaGastos.refresh();

        // Refrescar Saldos (si aplica)
        if (cuentaActual instanceof CuentaCompartida) {
            panelSaldos.setVisible(true);
            calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
        } else {
            panelSaldos.setVisible(false);
        }
    }

    private void calcularYMostrarSaldos(CuentaCompartida cuentaComp) {
        listaSaldos.getItems().clear();
        Map<String, Double> saldos = cuentaComp.calcularSaldos();
        saldos.forEach((persona, cantidad) -> 
            listaSaldos.getItems().add(String.format("%s: %.2f €", persona, cantidad))
        );
        // Estilo rojo/verde
        listaSaldos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else { setText(item); setStyle(item.contains("-") ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); }
            }
        });
    }

    // --- ACCIONES DE USUARIO (Lógica de Interfaz) ---

    private void abrirEditarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            GestorDialogos.mostrarAlerta("Selecciona un gasto de la tabla para editarlo.");
            return;
        }
        abrirFormularioGasto(seleccionado);
    }

    private void borrarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        boolean confirmado = GestorDialogos.pedirConfirmacion("Borrar Gasto", "¿Seguro que quieres borrar: " + seleccionado.getConcepto() + "?");
        
        if (confirmado) {
            // Lógica de negocio
            cuentaActual.eliminarGasto(seleccionado);
            cuentaService.agregarCuenta(cuentaActual); // Guardar
            actualizarVista();
        }
    }

    private void abrirFormularioGasto(Gasto gastoEdicion) {
        // Usamos el GestorNavegacion para abrir el modal y obtener el controlador
        FormularioGastoController controller = GestorNavegacion.abrirModal(
            "FormularioGastoView.fxml", 
            gastoEdicion == null ? "Nuevo Gasto" : "Editar Gasto", 
            c -> ((FormularioGastoController) c).initAttributes(cuentaActual, gastoEdicion)
        );

        // Si el usuario guardó (el modal se cerró), procesamos
        if (controller != null && controller.isGuardadoConfirmado()) {
            Gasto gastoResultante = controller.getGastoResultado();
            
            if (gastoEdicion == null) {
                // NUEVO: Delegamos al servicio (Crea gasto, revisa alertas, notifica y guarda)
                Alerta alerta = cuentaService.agregarGasto(cuentaActual, gastoResultante);
                /*if(alerta != null) {
                    GestorDialogos.mostrarAlerta("⚠️ Límite " + alerta.getTipo() + " superado. Notificación guardada.");
                }*/
            } else {
                // EDICIÓN: Reemplazo manual y guardar
                int index = cuentaActual.getGastos().indexOf(gastoEdicion);
                if (index != -1) {
                    cuentaActual.getGastos().set(index, gastoResultante);
                    cuentaService.agregarCuenta(cuentaActual);
                }
            }
            actualizarVista();
        }
    }

    @FXML 
    private void procesarImportacion() {
        // ... (Tu código de importación original va aquí, usando GestorDialogos para los errores) ...
        // Recuerda usar cuentaService.agregarGasto(destino, nuevo) dentro del bucle.
    }

    // --- NAVEGACIÓN (Delegada al Gestor) ---

    @FXML private void volverInicio() {
        GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "PrincipalView.fxml", "Mis Cuentas", null);
    }

    @FXML private void irACategorias() {
        GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "GestionCategoriasView.fxml", "Categorías", 
            (GestionCategoriasController c) -> c.setCuenta(cuentaActual));
    }

    @FXML private void irAVisualizacion() {
        GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "VisualizacionView.fxml", "Gráficos", 
            (VisualizacionController c) -> c.setCuenta(cuentaActual));
    }

    @FXML private void irAAlertas() {
        // Alertas se abre "encima" sin cerrar la actual, podemos usar abrirModal o navegar normal
        // Si quieres que sea una ventana nueva:
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/AlertaView.fxml"));
            javafx.scene.Parent root = loader.load();
            AlertaController controller = loader.getController();
            controller.setCuenta(cuentaActual);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch(Exception e) { e.printStackTrace(); }
    }

    @FXML private void irACMD() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/TerminalView.fxml"));
            javafx.scene.Parent root = loader.load();
            TerminalController controller = loader.getController();
            controller.setCuenta(cuentaActual);
            controller.setOnUpdate(this::actualizarVista); // Callback limpio
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch(Exception e) { e.printStackTrace(); }
    }
}