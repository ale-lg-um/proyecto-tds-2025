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
                // --- PASO 1: CONCEPTO ---
                case 1: 
                    if(entrada.isEmpty()) throw new Exception("El concepto no puede estar vacío.");
                    tempConcepto = entrada;
                    pasoActual = 2;
                    imprimir("Introduce el Importe:");
                    break;

                // --- PASO 2: IMPORTE ---
                case 2: 
                    tempImporte = Double.parseDouble(entrada.replace(",", "."));
                    pasoActual = 3; // AHORA VAMOS AL PASO DE FECHA
                    imprimir("Fecha (AAAA-MM-DD) [Enter para Hoy]:");
                    break;

                // --- PASO 3: FECHA (NUEVO) ---
                case 3:
                    if (entrada.isEmpty()) {
                        // Si pulsa Enter vacío -> Hoy
                        tempFecha = LocalDate.now();
                    } else {
                        // Intentamos leer la fecha escrita
                        try {
                            tempFecha = LocalDate.parse(entrada);
                        } catch (DateTimeParseException dtpe) {
                            throw new Exception("Formato incorrecto. Usa AAAA-MM-DD (ej: 2025-12-31)");
                        }
                    }
                    pasoActual = 4; // Pasamos a Categoría
                    imprimir("Categoría (o Enter para General):");
                    break;

                // --- PASO 4: CATEGORÍA (Antes era el 3) ---
                case 4: 
                    String catNombre = entrada;
                    Categoria cat = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(catNombre))
                            .findFirst()
                            .orElse(cuentaActiva.getCategorias().get(0));
                    
                    tempCategoria = cat.getNombre();

                    if (cuentaActiva instanceof CuentaCompartida) {
                        pasoActual = 5; // Vamos a Pagador
                        imprimir("Pagador (Nombre):");
                    } else {
                        guardarGasto(cat, "Yo");
                    }
                    break;

                // --- PASO 5: PAGADOR (Antes era el 4) ---
                case 5: 
                    Categoria catFinal = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(tempCategoria))
                            .findFirst().orElse(cuentaActiva.getCategorias().get(0));
                    
                    guardarGasto(catFinal, entrada);
                    break;

                // --- CASO BORRAR (ID 10) ---
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
            pasoActual = 0; // Cancelar si hay error grave
            reiniciarPrompt();
        }
    }

    private void guardarGasto(Categoria cat, String pagador) {
        // AHORA USAMOS 'tempFecha' EN LUGAR DE LocalDate.now()
        Gasto nuevo = new Gasto(tempConcepto, tempImporte, tempFecha, cat, pagador);
        
        cuentaActiva.agregarGasto(nuevo);
        guardarCambios();
        
        imprimir("✓ Gasto guardado (" + tempFecha + ").");
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
        
        // Añadimos la columna FECHA al listado
        String formato = "%-5s %-12s %-20s %-10s %-15s";
        imprimir(String.format(formato, "ID", "FECHA", "CONCEPTO", "IMPORTE", "CATEGORIA"));
        imprimir("----------------------------------------------------------------");

        for (int i = 0; i < cuentaActiva.getGastos().size(); i++) {
            Gasto g = cuentaActiva.getGastos().get(i);
            imprimir(String.format(formato, 
                "[" + i + "]", 
                g.getFecha().toString(), 
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
/*
package gestorgastos.controller;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class TerminalController {

    @FXML private TextArea txtOutput;
    @FXML private TextField txtInput;

    private Runnable onUpdateAction;

    private Cuenta cuentaActiva;
    private CuentaService cuentaService = CuentaService.getInstancia();

    // MÁQUINA DE ESTADOS
    // 0 = Esperando comando, 1 = Esperando Concepto, 2 = Esperando Importe, etc.
    private int pasoActual = 0; 
    
    // Variables temporales para guardar lo que el usuario va escribiendo paso a paso
    private String tempConcepto;
    private double tempImporte;
    private String tempCategoria;

    public void setCuenta(Cuenta cuenta) {
        this.cuentaActiva = cuenta;
        imprimir("========================================");
        imprimir(" CONSOLA INTEGRADA - " + cuenta.getNombre());
        imprimir(" Comandos: registrar, borrar, listar, ayuda");
        imprimir("========================================");
        imprimir("\nCMD> ");
    }

    @FXML
    public void initialize() {
        // Hacemos que el cursor siempre esté en el input al empezar
        Platform.runLater(() -> txtInput.requestFocus());
    }

    @FXML
    private void procesarEntrada() {
        String texto = txtInput.getText().trim();
        txtInput.clear();

        // 1. Mostrar lo que el usuario acaba de escribir (Eco)
        txtOutput.appendText(texto + "\n");

        // 2. Lógica según en qué paso estemos
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
                pasoActual = 1; // Pasamos al modo "Wizard"
                imprimir("--- Nuevo Gasto ---");
                imprimir("Introduce el Concepto:");
                break;

            case "borrar":
                listarGastos();
                pasoActual = 10; // Estado especial para borrado
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

    // --- LÓGICA PASO A PASO (Wizard) ---

    private void procesarPasoRegistro(String entrada) {
        try {
            switch (pasoActual) {
                // CASO REGISTRAR
                case 1: // Esperando Concepto
                    if(entrada.isEmpty()) throw new Exception("El concepto no puede estar vacío.");
                    tempConcepto = entrada;
                    pasoActual = 2;
                    imprimir("Introduce el Importe:");
                    break;

                case 2: // Esperando Importe
                    tempImporte = Double.parseDouble(entrada.replace(",", "."));
                    pasoActual = 3;
                    imprimir("Introduce Categoría (o pulsa Enter para General):");
                    break;

                case 3: // Esperando Categoría
                    String catNombre = entrada;
                    // Buscar o defecto
                    Categoria cat = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(catNombre))
                            .findFirst()
                            .orElse(cuentaActiva.getCategorias().get(0));
                    
                    tempCategoria = cat.getNombre(); // Guardamos solo el nombre o el objeto

                    // Si es compartida preguntamos pagador, si no terminamos
                    if (cuentaActiva instanceof CuentaCompartida) {
                        pasoActual = 4;
                        imprimir("Pagador (Nombre):");
                    } else {
                        guardarGasto(cat, "Yo");
                    }
                    break;

                case 4: // Esperando Pagador (Solo compartidas)
                    // Volvemos a buscar la categoría porque en el paso anterior solo guardé el nombre temporal
                    // (O podrías guardar el objeto Categoria en una variable tempCategoriaObj)
                    Categoria catFinal = cuentaActiva.getCategorias().stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(tempCategoria))
                            .findFirst().orElse(cuentaActiva.getCategorias().get(0));
                    
                    guardarGasto(catFinal, entrada);
                    break;

                // CASO BORRAR
                case 10: // Esperando ID para borrar
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
            imprimir("(!) Error: Debes introducir un número válido.");
        } catch (Exception e) {
            imprimir("(!) Error: " + e.getMessage());
            pasoActual = 0; // Cancelar operación
            reiniciarPrompt();
        }
    }

    private void guardarGasto(Categoria cat, String pagador) {
        Gasto nuevo = new Gasto(tempConcepto, tempImporte, LocalDate.now(), cat, pagador);
        cuentaActiva.agregarGasto(nuevo);
        guardarCambios();
        
        imprimir("✓ Gasto guardado correctamente.");
        pasoActual = 0; // Volver al inicio
        reiniciarPrompt();
    }
    
    private void guardarCambios() {
        cuentaService.agregarCuenta(null, cuentaActiva);
        if (onUpdateAction != null) {
            onUpdateAction.run();
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void listarGastos() {
        if (cuentaActiva.getGastos().isEmpty()) {
            imprimir("(Sin gastos)");
            return;
        }
        
        // 1. Definimos el ancho de las columnas
        // %-10s : Columna NUM (10 espacios, alineado izquierda)
        // %-25s : Columna CONCEPTO (25 espacios, alineado izquierda)
        // %-15s : Columna IMPORTE (15 espacios, alineado izquierda)
        // %-15s : Columna CATEGORIA
        String formato = "%-10s %-25s %-15s %-15s";

        // 2. Imprimimos Cabecera
        imprimir(String.format(formato, "NUM", "CONCEPTO", "IMPORTE", "CATEGORIA"));
        
        // (Opcional) Línea separadora simple sin barras verticales
        imprimir("----------------------------------------------------------------");

        for (int i = 0; i < cuentaActiva.getGastos().size(); i++) {
            Gasto g = cuentaActiva.getGastos().get(i);
            
            // 3. Preparamos los datos individuales
            String columnaNum = "[" + i + "]"; 
            String columnaImporte = String.format("%.2f€", g.getImporte()); // Formato con 2 decimales y símbolo €
            
            // 4. Creamos la línea final aplicando el formato de columnas
            String linea = String.format(formato, 
                                         columnaNum, 
                                         g.getConcepto(), 
                                         columnaImporte, 
                                         g.getCategoria().getNombre());
            
            imprimir(linea);
        }
    }

    private void imprimir(String texto) {
        txtOutput.appendText(texto + "\n");
    }

    private void reiniciarPrompt() {
        txtOutput.appendText("\nCMD> ");
    }

    public void setOnUpdate(Runnable action) {
        this.onUpdateAction = action;
    }
}
*/