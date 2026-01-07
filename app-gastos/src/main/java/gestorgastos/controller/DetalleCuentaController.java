package gestorgastos.controller;

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
	@FXML
	private Label lblTituloCuenta;
	@FXML
	private TableView<Gasto> tablaGastos;
	@FXML
	private TableColumn<Gasto, LocalDate> colFecha;
	@FXML
	private TableColumn<Gasto, String> colConcepto;
	@FXML
	private TableColumn<Gasto, String> colCategoria;
	@FXML
	private TableColumn<Gasto, Double> colImporte;
	@FXML
	private TableColumn<Gasto, String> colPagador;

	// --- UI: BOTONES DERECHA ---
	@FXML
	private Button btnAnadirGasto;
	@FXML
	private Button btnEditarGasto;
	@FXML
	private Button btnBorrarGasto;
	@FXML
	private Button btnImportar;

	// --- UI: PANEL SALDOS ---
	@FXML
	private javafx.scene.layout.VBox panelSaldos;
	@FXML
	private ListView<String> listaSaldos;

	// --- MODELO ---
	private Cuenta cuentaActual;
	private CuentaService cuentaService = CuentaService.getInstancia();

	@FXML
	public void initialize() {
		// Configurar los datos que van en cada columna
		colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
		colConcepto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getConcepto()));
		colImporte.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImporte()));
		colPagador.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPagador()));

		// El valor base de esta columna es el NOMBRE de la categoría
		colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria().getNombre()));

		// Configuración de las celdas
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

		// Configuración de los botones
		btnAnadirGasto.setOnAction(e -> abrirCrearGasto()); // Crear un gasto
		btnEditarGasto.setOnAction(e -> abrirEditarGasto()); // Editar un gasto
		btnBorrarGasto.setOnAction(e -> borrarGasto()); // Borrar un gasto
		btnImportar.setOnAction(e -> procesarImportacion()); // Importar gastoss
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuentaActual = cuenta;
		lblTituloCuenta.setText("Gastos: " + cuenta.getNombre());

		if (this.cuentaActual.getCategorias() == null) {
			this.cuentaActual.setCategorias(new ArrayList<>());
		}
		if (this.cuentaActual.getCategorias().isEmpty()) {
			this.cuentaActual.getCategorias().add(new Categoria("General", "Defecto", "#D3D3D3"));
		}

		actualizarTabla();

		if (cuenta instanceof CuentaCompartida) {
			panelSaldos.setVisible(true);
			calcularYMostrarSaldos((CuentaCompartida) cuenta);
		} else {
			panelSaldos.setVisible(false);
		}
	}

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
            
            if (controller.isGuardadoConfirmado()) {
                Gasto gastoQueVieneDelFormulario = controller.getGastoResultado();
                
                if (gastoEdicion == null) {
                    // CASO 1: NUEVO
                    System.out.println("DEBUG: Añadiendo gasto nuevo: " + gastoQueVieneDelFormulario.getConcepto());
                    cuentaActual.agregarGasto(gastoQueVieneDelFormulario);
                } else {
                    // CASO 2: EDICIÓN 
                    System.out.println("DEBUG: Editando gasto...");
                    System.out.println("   Original: " + gastoEdicion.getConcepto() + " - " + gastoEdicion.getImporte());
                    System.out.println("   Nuevo:    " + gastoQueVieneDelFormulario.getConcepto() + " - " + gastoQueVieneDelFormulario.getImporte());
                    
                    // Buscamos la posición del gasto original
                    int index = cuentaActual.getGastos().indexOf(gastoEdicion);
                    
                    if (index != -1) {

                        cuentaActual.getGastos().set(index, gastoQueVieneDelFormulario);
                        System.out.println("DEBUG: Reemplazo en lista realizado en índice: " + index);
                    } else {
                        System.out.println("ERROR CRÍTICO: No encuentro el gasto original en la lista para reemplazarlo.");
                    }
                }
                
                // Guardar y Refrescar
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
		cuentaService.agregarCuenta(null, cuentaActual);
		actualizarTabla();
		if (cuentaActual instanceof CuentaCompartida) {
			calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
		}
	}

	
	private void actualizarTabla() {
        // 1. Obtenemos una copia segura de los gastos actuales
        List<Gasto> listaGastos = new ArrayList<>();
        if (cuentaActual.getGastos() != null) {
            listaGastos.addAll(cuentaActual.getGastos());
        }
        
        // 2. Vaciamos la tabla de forma SEGURA (Lista vacía, NO null)
        tablaGastos.setItems(FXCollections.observableArrayList()); 
        
        // 3. Volvemos a meter los datos reales
        tablaGastos.setItems(FXCollections.observableArrayList(listaGastos));
        
        // 4. Refresco final
        tablaGastos.refresh(); 
    }

	private void calcularYMostrarSaldos(CuentaCompartida cuentaComp) {
		listaSaldos.getItems().clear();
		Map<String, Double> saldos = cuentaComp.calcularSaldos();
		saldos.forEach((persona, cantidad) -> {
			String texto = String.format("%s: %.2f €", persona, cantidad);
			listaSaldos.getItems().add(texto);
		});

		listaSaldos.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					setText(item);
					if (item.contains("-"))
						setStyle("-fx-text-fill: red;");
					else
						setStyle("-fx-text-fill: green;");
				}
			}
		});
	}

	private void mostrarAlerta(String mensaje) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	// Botones superiores

	@FXML
	private void volverInicio() {
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
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/gestorgastos/app_gastos/GestionCategoriasView.fxml"));
			Parent root = loader.load();

			GestionCategoriasController controller = loader.getController();
			controller.setCuenta(cuentaActual);

			Stage stage = (Stage) lblTituloCuenta.getScene().getWindow();
			stage.setScene(new Scene(root));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void irAVisualizacion() {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/gestorgastos/app_gastos/VisualizacionView.fxml"));
			Parent root = loader.load();

			VisualizacionController controller = loader.getController();
			controller.setCuenta(cuentaActual);

			Stage stage = (Stage) lblTituloCuenta.getScene().getWindow();

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/AlertaView.fxml"));
			Parent root = loader.load();

			AlertaController controller = loader.getController();
			controller.setCuenta(cuentaActual);

			Stage stage = new Stage();
			stage.setTitle("Configuración de Alertas - " + cuentaActual.getNombre());
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Error al abrir la ventana de alertas.").show();
		}
	}

	@FXML
	private void irACMD() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/TerminalView.fxml"));
			Parent root = loader.load();

			TerminalController controller = loader.getController();
			controller.setCuenta(cuentaActual);

			controller.setOnUpdate(() -> {
				actualizarTabla();
				if (cuentaActual instanceof CuentaCompartida) {
					calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
				}
			});

			Stage stage = new Stage();
			stage.setTitle("Terminal - " + cuentaActual.getNombre());
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			mostrarAlerta("Error al abrir la terminal.");
		}
	}

	// Método para iniciar la importación de gastos desde ficheros externos
	// Se importan también gastos de otras cuentas automáticamente
	@FXML
	private void procesarImportacion() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Importar Gastos");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Archivos Soportados", "*.csv", "*.txt", "*.json", "*.xlsx", "*.xml"));

		File fichero = fileChooser.showOpenDialog(btnImportar.getScene().getWindow());
		if (fichero == null)
			return;

		Importador importador = FactoriaImportacion.getImportador(fichero.getAbsolutePath());
		if (importador == null) {
			mostrarAlerta("Formato no soportado.");
			return;
		}

		try {

			List<GastoTemporal> temporales = importador.leerFichero(fichero.getAbsolutePath());

			Usuario user = gestorgastos.services.SesionService.getInstancia().getUsuarioActivo();

			List<Cuenta> cuentas = cuentaService.getCuentasDe(user);

			int insertados = 0;
			int descartados = 0;
			int alertasGeneradas = 0;

			for (GastoTemporal t : temporales) {
				System.out.println(t.toString());
				Optional<Cuenta> cMatch = cuentas.stream().filter(c -> c.getNombre().equalsIgnoreCase(t.nombreCuenta))
						.findFirst();

				if (cMatch.isEmpty()) {
					System.out.println("Descartado: No existe la cuenta: " + t.nombreCuenta + "\nGasto completo: " + t);
					descartados++;
					continue;
				}

				Cuenta destino = cMatch.get();

				if (destino.getAlertas() == null) {
					destino.setAlertas(new ArrayList<>());
				}

				if (destino.getNotificaciones() == null) {
					destino.setNotificaciones(new ArrayList<>());
				}

				boolean valido = false;

				// Comportamiento distinto según tipo de cuenta a la que pertenece el gasto
				if (destino instanceof CuentaPersonal) {
					valido = true;
				} else if (destino instanceof CuentaProporcional) {
					CuentaProporcional proporcional = (CuentaProporcional) destino;
					boolean esMiembro = proporcional.getMiembros().stream()
							.anyMatch(m -> m.equalsIgnoreCase(t.pagador));
					if (esMiembro) {
						valido = true;
					} else {
						System.out.println("Descartado (Especial): El usuario " + t.pagador
								+ " no está asociado a esta cuenta\nGasto completo: " + t);
					}
				} else if (destino instanceof CuentaCompartida) {
					CuentaCompartida compartida = (CuentaCompartida) destino;
					boolean esMiembro = compartida.getMiembros().stream().anyMatch(m -> m.equalsIgnoreCase(t.pagador));
					if (esMiembro) {
						valido = true;
					} else {
						System.out.println("Descartado (Compartida): El usuario " + t.pagador
								+ " no está asociado a esta cuenta\nGasto completo: " + t);
					}
				}

				if (valido) {

					Categoria real = destino.getCategorias().stream()
							.filter(c -> c.getNombre().equalsIgnoreCase(t.categoria)).findFirst()
							.orElse(destino.getCategorias().get(0));

					Gasto nuevo = new Gasto(t.concepto, t.importe, t.fecha, real, t.pagador);
					nuevo.setHora(t.hora);

					gestorgastos.services.ServicioAlertas servicio = new gestorgastos.services.ServicioAlertas();

					String errorAlerta = servicio.comprobarAlertas(destino, nuevo);

					if (errorAlerta != null) {
						System.out.println(
								"ALERTA SALTADA (" + destino.getNombre() + "): " + t.concepto + " -> " + errorAlerta);

						// Guardamos la notificación
						if (destino.getNombre().equalsIgnoreCase(cuentaActual.getNombre())) {
							cuentaActual.anadirNotificacion(errorAlerta);
						}
						cuentaService.agregarCuenta(user, destino);

						alertasGeneradas++; // Incrementamos el número de alertas generadas
					}

					destino.agregarGasto(nuevo);
					if (destino.getNombre().equalsIgnoreCase(cuentaActual.getNombre())) {
						cuentaActual.agregarGasto(nuevo);
					}

					insertados++;
				} else {
					descartados++;
				}
			}

			for (Cuenta c : cuentas) {
				cuentaService.agregarCuenta(null, c);
			}

			actualizarTabla();
			if (cuentaActual instanceof CuentaCompartida) {
				calcularYMostrarSaldos((CuentaCompartida) cuentaActual);
			}

			// Imprimir mensaje
			mostrarAlerta("Importación Finalizada\nInsertados: " + insertados + "\n(Con Alerta: " + alertasGeneradas
					+ ")\nDescartados: " + descartados);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarAlerta("Error al importar: " + e.getMessage());
		}
	}
}
