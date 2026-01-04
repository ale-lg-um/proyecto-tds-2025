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
                    
                    if(pagador.equalsIgnoreCase("Me")) {
                    	pagador = "Yo";
                    }

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
>>>>>>> 5eb73b302941a3206b867deed0d59891e3059c3f
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
<<<<<<< HEAD
}
=======
}
*/

