package gestorgastos.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import gestorgastos.model.Categoria;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.services.SesionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform; 
import javafx.scene.Node; 
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class VisualizacionController {

    // --- FILTROS ---
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private ListView<Month> listMeses;
    @FXML private ListView<Categoria> listCategorias;

    // --- GRÁFICOS ---
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    // --- CALENDARIO ---
    @FXML private StackPane contenedorCalendario;
    private CalendarView calendarView;
    private Calendar calendarioGastos;

    //private Cuenta cuentaActual;

    @FXML
    public void initialize() {
        // COnfigurar selección múltiple para los filtros
        listMeses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listCategorias.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Cargar meses
        listMeses.getItems().addAll(Month.values());
        
        Cuenta cuentaActual = SesionService.getInstancia().getCuentaActiva();
        listCategorias.getItems().setAll(cuentaActual.getCategorias());
        
        
        // Inicializar el calendario
        configurarCalendario();
        aplicarFiltros();
    }

    /*public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        // Cargar las categorías de esta cuenta en el filtro
        listCategorias.getItems().setAll(cuenta.getCategorias());
        
        // Mostrar todo por defecto al entrar
        aplicarFiltros();
    }*/

    private void configuringCalendario() {
        calendarView = new CalendarView();
        
        // Deshabilitar vistas que no necesitamos para que sea más simple
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageSwitcher(false);
        
        // Crear nuestro "Calendario" de datos
        calendarioGastos = new Calendar("Mis Gastos");
        calendarioGastos.setStyle(Style.STYLE1); // Color por defecto (Rojo/Rosa)
        
        CalendarSource fuente = new CalendarSource("Datos");
        fuente.getCalendars().add(calendarioGastos);
        
        calendarView.getCalendarSources().setAll(fuente);
        
        // Añadir a la pantalla
        contenedorCalendario.getChildren().add(calendarView);
        
        // Forzar vista de Día o Mes
        calendarView.showDayPage(); 
    }

    @FXML
    private void aplicarFiltros() {
    	Cuenta cuentaActual = SesionService.getInstancia().getCuentaActiva();
    	if (cuentaActual == null) return;

        List<Gasto> todos = cuentaActual.getGastos();
        
        List<Gasto> filtrados = todos.stream()
            // Filtrar por rango de fechas
            .filter(g -> {
                if (dateDesde.getValue() != null && g.getFecha().isBefore(dateDesde.getValue())) return false;
                if (dateHasta.getValue() != null && g.getFecha().isAfter(dateHasta.getValue())) return false;
                return true;
            })
            // Filtrar por meses seleccionados
            .filter(g -> {
                List<Month> mesesSel = listMeses.getSelectionModel().getSelectedItems();
                if (mesesSel.isEmpty()) return true; // Si no hay nada seleccionado, mostrar todo
                return mesesSel.contains(g.getFecha().getMonth());
            })
            // Filtrar por categorías seleccionadas
            .filter(g -> {
                List<Categoria> catsSel = listCategorias.getSelectionModel().getSelectedItems();
                if (catsSel.isEmpty()) return true;
                // Comparamos por nombre para asegurar
                return catsSel.stream().anyMatch(c -> c.getNombre().equals(g.getCategoria().getNombre()));
            })
            .collect(Collectors.toList());

        // Actualizar gráficos y calendario
        actualizarGraficos(filtrados);
        actualizarCalendario(filtrados);
    }

    @FXML
    private void limpiarFiltros() {
        dateDesde.setValue(null);
        dateHasta.setValue(null);
        listMeses.getSelectionModel().clearSelection();
        listCategorias.getSelectionModel().clearSelection();
        aplicarFiltros(); // Refrescar para mostrar todo
    }

 // Asegúrate de tener este import arriba:
    // import javafx.application.Platform; 
    // import javafx.scene.Node; 
    // import javafx.scene.Parent;

    private void actualizarGraficos(List<Gasto> gastos) {
    	Cuenta cuentaActual = SesionService.getInstancia().getCuentaActiva();
    	// 1. Desactivamos animaciones
        pieChart.setAnimated(false);
        barChart.setAnimated(false);

        // 2. Agrupamos los gastos por NOMBRE DE CATEGORÍA
        Map<String, Double> porCategoria = gastos.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getCategoria().getNombre(),
                        Collectors.summingDouble(Gasto::getImporte)
                ));

        // 3. Mapa de Colores
        Map<String, String> mapaColores = cuentaActual.getCategorias().stream()
                .collect(Collectors.toMap(Categoria::getNombre, Categoria::getColorHex, (a, b) -> a));

        // --- A. GRÁFICO CIRCULAR (PieChart) ---
        pieChart.getData().clear();
        porCategoria.forEach((catName, total) -> {
            PieChart.Data data = new PieChart.Data(catName, total);
            pieChart.getData().add(data);
            
            String color = mapaColores.getOrDefault(catName, "#808080");
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        });

        // --- B. GRÁFICO DE BARRAS (BarChart) ---
        barChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Gastos por Categoría");

        porCategoria.forEach((catName, total) -> {
            series.getData().add(new XYChart.Data<>(catName, total));
        });

        barChart.getData().add(series);

        // Pintamos las barras manualmente
        for (XYChart.Data<String, Number> data : series.getData()) {
            javafx.scene.Node barra = data.getNode();
            if (barra != null) {
                String catName = data.getXValue();
                String color = mapaColores.getOrDefault(catName, "#808080");
                barra.setStyle("-fx-bar-fill: " + color + ";");
            }
        }

        // --- C. ARREGLAR LA LEYENDA DEL PIECHART ---
        Platform.runLater(() -> {
            Node legend = pieChart.lookup(".chart-legend");
            
            if (legend != null && legend instanceof Parent) {
                for (Node item : ((Parent) legend).getChildrenUnmodifiable()) {
                    if (item instanceof Label) {
                        Label label = (Label) item;
                        String catName = label.getText(); 
                        
                        if (mapaColores.containsKey(catName)) {
                            String color = mapaColores.get(catName);
                            
                            if (label.getGraphic() != null) {
                                label.getGraphic().setStyle("-fx-background-color: " + color + ";");
                            }
                        }
                    }
                }
            }
        });
    }

    private void actualizarCalendario(List<Gasto> gastos) {
        calendarioGastos.clear(); // Borrar entradas anteriores
        
        for (Gasto g : gastos) {
            // Crear una entrada en el calendario
            Entry<String> entry = new Entry<>(g.getConcepto() + " (" + g.getImporte() + "€)");
            
            entry.setInterval(g.getFecha(), g.getHora(), g.getFecha(), g.getHora().plusHours(1));
            
            calendarioGastos.addEntry(entry);
        }
        
        // Ir a la fecha del último gasto si hay alguno, para no ver el calendario vacío
        if (!gastos.isEmpty()) {
            calendarView.setDate(gastos.get(gastos.size() - 1).getFecha());
        }
    }
    
    // Configurar método inicial para corregir nombre
    private void configurarCalendario() {
        configuringCalendario();
    }

    @FXML
    private void volver() {
        try {
        	Cuenta cuentaActual = SesionService.getInstancia().getCuentaActiva();
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/DetalleCuentaView.fxml"));
            Parent root = loader.load();

            DetalleCuentaController controller = loader.getController();
            controller.setCuenta(cuentaActual);

            Stage stage = (Stage) dateDesde.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}