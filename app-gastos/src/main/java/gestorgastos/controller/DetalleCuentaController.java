package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import gestorgastos.services.SesionService;
import gestorgastos.dto.GastoTemporal;
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
        
        this.cuentaActual = SesionService.getInstancia().getCuentaActiva();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Gastos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos Soportados", "*.csv", "*.txt", "*.json", "*.xlsx", "*.xml")
        );

        File fichero = fileChooser.showOpenDialog(lblTituloCuenta.getScene().getWindow());
        if (fichero == null) return;

        // 1. Obtener el importador (Patrón Factory)
        Importador importador = FactoriaImportacion.getImportador(fichero.getAbsolutePath());
        if (importador == null) {
            GestorDialogos.mostrarError("Error", "Formato no soportado.");
            return;
        }

        try {
            // 2. Leer datos temporales
            List<GastoTemporal> temporales = importador.leerFichero(fichero.getAbsolutePath());
            
            // 3. Obtener cuentas del usuario actual (PR #5: Usuario implícito en sesión)
            List<Cuenta> cuentasUsuario = cuentaService.getCuentasUsuarioActual();

            int insertados = 0;
            int descartados = 0;
            int alertasGeneradas = 0;

            for (GastoTemporal t : temporales) {
                // Buscamos la cuenta destino por nombre
                Optional<Cuenta> cMatch = cuentasUsuario.stream()
                        .filter(c -> c.getNombre().equalsIgnoreCase(t.nombreCuenta))
                        .findFirst();

                if (cMatch.isEmpty()) {
                    System.out.println("Descartado: No existe la cuenta: " + t.nombreCuenta);
                    descartados++;
                    continue;
                }

                Cuenta destino = cMatch.get();

                // 4. Validación de Miembros (Lógica de Negocio básica)
                boolean valido = true;
                if (destino instanceof CuentaCompartida) {
                    // Tanto para Compartida como Proporcional (que hereda de Compartida)
                    List<String> miembros = ((CuentaCompartida) destino).getMiembros();
                    // Si el pagador no es "Yo" y no está en la lista, es inválido
                    if (!t.pagador.equalsIgnoreCase("Yo") && 
                        miembros.stream().noneMatch(m -> m.equalsIgnoreCase(t.pagador))) {
                        valido = false;
                        System.out.println("Descartado: El usuario " + t.pagador + " no pertenece a la cuenta.");
                    }
                }

                if (valido) {
                    // Asignar Categoría (Buscamos la real o usamos la primera por defecto)
                    Categoria catReal = destino.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(t.categoria))
                            .findFirst()
                            .orElse(destino.getCategorias().get(0));

                    Gasto nuevo = new Gasto(t.concepto, t.importe, t.fecha, catReal, t.pagador);
                    if(t.hora != null) nuevo.setHora(t.hora);

                    Alerta alertaSaltada = cuentaService.agregarGasto(destino, nuevo); 
                    
                    if (alertaSaltada != null) {
                        alertasGeneradas++;
                    }
                    insertados++;
                } else {
                    descartados++;
                }
            }

            // 5. Actualizar la vista solo si estamos viendo la cuenta afectada
            actualizarVista();
            
            GestorDialogos.mostrarAlerta("Importación Finalizada\nInsertados: " + insertados + 
                                         "\n(Alertas nuevas: " + alertasGeneradas + ")" +
                                         "\nDescartados: " + descartados);

        } catch (Exception e) {
            e.printStackTrace();
            GestorDialogos.mostrarError("Error Importación", "Fallo al leer el archivo: " + e.getMessage());
        }
    }

    // --- NAVEGACIÓN (Delegada al Gestor) ---

    @FXML private void volverInicio() {
        GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "PrincipalView.fxml", "Mis Cuentas", null);
    }

    @FXML private void irACategorias() {
        //GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "GestionCategoriasView.fxml", "Categorías", 
            //(GestionCategoriasController c) -> c.setCuenta(cuentaActual));
    	gestorgastos.services.SesionService.getInstancia().setCuentaActiva(cuentaActual);
    	GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "GestionCategoriasView.fxml", "Categorías", null);
    }

    @FXML private void irAVisualizacion() {
        //GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "VisualizacionView.fxml", "Gráficos", 
            //(VisualizacionController c) -> c.setCuenta(cuentaActual));
    	
    	gestorgastos.services.SesionService.getInstancia().setCuentaActiva(cuentaActual);
    	GestorNavegacion.navegar((Stage) lblTituloCuenta.getScene().getWindow(), "VisualizacionView.fxml", "Categorías", null);
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