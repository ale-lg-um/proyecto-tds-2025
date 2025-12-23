package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.ServicioAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalTime; // <--- Importar

import java.time.LocalDate;


public class FormularioGastoController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtConcepto;
    @FXML private TextField txtImporte;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<Categoria> comboCategoria;
    
    @FXML private VBox panelPagador;
    @FXML private ComboBox<String> comboPagador;
    @FXML private Label lblError;
    
    @FXML private Spinner<Integer> spinHora;
    @FXML private Spinner<Integer> spinMinuto;

    private Gasto gastoResultado;
    private boolean esEdicion = false;
    private Cuenta cuentaAsociada; // Necesario para saber los miembros y las categorías

    @FXML
    public void initialize() {
        // En initialize SOLO configuramos cosas que no dependen de la cuenta
        dateFecha.setValue(LocalDate.now());
        spinHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        spinMinuto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        
        // ¡IMPORTANTE!: Aquí NO cargamos las categorías todavía, 
        // porque aún no sabemos qué cuenta es. Lo hacemos en initAttributes.
    }

 // Método para configurar la ventana según si es CREAR o EDITAR
    public void initAttributes(Cuenta cuenta, Gasto gastoAEditar) {
        this.cuentaAsociada = cuenta;

        // 1. CARGAR LAS CATEGORÍAS
        comboCategoria.setItems(FXCollections.observableArrayList(cuenta.getCategorias()));

        if (!comboCategoria.getItems().isEmpty()) {
            comboCategoria.getSelectionModel().selectFirst();
        }

        // 2. Configurar Visibilidad del Pagador
        if (cuenta instanceof CuentaCompartida) {
            panelPagador.setVisible(true);
            panelPagador.setManaged(true);
            comboPagador.setItems(FXCollections.observableArrayList(((CuentaCompartida) cuenta).getMiembros()));
        } else {
            panelPagador.setVisible(false);
            panelPagador.setManaged(false); 
        }

        // 3. Rellenar datos
        if (gastoAEditar != null) {
            // --- MODO EDICIÓN ---
            this.esEdicion = true;
            this.gastoResultado = gastoAEditar; 
            lblTitulo.setText("Editar Gasto");
            
            txtConcepto.setText(gastoAEditar.getConcepto());
            txtImporte.setText(String.valueOf(gastoAEditar.getImporte()));
            dateFecha.setValue(gastoAEditar.getFecha());
            
            // ---> AQUÍ CARGAMOS LA HORA GUARDADA <---
            spinHora.getValueFactory().setValue(gastoAEditar.getHora().getHour());
            spinMinuto.getValueFactory().setValue(gastoAEditar.getHora().getMinute());
            // ----------------------------------------
            
            comboCategoria.setValue(gastoAEditar.getCategoria());
            
            if (cuenta instanceof CuentaCompartida) {
                comboPagador.setValue(gastoAEditar.getPagador());
            }
        } else {
            // --- MODO CREAR (NUEVO) ---
            lblTitulo.setText("Nuevo Gasto");
            
            // ---> AQUÍ INICIALIZAMOS LA FECHA Y HORA ACTUALES <---
            dateFecha.setValue(java.time.LocalDate.now()); // Ponemos fecha de hoy
            
            java.time.LocalTime ahora = java.time.LocalTime.now(); // Hora de ahora mismo
            spinHora.getValueFactory().setValue(ahora.getHour());
            spinMinuto.getValueFactory().setValue(ahora.getMinute());
            // -----------------------------------------------------

            if (cuenta instanceof CuentaCompartida && !comboPagador.getItems().isEmpty()) {
                comboPagador.getSelectionModel().selectFirst();
            }
        }
    }

    public Gasto getGastoResultado() {
        return gastoResultado;
    }

    @FXML
    private void guardar() {
        // 1. Validaciones básicas
        String concepto = txtConcepto.getText();
        if (concepto.isEmpty()) {
            lblError.setText("El concepto es obligatorio");
            return;
        }

        double importe;
        try {
            importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            lblError.setText("El importe debe ser un número válido");
            return;
        }

        LocalDate fecha = dateFecha.getValue();
        if (fecha == null) {
            lblError.setText("La fecha es obligatoria");
            return;
        }
        
        Categoria categoria = comboCategoria.getValue();
        if (categoria == null) {
            lblError.setText("Debes tener al menos una categoría");
            return;
        }
        
        String pagador = "Yo";
        if (cuentaAsociada instanceof CuentaCompartida) {
            pagador = comboPagador.getValue();
            if (pagador == null) {
                lblError.setText("Debes seleccionar quién pagó");
                return;
            }
        }

        // 2. CREAR O EDITAR EL OBJETO
        if (esEdicion) {
            gastoResultado.setConcepto(concepto);
            gastoResultado.setImporte(importe);
            gastoResultado.setFecha(fecha);
            gastoResultado.setCategoria(categoria);
            gastoResultado.setPagador(pagador);
        } else {
            // Al hacer esto, la hora se pone automáticamente a "AHORA MISMO" por defecto
            gastoResultado = new Gasto(concepto, importe, fecha, categoria, pagador);
        }
        
        gestorgastos.services.ServicioAlertas servicioAlertas = new gestorgastos.services.ServicioAlertas();
        servicioAlertas.comprobarAlertas(cuentaAsociada,gastoResultado);

        // --- IMPORTANTE: AQUÍ FORZAMOS LA HORA DE LOS SPINNERS ---
        // Leemos lo que el usuario puso en las ruedecitas
        int h = spinHora.getValue();
        int m = spinMinuto.getValue();
        
        // Se lo asignamos al gasto (sobrescribiendo la hora por defecto)
        gastoResultado.setHora(java.time.LocalTime.of(h, m));
        // ---------------------------------------------------------

        // Cerrar ventana
        cerrarVentana();
    }
    @FXML
    private void cancelar() {
        gastoResultado = null; // Indicamos que no se hizo nada
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtConcepto.getScene().getWindow();
        stage.close();
    }
}