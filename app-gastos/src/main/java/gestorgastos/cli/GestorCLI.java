package gestorgastos.cli;

import gestorgastos.model.*;
import gestorgastos.services.CuentaService;

import java.time.LocalDate;
import java.util.Scanner;

public class GestorCLI implements Runnable {

    private final Cuenta cuentaActiva;
    private final CuentaService cuentaService = CuentaService.getInstancia();
    private final Scanner scanner = new Scanner(System.in);
    private volatile boolean activo = true; // 'volatile' para asegurar que el hilo vea el cambio

    public GestorCLI(Cuenta cuenta) {
        this.cuentaActiva = cuenta;
    }

    public void detener() {
        this.activo = false;
        System.out.println("\n--- [CLI] Consola detenida para: " + cuentaActiva.getNombre() + " ---");
    }

    @Override
    public void run() {
        System.out.println("\n================================================");
        System.out.println("  CONSOLA DE GESTIÓN ACTIVA: " + cuentaActiva.getNombre());
        System.out.println("  Comandos disponibles: listar, registrar, borrar, ayuda");
        System.out.println("================================================\n");
        System.out.print(cuentaActiva.getNombre() + "> ");

        while (activo) {
            try {
                // Verificamos si hay entrada disponible para no bloquear el hilo eternamente
                if (System.in.available() > 0) {
                    String linea = scanner.nextLine().trim();
                    if (!activo) break; // Doble check por seguridad

                    procesarComando(linea.toLowerCase());
                    
                    if (activo) {
                        System.out.print(cuentaActiva.getNombre() + "> ");
                    }
                } else {
                    // Pequeña pausa para no saturar la CPU buscando input
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                // Ignorar interrupciones
            }
        }
    }

    private void procesarComando(String comando) {
        switch (comando) {
            case "ayuda":
                mostrarAyuda();
                break;
            case "listar":
                listarGastos();
                break;
            case "registrar":
                registrarGasto();
                break;
            case "borrar":
                borrarGasto();
                break;
            case "salir":
                System.out.println("Para salir, usa el botón 'Volver' de la interfaz gráfica.");
                break;
            default:
                if (!comando.isEmpty()) System.out.println("Comando no reconocido. Escribe 'ayuda'.");
        }
    }

    private void mostrarAyuda() {
        System.out.println(" --- Ayuda CLI ---");
        System.out.println(" listar    -> Ver lista de gastos con su ID");
        System.out.println(" registrar -> Añadir un nuevo gasto paso a paso");
        System.out.println(" borrar    -> Eliminar un gasto usando su ID");
    }

    private void listarGastos() {
        if (cuentaActiva.getGastos().isEmpty()) {
            System.out.println("    (Esta cuenta no tiene gastos)");
            return;
        }
        System.out.println(" --- Gastos de " + cuentaActiva.getNombre() + " ---");
        for (int i = 0; i < cuentaActiva.getGastos().size(); i++) {
            Gasto g = cuentaActiva.getGastos().get(i);
            System.out.printf("    [%d] %s | %.2f€ | %s\n", i, g.getConcepto(), g.getImporte(), g.getCategoria().getNombre());
        }
    }

    private void registrarGasto() {
        try {
            System.out.print("Concepto: ");
            String concepto = scanner.nextLine();

            System.out.print("Importe: ");
            String impStr = scanner.nextLine().replace(",", ".");
            double importe = Double.parseDouble(impStr);

            System.out.print("Categoría (Nombre): ");
            String catNombre = scanner.nextLine();
            
            // Lógica para buscar la categoría existente en la cuenta
            Categoria cat = cuentaActiva.getCategorias().stream()
                    .filter(c -> c.getNombre().equalsIgnoreCase(catNombre))
                    .findFirst()
                    .orElse(null);

            if (cat == null) {
                System.out.println("(!) Categoría no encontrada. Usando la primera disponible.");
                cat = cuentaActiva.getCategorias().get(0);
            }

            String pagador = "Yo";
            if (cuentaActiva instanceof CuentaCompartida) {
                System.out.print("Pagador (Nombre exacto): ");
                pagador = scanner.nextLine();
            }

            Gasto nuevo = new Gasto(concepto, importe, LocalDate.now(), cat, pagador);
            cuentaActiva.agregarGasto(nuevo);
            
            // GUARDAR EN JSON
            cuentaService.agregarCuenta(null, cuentaActiva);
            System.out.println("✓ Gasto registrado. (Nota: Refresca la tabla visual entrando y saliendo).");

        } catch (NumberFormatException e) {
            System.out.println("(!) Error: El importe debe ser un número.");
        } catch (Exception e) {
            System.out.println("(!) Error al registrar: " + e.getMessage());
        }
    }

    private void borrarGasto() {
        listarGastos();
        try {
            System.out.print("Introduce el ID (número) a borrar: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            if (id >= 0 && id < cuentaActiva.getGastos().size()) {
                Gasto eliminado = cuentaActiva.getGastos().remove(id);
                cuentaService.agregarCuenta(null, cuentaActiva); // Guardar
                System.out.println("✓ Gasto '" + eliminado.getConcepto() + "' eliminado.");
            } else {
                System.out.println("(!) ID no válido.");
            }
        } catch (Exception e) {
            System.out.println("(!) Error al borrar. Introduce un número válido.");
        }
    }
}
