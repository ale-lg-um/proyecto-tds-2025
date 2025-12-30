package gestorgastos.controller;

//import gestorgastos.cli.GestorCLI;
import gestorgastos.model.*;
import gestorgastos.importacion.*;
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
import javafx.stage.FileChooser;

import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Optional;

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

    // --- GESTIÓN DE CONSOLA (CLI) ---
    //private GestorCLI gestorCLI;
    //private Thread hiloCLI;

    @FXML
    public void initialize() {
        // 1. Configurar QUÉ datos van en cada columna (ValueFactory)
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
        colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcepto()));
        colImporte.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImporte()));
        colPagador.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPagador()));
        
        // El valor base de esta columna es el NOMBRE de la categoría
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria().getNombre()));

        // --- Configurar CÓMO se ve la celda (CellFactory) para el COLOR ---
        colCategoria.setCellFactory(column -> new TableCell<Gasto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // Círculo de color
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Gasto gasto = (Gasto) getTableRow().getItem();
                        Categoria cat = gasto.getCategoria();
                        Circle circle = new Circle(8);
                        try {
                            circle.setFill(Color.web(cat.getColorHex()));
                            circle.setStroke(Color.DARKGRAY);
                        } catch (Exception e) {
                            circle.setFill(Color.LIGHTGRAY);
                        }
                        setGraphic(circle);
                    }
                }
            }
        });

        // 2. Configurar acciones de los botones
        btnAnadirGasto.setOnAction(e -> abrirCrearGasto());
        btnEditarGasto.setOnAction(e -> abrirEditarGasto());
        btnBorrarGasto.setOnAction(e -> borrarGasto());
        //btnImportar.setOnAction(e -> System.out.println("Funcionalidad Importar pendiente..."));
        btnImportar.setOnAction(e -> procesarImportacion());
    }

    /**
     * Recibe la cuenta desde el controlador anterior.
     */
    public void setCuenta(Cuenta cuenta) {
        this.cuentaActual = cuenta;
        lblTituloCuenta.setText("Gastos: " + cuenta.getNombre());

        // Aseguramos que la lista de categorías existe para evitar errores
        if (this.cuentaActual.getCategorias() == null) {
            this.cuentaActual.setCategorias(new ArrayList<>());
        }
        if (this.cuentaActual.getCategorias().isEmpty()) {
            this.cuentaActual.getCategorias().add(new Categoria("General", "Defecto", "#D3D3D3"));
        }

        actualizarTabla();

        // Gestión del Panel de Saldos
        if (cuenta instanceof CuentaCompartida) {
            panelSaldos.setVisible(true);
            calcularYMostrarSaldos((CuentaCompartida) cuenta);
        } else {
            panelSaldos.setVisible(false);
        }

        // --- INICIAR HILO DE CONSOLA (CLI) ---
       // iniciarCLI();
    }
    /*
    // --- MÉTODOS CLI ---
    private void iniciarCLI() {
        // Solo iniciamos si no está ya corriendo
        if (hiloCLI == null || !hiloCLI.isAlive()) {
            gestorCLI = new GestorCLI(cuentaActual);
            hiloCLI = new Thread(gestorCLI);
            hiloCLI.setDaemon(true); // Se cierra si cierras la app principal
            hiloCLI.start();
        }
    }

    private void detenerCLI() {
        if (gestorCLI != null) {
            gestorCLI.detener();
        }
    }
    // -------------------
    */

    // --- LÓGICA CRUD ---

    private void abrirCrearGasto() {
        abrirFormularioGasto(null);
    }

    private void abrirEditarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un gasto de la tabla para editarlo.");
            return;
        }
        abrirFormularioGasto(seleccionado);
    }

    private void abrirFormularioGasto(Gasto gastoEdicion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/FormularioGastoView.fxml"));
            Parent root = loader.load();

            FormularioGastoController controller = loader.getController();
            controller.initAttributes(cuentaActual, gastoEdicion);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(gastoEdicion == null ? "Nuevo Gasto" : "Editar Gasto");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // --- CAMBIO CLAVE AQUÍ ---
            
            // Antes tenías: if (controller.getGastoResultado() != null) {
            
            // Ahora ponemos esto:
            if (controller.isGuardadoConfirmado() && controller.getGastoResultado() != null) {
                
                if (gastoEdicion == null) {
                    // Solo añadimos si es nuevo. Si es edición, ya modificamos el objeto por referencia
                    cuentaActual.agregarGasto(controller.getGastoResultado());
                }
                
                // Guardamos en el JSON
                guardarCambiosYRefrescar();
            }
            // Si cierra con X o cancela, 'isGuardadoConfirmado' será false y no entrará aquí.

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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Borrar Gasto");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres borrar: " + seleccionado.getConcepto() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cuentaActual.eliminarGasto(seleccionado);
            guardarCambiosYRefrescar();
        }
    }

    private void guardarCambiosYRefrescar() {
        // 1. Guardar en JSON
        cuentaService.agregarCuenta(null, cuentaActual);
        // 2. Refrescar Tabla
        actualizarTabla();
        // 3. Recalcular Saldos si es compartida
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
        Map<String, Double> saldos = cuentaComp.calcularSaldos();
        saldos.forEach((persona, cantidad) -> {
            String texto = String.format("%s: %.2f €", persona, cantidad);
            listaSaldos.getItems().add(texto);
        });

        // Colores para deudores/acreedores
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
        // DETENER LA CONSOLA AL SALIR
        //detenerCLI();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/PrincipalView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestor de Gastos - Mis Cuentas");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) lblTituloCuenta.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irACategorias() {
        // DETENER LA CONSOLA AL CAMBIAR DE VISTA
        //detenerCLI();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/GestionCategoriasView.fxml"));
            Parent root = loader.load();
            
            GestionCategoriasController controller = loader.getController();
            controller.setCuenta(cuentaActual);

            Stage stage = (Stage) lblTituloCuenta.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    @FXML
    private void irAVisualizacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/VisualizacionView.fxml"));
            Parent root = loader.load();

            VisualizacionController controller = loader.getController();
            controller.setCuenta(cuentaActual); // ¡Pasamos la cuenta!

            Stage stage = (Stage) lblTituloCuenta.getScene().getWindow();
            
            // Hacemos la ventana más grande porque el calendario ocupa espacio
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
            // 1. Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/AlertaView.fxml"));
            Parent root = loader.load();

            // 2. Obtener el controlador y pasarle los datos
            AlertaController controller = loader.getController();
            controller.setCuenta(cuentaActual); // ¡Importante! Pasamos la cuenta actual

            // 3. Crear y mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Configuración de Alertas - " + cuentaActual.getNombre());
            stage.setScene(new Scene(root));
            
            // Usamos show() en vez de showAndWait() para que puedas ver las alertas 
            // y la tabla de gastos a la vez (muy útil para comprobar cosas).
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Opcional: mostrar una alerta visual de error
            new Alert(Alert.AlertType.ERROR, "Error al abrir la ventana de alertas.").show();
        }
    }
    /*
    @FXML private void irACMD() { System.out.println("La consola ya está activa en segundo plano (mira tu IDE)."); }
    */
    @FXML
    private void irACMD() {
        try {
            // 1. Cargar la vista de la Terminal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/TerminalView.fxml"));
            Parent root = loader.load();

            // 2. Pasar la cuenta al controlador de la terminal
            TerminalController controller = loader.getController();
            controller.setCuenta(cuentaActual);

            controller.setOnUpdate(() -> {
                actualizarTabla(); // Refresca la tabla de gastos
                
                // Si es compartida, refresca también los saldos
                if (cuentaActual instanceof CuentaCompartida) {
                    calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
                }
            });

            // 3. Abrir en una ventana nueva (Stage)
            Stage stage = new Stage();
            stage.setTitle("Terminal - " + cuentaActual.getNombre());
            stage.setScene(new Scene(root));
            
            // Opcional: Hacer que sea modal (no puedes tocar la ventana de atrás)
            // stage.initModality(Modality.APPLICATION_MODAL); 
            
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir la terminal.");
        }
    }
    
    /*private void procesarImportacion() {
    	// Abrir ventana explorador de archivos
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("ImportarGastos");
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivos Soportados", "*.csv", "*.txt"));
    	
    	File fichero = fileChooser.showOpenDialog(btnImportar.getScene().getWindow());
    	
    	if(fichero == null) return;
    	
    	// Obtener adaptador adecuado
    	
    	Importador importador = FactoriaImportacion.getImportador(fichero.getAbsolutePath());
    	
    	if(importador == null) {
    		mostrarAlerta("Formato de archivo no soportado");
    		return;
    	}
    	
    	try {
    		// Leer los datos con el adaptador
    		List<GastoTemporal> temporales = importador.leerFichero(fichero.getAbsolutePath());
    		
    		// Obtener nombres de todas las cuentas para buscar coincidencias
    		Usuario user = gestorgastos.services.SesionService.getInstancia().getUsuarioActivo();
    		List<Cuenta> cuentas = cuentaService.getCuentasDe(user);
    		
    		int insertados = 0;
    		int descartados = 0;
    		
    		for(GastoTemporal t : temporales) {
    			// Buscar cuenta por nombre
    			Optional<Cuenta> cuentaMatch = cuentas.stream()
    					.filter(c -> c.getNombre().equalsIgnoreCase(t.nombreCuenta))
    					.findFirst();
    			
    			if(cuentaMatch.isEmpty()) {
    				descartados++; // El usuario no posee una cuenta con ese nombre
    				continue;
    			}
    			
    			Cuenta cuentaDestino = cuentaMatch.get();
    			boolean valido = false;
    			
    			// Validar según el tipo de cuenta
    			if(cuentaDestino instanceof CuentaPersonal) {
    				valido = true; // Si la cuenta es personal, entra directo
    			} else if(cuentaDestino instanceof CuentaCompartida) {
    				CuentaCompartida compartida = (CuentaCompartida) cuentaDestino;
    				boolean esMiembro = compartida.getMiembros().stream()
    						.anyMatch(m -> m.equalsIgnoreCase(t.pagador));
    				
    				if(esMiembro) {
    					valido = true;
    				} else {
    					System.out.println("Descartado: " + t.pagador + " no pertenece a la cuenta " + compartida.getNombre());
    					descartados++;
    				}
    			}
    			
    			if(valido) {
    				Categoria real = cuentaDestino.getCategorias().stream()
    						.filter(c -> c.getNombre().equalsIgnoreCase(t.categoria))
    						.findFirst()
    						.orElse(cuentaDestino.getCategorias().get(0)); // Si la categoria del gasto no existe en la cuenta, el gasto se asigna a la categoria General
    				
    				Gasto nuevoGasto = new Gasto(t.concepto, t.importe, t.fecha, real, t.pagador);
    				nuevoGasto.setHora(t.hora);
    				
    				cuentaDestino.agregarGasto(nuevoGasto);
    				insertados++;
    			}
    		}
    		
    		// Guardar
    		for(Cuenta c: cuentas) {
    			cuentaService.agregarCuenta(null,  c);
    		}
    		
    		// Refrescar interfaz
    		actualizarTabla();
    		
    		if(cuentaActual instanceof CuentaCompartida) {
    			calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
    		}
    		
    		mostrarAlerta("Improtación finalizada.\nInsertados: " + insertados + "\nDescartados: " + descartados);
    	} catch (Exception e) {
    		e.printStackTrace();
    		mostrarAlerta("Error al importar fichero: " + e.getMessage());
    	}
    }*/
    
    private void procesarImportacion() {

    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Importar Gastos");
    	fileChooser.getExtensionFilters().addAll(
    			new FileChooser.ExtensionFilter("Archivos Soportados", "*.csv", "*.txt")
    	);
    	
    	File fichero = fileChooser.showOpenDialog(btnImportar.getScene().getWindow());
    	if(fichero == null) return;
    	
    	Importador importador = FactoriaImportacion.getImportador(fichero.getAbsolutePath());
    	if(importador == null) {
    		mostrarAlerta("Formato no soportado.");
    		return;
    	}
    	
    	try {

    		List<GastoTemporal> temporales = importador.leerFichero(fichero.getAbsolutePath());

    		Usuario user = gestorgastos.services.SesionService.getInstancia().getUsuarioActivo();

    		List<Cuenta> cuentas = cuentaService.getCuentasDe(user);

    		
    		int insertados = 0;
    		int descartados = 0;

			

    		
    		for(GastoTemporal t : temporales) {
    			
    			Optional<Cuenta> cMatch = cuentas.stream()
    					.filter(c -> c.getNombre().equalsIgnoreCase(t.nombreCuenta))
    					.findFirst();
    			
    			if(cMatch.isEmpty()) {
    				System.out.println("Descartado: No existe la cuenta: " + t.nombreCuenta + "\nGasto completo: " + t);
    				descartados++;
    				continue;
    			}
    			
    			Cuenta destino = cMatch.get();
    			boolean valido = false;
    			
    			// En caso de que la cuenta sea personal, insertamos directamente el gasto
    			if(destino instanceof CuentaPersonal) {
    				valido = true;
    			} else if(destino instanceof CuentaProporcional) {
    				CuentaProporcional proporcional = (CuentaProporcional) destino;
    				boolean esMiembro = proporcional.getMiembros().stream()
    						.anyMatch(m -> m.equalsIgnoreCase(t.pagador));
    				if(esMiembro) {
    					valido = true;
    				} else {
    					System.out.println("Descartado (Especial): El usuario " + t.pagador + " no está asociado a esta cuenta\nGasto completo: " + t);
    				}
    			} else if(destino instanceof CuentaCompartida) {
    				CuentaCompartida compartida = (CuentaCompartida) destino;
    				boolean esMiembro = compartida.getMiembros().stream()
    						.anyMatch(m -> m.equalsIgnoreCase(t.pagador));
    				if(esMiembro) {
    					valido = true;
    				} else {
    					System.out.println("Descartado (Compartida): El usuario " + t.pagador + " no está asociado a esta cuenta\nGasto completo: " + t);
    				}
    			}
    			
    			if(valido) {

    				Categoria real = destino.getCategorias().stream()
    						.filter(c -> c.getNombre().equalsIgnoreCase(t.categoria))
    						.findFirst()
    						.orElse(destino.getCategorias().get(0));

    				Gasto nuevo = new Gasto(t.concepto, t.importe, t.fecha, real, t.pagador);
    				nuevo.setHora(t.hora);
    				

    			    gestorgastos.services.ServicioAlertas servicio = new gestorgastos.services.ServicioAlertas();
    			    

    			    String errorAlerta = servicio.comprobarAlertas(destino, nuevo);

    			    if (errorAlerta != null) {
    			        System.out.println("Descartado por ALERTA (" + destino.getNombre() + "): " + t.concepto + " -> " + errorAlerta);
    			        cuentaService.agregarCuenta(user, destino);
    			        descartados++;
    			        continue; 
    			    }
    			    // ---------------------------------------------------------------
    				
    				destino.agregarGasto(nuevo);
    				if(destino.getNombre().equalsIgnoreCase(cuentaActual.getNombre())) {
    					cuentaActual.agregarGasto(nuevo);
    				}
    				
    				insertados++;
    			} else {
    				descartados++;
    			}
    		}
    		
    		for(Cuenta c : cuentas) {
    			cuentaService.agregarCuenta(null, c);
    		}
    		
    		actualizarTabla();
    		if(cuentaActual instanceof CuentaCompartida) {
    			calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
    		}
    		
    		mostrarAlerta("Importación Finalizada\nInsertados: " + insertados + "\nDescartados: " + descartados);
    	} catch (Exception e) {
    		e.printStackTrace();
    		mostrarAlerta("Error al importar: " + e.getMessage());
    	}
    }
}

