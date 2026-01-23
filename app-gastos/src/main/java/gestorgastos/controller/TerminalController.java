package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class TerminalController {

    @FXML private TextArea txtOutput;
    @FXML private TextField txtInput;

    private Cuenta cuentaActiva;
    private CuentaService cuentaService = CuentaService.getInstancia();
    private Runnable onUpdateAction;
    private java.time.LocalTime tempHora;

    // MÁQUINA DE ESTADOS
    // 0=Cmd, 1=Concepto, 2=Importe, 3=Fecha, 4=Categoría, 5=Pagador
    private int pasoActual = 0; 
    
    // Variables temporales
    private String tempConcepto;
    private double tempImporte;
    private LocalDate tempFecha;
    private String tempCategoria;

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActiva = cuenta;
        imprimir("========================================");
        imprimir(" CONSOLA INTEGRADA - " + cuenta.getNombre());
        imprimir(" Comandos: registrar, borrar, listar, ayuda");
        imprimir("========================================");
        imprimir("\nCMD> ");
    }

    public void setOnUpdate(Runnable action) {
        this.onUpdateAction = action;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> txtInput.requestFocus());
    }

    @FXML
    private void procesarEntrada() {
        String texto = txtInput.getText().trim();
        txtInput.clear();
        txtOutput.appendText(texto + "\n"); // Eco

        if (pasoActual == 0) {
            ejecutarComandoPrincipal(texto.toLowerCase());
        } else {
            procesarPasoRegistro(texto);
        }
    }

    private void ejecutarComandoPrincipal(String comando) {
        switch (comando) {
            case "ayuda":
                imprimir("listar    -> Ver gastos");
                imprimir("registrar -> Añadir nuevo gasto");
                imprimir("borrar    -> Eliminar gasto por ID");
                imprimir("clear     -> Limpiar pantalla");
                reiniciarPrompt();
                break;
            case "listar":
                listarGastos();
                reiniciarPrompt();
                break;
            case "registrar":
                pasoActual = 1;
                imprimir("--- Nuevo Gasto ---");
                imprimir("Introduce el Concepto:");
                break;
            case "borrar":
                listarGastos();
                pasoActual = 10;
                imprimir("Introduce el ID del gasto a borrar:");
                break;
            case "clear":
                txtOutput.clear();
                reiniciarPrompt();
                break;
            default:
                imprimir("Comando no reconocido.");
                reiniciarPrompt();
        }
    }

    private void procesarPasoRegistro(String entrada) {
        try {
            switch (pasoActual) {
                case 1:
                     if(entrada.isEmpty()) throw new Exception("El concepto no puede estar vacío.");
                     tempConcepto = entrada;
                     pasoActual = 2;
                     imprimir("Introduce el Importe:");
                     break;

                case 2: 
                     tempImporte = Double.parseDouble(entrada.replace(",", "."));
                     pasoActual = 3;
                     imprimir("Fecha (AAAA-MM-DD) [Enter para Hoy]:");
                     break;

                case 3:
                    if (entrada.isEmpty()) {
                        tempFecha = LocalDate.now();
                    } else {
                        try {
                            tempFecha = LocalDate.parse(entrada);
                        } catch (DateTimeParseException dtpe) {
                            throw new Exception("Formato incorrecto. Usa AAAA-MM-DD");
                        }
                    }

                    pasoActual = 31; 
                    imprimir("Hora (HH:mm) [Enter para Ahora]:");
                    break;

                case 31:
                    if (entrada.isEmpty()) {
                        tempHora = java.time.LocalTime.now();
                    } else {
                        try {
                            tempHora = java.time.LocalTime.parse(entrada);
                        } catch (DateTimeParseException e) {
                            throw new Exception("Formato incorrecto. Usa HH:mm (ej: 14:30)");
                        }
                    }
                    pasoActual = 4;
                    imprimir("Categoría (o Enter para General):");
                    break;
         
                case 4: 
                    String catNombre = entrada;
                    Categoria cat = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(catNombre))
                            .findFirst()
                            .orElse(cuentaActiva.getCategorias().get(0));
                    
                    tempCategoria = cat.getNombre();

                    if (cuentaActiva instanceof CuentaCompartida) {
                        pasoActual = 5; 
                        imprimir("Pagador (Nombre):");
                    } else {
                        guardarGasto(cat, "Yo");
                    }
                    break;

                case 5:
                    Categoria catFinal = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(tempCategoria))
                            .findFirst().orElse(cuentaActiva.getCategorias().get(0));
                    guardarGasto(catFinal, entrada);
                    break;
                    
                case 10:
                    int id = Integer.parseInt(entrada);
                    if (id >= 0 && id < cuentaActiva.getGastos().size()) {
                         Gasto g = cuentaActiva.getGastos().remove(id);
                         guardarCambios();
                         imprimir("✓ Borrado: " + g.getConcepto());
                    } else {
                         imprimir("(!) ID inválido.");
                    }
                    pasoActual = 0;
                    reiniciarPrompt();
                    break;
            }

        } catch (NumberFormatException e) {
            imprimir("(!) Error: Número inválido.");
        } catch (Exception e) {
            imprimir("(!) Error: " + e.getMessage());
            
            pasoActual = 0; 
            reiniciarPrompt();
        }
    }

    private void guardarGasto(Categoria cat, String pagador) {
    // Crear el gasto
    Gasto nuevo = new Gasto(tempConcepto, tempImporte, tempFecha, cat, pagador);
    
    // Editar la hora
    nuevo.setHora(tempHora);

    // Comprobar alertas
    gestorgastos.services.ServicioAlertas servicio = new gestorgastos.services.ServicioAlertas();
    
    // Guardamos el resultado. Si hay alerta, el servicio ya creó la notificación internamente.
    //String mensajeError = servicio.comprobarAlertas(cuentaActiva, nuevo); 

    // Si mensajeError tiene texto, significa que nos hemos pasado del límite de la alerta
    
    // Corrección tercer PR
    Alerta alertaSaltada = servicio.comprobarAlertas(cuentaActiva, nuevo);
    if (alertaSaltada != null) {
    	String mensaje = "Has superado el límite de " + alertaSaltada.getLimite() + "€ definido en tu alerta.";
        // Como estamos en CMD, no puede aparecer una ventana con el mensaje, así que el mensaje se imprime por la terminal
        imprimir("\n**************************************************");
        imprimir("⚠️  GASTO BLOQUEADO POR ALERTA");
        imprimir("--------------------------------------------------");
        imprimir(mensaje);
        imprimir("--------------------------------------------------");
        imprimir("ℹ️  Se ha generado una notificación en tu cuenta.");
        imprimir("    El gasto SI se ha guardado.");
        imprimir("**************************************************\n");

        // Reseteamos el flujo para volver al menú principal
        guardarCambios();
        pasoActual = 0;
        reiniciarPrompt();
        
    }

    // Guardamos el gasto
    cuentaActiva.agregarGasto(nuevo);
    guardarCambios();
    
    imprimir("✓ Gasto guardado (" + tempFecha + " " + tempHora.toString().substring(0,5) + ").");
    pasoActual = 0;
    reiniciarPrompt();
}
    
    private void guardarCambios() {
        cuentaService.agregarCuenta(cuentaActiva);
        if (onUpdateAction != null) onUpdateAction.run();
    }

    // Listamos los gastos
    private void listarGastos() {
        if (cuentaActiva.getGastos().isEmpty()) {
            imprimir("(Sin gastos)");
            return;
        }
        
        String formato = "%-5s %-18s %-20s %-10s %-15s";
        
        imprimir(String.format(formato, "ID", "FECHA Y HORA", "CONCEPTO", "IMPORTE", "CATEGORIA"));
        imprimir("-----------------------------------------------------------------------");

        for (int i = 0; i < cuentaActiva.getGastos().size(); i++) {
            Gasto g = cuentaActiva.getGastos().get(i);
            
            // Combinamos fecha y hora para verlo bonito
            String fechaHoraStr = g.getFecha().toString();
            if (g.getHora() != null) {
                // Cortamos los segundos para que quede más limpio (HH:mm)
                String horaSimple = g.getHora().toString();
                if(horaSimple.length() > 5) horaSimple = horaSimple.substring(0, 5);
                
                fechaHoraStr += " " + horaSimple;
            }

            imprimir(String.format(formato, 
                "[" + i + "]", 
                fechaHoraStr,
                g.getConcepto(), 
                String.format("%.2f€", g.getImporte()), 
                g.getCategoria().getNombre()));
        }
    }

    private void imprimir(String texto) {
        txtOutput.appendText(texto + "\n");
    }

    private void reiniciarPrompt() {
        txtOutput.appendText("\nCMD> ");
    }
}
