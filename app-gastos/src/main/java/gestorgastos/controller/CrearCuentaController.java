package gestorgastos.controller;

import gestorgastos.model.Usuario;
import gestorgastos.services.CuentaService;
import gestorgastos.services.SesionService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrearCuentaController {

    @FXML private TextField nombreCuentaField;
    @FXML private Button btnCrear;

    private CuentaService cuentaService = CuentaService.getInstancia();
    private Runnable onCuentaCreada; // callback para actualizar lista

    public void setOnCuentaCreada(Runnable callback) {
        this.onCuentaCreada = callback;
    }

    @FXML
    public void initialize() {
        btnCrear.setOnAction(e -> {
            String nombre = nombreCuentaField.getText();
            if (nombre == null || nombre.isBlank()) {
                System.out.println("⚠️ Nombre vacío");
                return;
            }

            Usuario usuario = SesionService.getInstancia().getUsuarioActivo();
            cuentaService.crearCuenta(usuario, nombre);

            if (onCuentaCreada != null) {
                onCuentaCreada.run(); // actualiza lista en principal
            }

            // cerrar ventana
            Stage stage = (Stage) btnCrear.getScene().getWindow();
            stage.close();
        });
    }
}
