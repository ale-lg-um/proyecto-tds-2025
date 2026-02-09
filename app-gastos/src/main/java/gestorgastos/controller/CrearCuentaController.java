package gestorgastos.controller;

import gestorgastos.services.CuentaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class CrearCuentaController {

    @FXML private TextField nombreCuentaField;
    @FXML private Button btnCrear;

    @FXML private ComboBox<String> comboTipo;

    @FXML private VBox panelMiembros;

    @FXML private TextField txtNuevoMiembro;
    @FXML private TextField txtPorcentaje;
    @FXML private Button btnAnadirMiembro;
    @FXML private ListView<String> listaMiembrosView;
    @FXML private Label lblError;
    @FXML private Label lblProgresoPorcentaje;

    private CuentaService cuentaService = CuentaService.getInstancia();
    private Runnable onCuentaCreada;

    private ObservableList<String> miembrosVisuales = FXCollections.observableArrayList();
    private Map<String, Double> mapaPorcentajes = new HashMap<>();

    public void setOnCuentaCreada(Runnable callback) {
        this.onCuentaCreada = callback;
    }

    @FXML
    public void initialize() {
        comboTipo.setItems(FXCollections.observableArrayList("Personal", "Compartida", "Especial"));
        comboTipo.getSelectionModel().select("Personal");

        listaMiembrosView.setItems(miembrosVisuales);

        panelMiembros.setVisible(false);
        txtPorcentaje.setManaged(false);
        txtPorcentaje.setVisible(false);
        lblProgresoPorcentaje.setVisible(false);

        comboTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            actualizarInterfaz(newVal);
        });

        btnAnadirMiembro.setOnAction(e -> agregarMiembro());
        btnCrear.setOnAction(e -> crearLaCuenta());
    }

    private void actualizarInterfaz(String tipo) {
        miembrosVisuales.clear();
        mapaPorcentajes.clear();
        lblError.setText("");
        
        actualizarProgreso();

        if ("Personal".equals(tipo)) {
            panelMiembros.setVisible(false);
        } else {
            panelMiembros.setVisible(true);

            if ("Especial".equals(tipo)) {
                txtPorcentaje.setManaged(true);
                txtPorcentaje.setVisible(true);
                txtPorcentaje.setPromptText("% (Ej: 30)");
            } else {
                txtPorcentaje.setManaged(false);
                txtPorcentaje.setVisible(false);
            }
        }
    }

    private void agregarMiembro() {
        String nombrePersona = txtNuevoMiembro.getText().trim();
        if (nombrePersona.isEmpty()) return;

        String tipo = comboTipo.getValue();

        if ("Especial".equals(tipo)) {
            try {
                String porcText = txtPorcentaje.getText().replace(",", ".");
                double porcentaje = Double.parseDouble(porcText);

                mapaPorcentajes.put(nombrePersona, porcentaje);
                miembrosVisuales.add(nombrePersona + " (" + porcentaje + "%)");
                
                actualizarProgreso(); 

            } catch (NumberFormatException ex) {
                lblError.setText("Error: El porcentaje debe ser un número");
                return;
            }
        } else {
            miembrosVisuales.add(nombrePersona);
        }

        txtNuevoMiembro.clear();
        txtPorcentaje.clear();
        txtNuevoMiembro.requestFocus();
    }

    private void crearLaCuenta() {
        String nombre = nombreCuentaField.getText();
        if (nombre == null || nombre.isBlank()) {
            lblError.setText("⚠️ El nombre de la cuenta es obligatorio");
            return;
        }
        
        String tipo = comboTipo.getValue();

        try {
            cuentaService.procesarNuevaCuenta(
                nombre, 
                tipo, 
                miembrosVisuales, 
                mapaPorcentajes
            );

            if (onCuentaCreada != null) onCuentaCreada.run();
            ((Stage) btnCrear.getScene().getWindow()).close();

        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }
    
    private void actualizarProgreso() {
        if ("Especial".equals(comboTipo.getValue())) {
            double suma = mapaPorcentajes.values().stream().mapToDouble(d -> d).sum();
            
            lblProgresoPorcentaje.setVisible(true);
            lblProgresoPorcentaje.setText("Total asignado: " + String.format("%.2f", suma) + "% (Falta: " + String.format("%.2f", 100.0 - suma) + "%)");

            if (Math.abs(suma - 100.0) < 0.01) {
                 lblProgresoPorcentaje.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                 lblProgresoPorcentaje.setStyle("-fx-text-fill: red;");
            }
        } else {
            lblProgresoPorcentaje.setVisible(false);
        }
    }
}