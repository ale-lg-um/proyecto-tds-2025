package gestorgastos.importacion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gestorgastos.dto.GastoTemporal;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Faltaba este import

import java.util.ArrayList;
import java.util.Arrays; // Faltaba este import
import java.util.List;

public class AdaptadorExcel extends Importador {

   
    // Vital para que 'parsearFechaRobusta' funcione.
    private final List<DateTimeFormatter> formatters = Arrays.asList( // Diversos formatos de fechas aceptados en Excel
        DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),     
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),   
        DateTimeFormatter.ofPattern("d/M/yyyy H:mm"),     
        DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"), 
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
                    // Leer la fecha
                    Cell celdaFecha = row.getCell(0);
                    if (celdaFecha == null) continue;

                    LocalDateTime fechaHora = null;
                    if (celdaFecha.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celdaFecha)) {
                        fechaHora = celdaFecha.getLocalDateTimeCellValue();
                    } else {
                        String fechaTexto = getValorCelda(celdaFecha);
                        fechaHora = parsearFechaRobusta(fechaTexto); 
                    }

                    if (fechaHora == null) continue;

                    // Leer campos
                    String cuenta      = getValorCelda(row.getCell(1)); // Columna B: Account
                    String categoria   = getValorCelda(row.getCell(3)); // Columna D: Subcategory -> Categoria App
                    String concepto    = getValorCelda(row.getCell(4)); // Columna E: Note -> Concepto
                    String pagador     = getValorCelda(row.getCell(5)); // Columna F: Payer
                    
                    if(pagador.equalsIgnoreCase("Me")) {
                    	pagador = "Yo";
                    }

                    // Leer importe
                    double importe = 0.0;
                    Cell celdaImporte = row.getCell(6); // Col G: Amount
                    
                    if (celdaImporte != null && celdaImporte.getCellType() == CellType.NUMERIC) {
                        importe = celdaImporte.getNumericCellValue();
                    } else {
                        // Formateo
                        String impTxt = getValorCelda(celdaImporte)
                                .replace(",", ".")
                                .replace("EUR", "")
                                .replace("€", "")
                                .trim();
                        if (!impTxt.isEmpty()) {
                            importe = Double.parseDouble(impTxt);
                        }
                    }

                    // Creación del gasto
                   GastoTemporal temp = new GastoTemporal(
                            cuenta, concepto, importe, categoria, pagador,
                            fechaHora.toLocalDate(), fechaHora.toLocalTime()
                    );
                    lista.add(temp);

                } catch (Exception e) {
                    // Imprimimos el error 
                    System.err.println("⚠️ Error leyendo fila Excel " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }
        return lista;
    }

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