package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import gestorgastos.services.SesionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback; // IMPORTANTE
import java.time.LocalDate; // IMPORTANTE
import java.time.format.DateTimeFormatter; // IMPORTANTE
import java.time.temporal.TemporalAdjusters; // IMPORTANTE
import java.time.DayOfWeek; // IMPORTANTE

import java.util.ArrayList;

public class AlertaController {
    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private TextField txtLimite;
    @FXML private ListView<Alerta> listaAlertasConfiguradas;

    @FXML private ListView<Notificacion> listaHistorial;

    private Cuenta cuentaActual;
    private CuentaService cuentaService = CuentaService.getInstancia();
    
    // Formateador para que las fechas se vean bonitas (ej: 06/01/2026)
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*public void setCuenta(Cuenta cuenta){
        this.cuentaActual = cuenta;
        if(cuenta.getAlertas() == null) cuenta.setAlertas(new ArrayList<>());
        if(cuenta.getNotificaciones() == null) cuenta.setNotificaciones(new ArrayList<>());
        cargarDatos();
    }*/

    @FXML
    public void initialize() {
        this.cuentaActual = SesionService.getInstancia().getCuentaActiva();
    	comboTipo.setItems(FXCollections.observableArrayList("SEMANAL","MENSUAL"));
        comboTipo.getSelectionModel().selectFirst();

        // Personalizamos cómo se ve cada celda de la lista de alertas
        listaAlertasConfiguradas.setCellFactory(new Callback<ListView<Alerta>, ListCell<Alerta>>() {
            @Override
            public ListCell<Alerta> call(ListView<Alerta> param) {
                return new ListCell<Alerta>() {
                    @Override
                    protected void updateItem(Alerta item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        } else {
                            // Cálculo de la fecha de hoy
                            LocalDate hoy = LocalDate.now();
                            String rangoFechas = "";

                            // Dos timpos de alertas: semanales y mensuales
                            if ("SEMANAL".equalsIgnoreCase(item.getTipo())) {
                                LocalDate inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                                LocalDate fin = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                                rangoFechas = "(" + inicio.format(fmt) + " al " + fin.format(fmt) + ")";
                            } 
                            else if ("MENSUAL".equalsIgnoreCase(item.getTipo())) {
                                LocalDate inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
                                LocalDate fin = hoy.with(TemporalAdjusters.lastDayOfMonth());
                                rangoFechas = "(" + inicio.format(fmt) + " al " + fin.format(fmt) + ")";
                            }

                            // Obtenemos el nombre de la categoría. Si no tiene, se asigna a la categoría General
                            String nombreCat = (item.getCategoria() != null) ? item.getCategoria().getNombre() : "General";

                            // Formateo del texto
                            // Ejemplo: SEMANAL (01/01/2026 al 07/01/2026) -> 100.0€ (Comida)
                            setText(item.getTipo() + " " + rangoFechas + " -> " + item.getLimite() + "€ (" + nombreCat + ")");
                        }
                    }
                };
            }
        });
        
        cargarDatos();
    }
    
    @FXML
    public void cargarDatos() {
    	if(cuentaActual != null){
            comboCategoria.getItems().clear();
            // Si no se selecciona categoria, es una alerta sin categoria

            comboCategoria.getItems().addAll(cuentaActual.getCategorias());
            listaAlertasConfiguradas.setItems(FXCollections.observableArrayList(cuentaActual.getAlertas()));
            listaHistorial.setItems((FXCollections.observableArrayList(cuentaActual.getNotificaciones())));
        }
    }
    
    @FXML
    public void guardarAlertas() {
    	try {
    		String tipoAlerta = comboTipo.getValue();
    		double limite = Double.parseDouble(txtLimite.getText().replace(",", "."));
    		Categoria categoria = comboCategoria.getValue();
    		Alerta alerta = new Alerta(tipoAlerta, limite, categoria);
    		cuentaActual.agregarAlerta(alerta);
    		cuentaService.agregarCuenta(cuentaActual);
    		this.cargarDatos();
    		this.txtLimite.clear();
    		this.comboCategoria.getSelectionModel().clearSelection();
    	} catch (NumberFormatException e) {
    		System.err.println("Error en el formato numérico del campo límite");
    	}
    }
    
    @FXML
    public void borrarAlertas() {
    	Alerta alertaBorrar;
    	alertaBorrar = listaAlertasConfiguradas.getSelectionModel().getSelectedItem();
    	if(alertaBorrar != null) {
    		cuentaActual.getAlertas().remove(alertaBorrar);
    		cuentaService.agregarCuenta(cuentaActual);
    		this.cargarDatos();
    	}
    }
    
    @FXML
    public void limpiarHistorial() {
    	cuentaActual.getNotificaciones().clear();
    	cuentaService.agregarCuenta(cuentaActual);
    	this.cargarDatos();
    }
}
