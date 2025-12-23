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
    private java.time.LocalTime tempHora; // <--- NUEVA

    // MÁQUINA DE ESTADOS
    // 0=Cmd, 1=Concepto, 2=Importe, 3=Fecha, 4=Categoría, 5=Pagador
    private int pasoActual = 0; 
    
    // Variables temporales
    private String tempConcepto;
    private double tempImporte;
    private LocalDate tempFecha; // <--- NUEVA VARIABLE
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
                // ... (Case 1 y 2 siguen igual) ...
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

                // --- MODIFICADO: PASO FECHA ---
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
                    // ANTES IBA AL 4. AHORA VA AL 31 (Hora)
                    pasoActual = 31; 
                    imprimir("Hora (HH:mm) [Enter para Ahora]:");
                    break;

                // --- NUEVO: PASO HORA ---
                case 31:
                    if (entrada.isEmpty()) {
                        // Si pulsa Enter, usamos la hora actual
                        tempHora = java.time.LocalTime.now();
                    } else {
                        try {
                            // Intentamos leer la hora (ej: 14:30)
                            tempHora = java.time.LocalTime.parse(entrada);
                        } catch (DateTimeParseException e) {
                            throw new Exception("Formato incorrecto. Usa HH:mm (ej: 14:30)");
                        }
                    }
                    pasoActual = 4; // Ahora sí vamos a Categoría
                    imprimir("Categoría (o Enter para General):");
                    break;
                // ------------------------

                // --- PASO 4: CATEGORÍA ---
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

                // ... (Case 5 y Case 10 siguen igual) ...
                case 5:
                    Categoria catFinal = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(tempCategoria))
                            .findFirst().orElse(cuentaActiva.getCategorias().get(0));
                    guardarGasto(catFinal, entrada);
                    break;
                    
                case 10:
                    // ... (tu código de borrar) ...
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
            // IMPORTANTE: Si falla la hora, podríamos querer volver a pedirla en vez de reiniciar todo.
            // Pero reiniciar es más seguro para evitar bucles.
            pasoActual = 0; 
            reiniciarPrompt();
        }
    }

    private void guardarGasto(Categoria cat, String pagador) {
    // 1. Creamos el gasto
    Gasto nuevo = new Gasto(tempConcepto, tempImporte, tempFecha, cat, pagador);
    
    // 2. SOBRESCRIBIMOS CON LA HORA QUE ELIGIÓ EL USUARIO
    nuevo.setHora(tempHora);

    // ---------------------------------------------------------
    // 3. COMPROBACIÓN DE ALERTAS (BLOQUEANTE)
    // ---------------------------------------------------------
    gestorgastos.services.ServicioAlertas servicio = new gestorgastos.services.ServicioAlertas();
    
    // Guardamos el resultado. Si hay alerta, el servicio ya creó la notificación internamente.
    String mensajeError = servicio.comprobarAlertas(cuentaActiva, nuevo); 

    // Si mensajeError tiene texto, significa que nos hemos pasado
    if (mensajeError != null) {
        // Mostramos el "Pop-up" versión texto
        imprimir("\n**************************************************");
        imprimir("⚠️  GASTO BLOQUEADO POR ALERTA");
        imprimir("--------------------------------------------------");
        imprimir(mensajeError);
        imprimir("--------------------------------------------------");
        imprimir("ℹ️  Se ha generado una notificación en tu cuenta.");
        imprimir("    El gasto NO se ha guardado.");
        imprimir("**************************************************\n");

        // Reseteamos el flujo para volver al menú principal
        guardarCambios();
        pasoActual = 0;
        reiniciarPrompt();
        
        // IMPORTANTE: Return para que NO ejecute el código de abajo (agregarGasto)
        return; 
    }

    // ---------------------------------------------------------
    // 4. GUARDAR EN LA CUENTA (Solo llegamos aquí si mensajeError fue null)
    // ---------------------------------------------------------
    cuentaActiva.agregarGasto(nuevo);
    guardarCambios();
    
    imprimir("✓ Gasto guardado (" + tempFecha + " " + tempHora.toString().substring(0,5) + ").");
    pasoActual = 0;
    reiniciarPrompt();
}
    
    private void guardarCambios() {
        cuentaService.agregarCuenta(null, cuentaActiva);
        if (onUpdateAction != null) onUpdateAction.run();
    }

    private void listarGastos() {
        if (cuentaActiva.getGastos().isEmpty()) {
            imprimir("(Sin gastos)");
            return;
        }
        
        // Hacemos la columna de fecha un poco más ancha para que quepa la hora
        // Antes era %-12s, ahora %-18s
        String formato = "%-5s %-18s %-20s %-10s %-15s";
        
        imprimir(String.format(formato, "ID", "FECHA Y HORA", "CONCEPTO", "IMPORTE", "CATEGORIA"));
        imprimir("-----------------------------------------------------------------------");

        for (int i = 0; i < cuentaActiva.getGastos().size(); i++) {
            Gasto g = cuentaActiva.getGastos().get(i);
            
            // Combinamos fecha y hora para verlo bonito
            // Ejemplo: "2023-12-20 14:30"
            String fechaHoraStr = g.getFecha().toString();
            if (g.getHora() != null) {
                // Cortamos los segundos para que quede más limpio (HH:mm)
                String horaSimple = g.getHora().toString();
                if(horaSimple.length() > 5) horaSimple = horaSimple.substring(0, 5);
                
                fechaHoraStr += " " + horaSimple;
            }

            imprimir(String.format(formato, 
                "[" + i + "]", 
                fechaHoraStr, // <--- Usamos la variable combinada
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
