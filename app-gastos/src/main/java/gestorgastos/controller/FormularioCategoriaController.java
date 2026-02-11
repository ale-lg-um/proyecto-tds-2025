package gestorgastos.controller;

import gestorgastos.model.Categoria;
import gestorgastos.services.CategoriasService;
import gestorgastos.services.CuentaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FormularioCategoriaController {

    @FXML private TextField txtNombre;
    @FXML private ColorPicker colorPicker;
    @FXML private Label lblError;
    
    private Categoria categoriaResultado;
    
    private final CategoriasService categoriaService = CategoriasService.getInstancia();

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.WHITE); // Por defecto
    }

    public Categoria getCategoriaResultado() {
        return categoriaResultado;
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            lblError.setText("El nombre es obligatorio");
            return;
        }

        Color c = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue() * 255));

        categoriaResultado = CategoriasService.crearCategorias(nombre, "", hex);
        
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    @FXML
    private void cancelar() {
        categoriaResultado = null;
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}