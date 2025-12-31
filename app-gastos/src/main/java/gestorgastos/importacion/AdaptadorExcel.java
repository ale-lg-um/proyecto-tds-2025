package gestorgastos.importacion;

import gestorgastos.model.GastoTemporal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Faltaba este import
import java.util.ArrayList;
import java.util.Arrays; // Faltaba este import
import java.util.List;

public class AdaptadorExcel extends Importador {

    // 1. LISTA DE FORMATOS (Faltaba esto en tu código)
    // Es vital para que 'parsearFechaRobusta' funcione.
    private final List<DateTimeFormatter> formatters = Arrays.asList(
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),     // Prioridad: USA (Ej: 12/18/2025)
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),   
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),     // Europa
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"), // ISO / SQL
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm")
    );

    @Override
    public List<GastoTemporal> leerFichero(String ruta) throws Exception {
        List<GastoTemporal> lista = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(ruta));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                // Saltar cabecera
                if (row.getRowNum() == 0) continue;

                try {
                    // --- 1. LECTURA DE FECHA ---
                    Cell celdaFecha = row.getCell(0);
                    if (celdaFecha == null) continue;

                    LocalDateTime fechaHora = null;

                    // CASO A: Excel numérico real
                    if (celdaFecha.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celdaFecha)) {
                        fechaHora = celdaFecha.getLocalDateTimeCellValue();
                    } 
                    // CASO B: Texto (Aquí entra el 12/18/2025)
                    else {
                        String fechaTexto = getValorCelda(celdaFecha);
                        fechaHora = parsearFechaRobusta(fechaTexto); 
                    }

                    if (fechaHora == null) continue;

                    // --- 2. LECTURA DE CAMPOS ---
                    String cuenta      = getValorCelda(row.getCell(1)); // Col B: Account
                    // Col C (row.getCell(2)) es 'Category' (efectivo/tarjeta), la ignoramos
                    String categoria   = getValorCelda(row.getCell(3)); // Col D: Subcategory -> Categoria App
                    String concepto    = getValorCelda(row.getCell(4)); // Col E: Note -> Concepto
                    String pagador     = getValorCelda(row.getCell(5)); // Col F: Payer

                    // --- 3. LECTURA DE IMPORTE ---
                    double importe = 0.0;
                    Cell celdaImporte = row.getCell(6); // Col G: Amount
                    
                    if (celdaImporte != null && celdaImporte.getCellType() == CellType.NUMERIC) {
                        importe = celdaImporte.getNumericCellValue();
                    } else {
                        // Limpieza de símbolos de moneda y comas
                        String impTxt = getValorCelda(celdaImporte)
                                .replace(",", ".")
                                .replace("EUR", "")
                                .replace("€", "")
                                .trim();
                        if (!impTxt.isEmpty()) {
                            importe = Double.parseDouble(impTxt);
                        }
                    }

                    // --- 4. CREAR OBJETO ---
                    GastoTemporal temp = new GastoTemporal(
                            cuenta, concepto, importe, categoria, pagador,
                            fechaHora.toLocalDate(), fechaHora.toLocalTime()
                    );
                    lista.add(temp);

                } catch (Exception e) {
                    // Imprimimos el error pero seguimos con la siguiente fila
                    System.err.println("⚠️ Error leyendo fila Excel " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }
        return lista;
    }

    // --- MÉTODOS AUXILIARES QUE FALTABAN ---

    // Este método prueba varios formatos hasta que uno funciona
    private LocalDateTime parsearFechaRobusta(String fechaStr) throws Exception {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        String fechaLimpia = fechaStr.trim();

        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(fechaLimpia, fmt);
            } catch (Exception e) {
                // Ignorar y probar el siguiente
            }
        }
        throw new Exception("Formato de fecha desconocido: " + fechaStr);
    }

    private String getValorCelda(Cell cell) {
        if (cell == null) return "";
        DataFormatter fmt = new DataFormatter();
        return fmt.formatCellValue(cell).trim();
    }

    @Override
    protected GastoTemporal parsearGasto(String linea) {
        return null; 
    }
}
/*
package gestorgastos.importacion;

import gestorgastos.model.GastoTemporal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdaptadorExcel extends Importador {

    // LISTA DE FORMATOS POSIBLES (Para que no falle si Excel cambia algo)
    private final List<DateTimeFormatter> formatters = Arrays.asList(
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),       // Ej: 12/18/2025 21:02
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"),  // Ej: 2024-02-05 12:33:00 (Excel a veces añade segundos)
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm"),     // Ej: 2024-02-05 12:33
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),       // Ej: 05/02/2024 12:33 (Europeo)
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss")     // Ej: 5/2/2024 12:33:00
    );

    @Override
    public List<GastoTemporal> leerFichero(String ruta) throws Exception {
        List<GastoTemporal> lista = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(ruta));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            for (Row row : sheet) {
                // Saltamos cabecera
                if (row.getRowNum() == 0) continue;

                try {
                    // Leemos las celdas como Texto (String) para evitar líos internos de Excel
                    // A=0, B=1, C=2, D=3, E=4, F=5, G=6
                    String fechaStr = dataFormatter.formatCellValue(row.getCell(0));
                    String cuenta = dataFormatter.formatCellValue(row.getCell(1));
                    String categoriaApp = dataFormatter.formatCellValue(row.getCell(3)); 
                    String concepto = dataFormatter.formatCellValue(row.getCell(4)); 
                    String pagador = dataFormatter.formatCellValue(row.getCell(5));
                    String importeStr = dataFormatter.formatCellValue(row.getCell(6));

                    // Validación básica
                    if (fechaStr == null || fechaStr.trim().isEmpty() || cuenta.isEmpty()) continue;

                    // INTENTO DE PARSEO DE FECHA (Prueba todos los formatos)
                    LocalDateTime fechaHora = parsearFechaRobusta(fechaStr);

                    // Limpieza de importe (cambiar comas por puntos)
                    double importe = Double.parseDouble(importeStr.replace(",", "."));

                    GastoTemporal temp = new GastoTemporal(
                            cuenta,
                            concepto,
                            importe,
                            categoriaApp,
                            pagador,
                            fechaHora.toLocalDate(),
                            fechaHora.toLocalTime()
                    );

                    lista.add(temp);

                } catch (Exception e) {
                    // Mostramos error pero NO detenemos la importación
                    System.err.println("⚠️ Error saltando fila " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }
        return lista;
    }

    // Método auxiliar que prueba varios formatos
    private LocalDateTime parsearFechaRobusta(String fechaStr) throws Exception {
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(fechaStr, fmt);
            } catch (Exception e) {
                // Si falla, probamos el siguiente formato del bucle
            }
        }
        // Si llegamos aquí, ninguno funcionó
        throw new Exception("Formato de fecha desconocido: " + fechaStr);
    }

    @Override
    protected GastoTemporal parsearGasto(String linea) {
        return null; 
    }
}
*/