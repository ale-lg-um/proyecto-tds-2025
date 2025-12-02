package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    private Gasto gastoResultado;
    private boolean esEdicion = false;
    private Cuenta cuentaAsociada; // Necesario para saber los miembros

    @FXML
    public void initialize() {
        // ANTES: Tenías esto que creaba una lista falsa solo para esta ventana
        // comboCategoria.getItems().add(new Categoria("General"));

        // AHORA: Conectamos con la lista REAL del servicio (donde se guardan las nuevas)
        comboCategoria.setItems(FXCollections.observableArrayList(
            CuentaService.getInstancia().getCategorias()
        ));

        // Seleccionamos la primera por defecto para que no salga vacío
        if (!comboCategoria.getItems().isEmpty()) {
            comboCategoria.getSelectionModel().selectFirst();
        }
        
        dateFecha.setValue(LocalDate.now());
    }

    // Método para configurar la ventana según si es CREAR o EDITAR
    public void initAttributes(Cuenta cuenta, Gasto gastoAEditar) {
        this.cuentaAsociada = cuenta;

        // 1. Configurar Visibilidad del Pagador (Lógica Compartida vs Personal)
        if (cuenta instanceof CuentaCompartida) {
            panelPagador.setVisible(true);
            panelPagador.setManaged(true);
            // Cargamos los miembros en el combo
            comboPagador.setItems(FXCollections.observableArrayList(((CuentaCompartida) cuenta).getMiembros()));
        } else {
            // Si es personal, ocultamos el selector
            panelPagador.setVisible(false);
            panelPagador.setManaged(false); 
        }

        // 2. Rellenar datos si es EDICIÓN
        if (gastoAEditar != null) {
            this.esEdicion = true;
            this.gastoResultado = gastoAEditar; // Referencia al objeto original
            lblTitulo.setText("Editar Gasto");
            
            txtConcepto.setText(gastoAEditar.getConcepto());
            txtImporte.setText(String.valueOf(gastoAEditar.getImporte()));
            dateFecha.setValue(gastoAEditar.getFecha());
            comboCategoria.setValue(gastoAEditar.getCategoria());
            
            if (cuenta instanceof CuentaCompartida) {
                comboPagador.setValue(gastoAEditar.getPagador());
            }
        } else {
            lblTitulo.setText("Nuevo Gasto");
            // Si es compartido, seleccionamos al primer miembro por defecto para evitar nulos
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
        // Validaciones básicas
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
        
        // Determinar quién paga
        String pagador;
        if (cuentaAsociada instanceof CuentaCompartida) {
            pagador = comboPagador.getValue();
            if (pagador == null) {
                lblError.setText("Debes seleccionar quién pagó");
                return;
            }
        } else {
            pagador = "Yo"; // Valor por defecto para cuentas personales
        }

        // Crear o Actualizar el objeto
        if (esEdicion) {
            // Actualizamos el objeto existente
            gastoResultado.setConcepto(concepto);
            gastoResultado.setImporte(importe);
            gastoResultado.setFecha(fecha);
            gastoResultado.setCategoria(categoria);
            gastoResultado.setPagador(pagador);
        } else {
            // Creamos uno nuevo
            gastoResultado = new Gasto(concepto, importe, fecha, categoria, pagador);
        }

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