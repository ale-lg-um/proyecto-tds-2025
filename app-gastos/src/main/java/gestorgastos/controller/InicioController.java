
package gestorgastos.controller;

import gestorgastos.services.SesionService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class InicioController {

    @FXML
    private TextField usuarioField;

    private SesionService sesionService = new SesionService();

    @FXML
    private void handleEntrar() {
        String nombre = usuarioField.getText();
        sesionService.iniciarSesion(nombre);

        // Aquí podrías cargar otra vista (ej. PrincipalView.fxml) si el login es correcto
        // Por ahora solo imprime en consola
    }
}
