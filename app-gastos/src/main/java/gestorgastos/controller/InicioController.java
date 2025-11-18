package gestorgastos.controller;

import gestorgastos.services.SesionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class InicioController {

    @FXML
    private TextField usuarioField;

    @FXML
    private void handleEntrar(ActionEvent event) {
        String nombre = usuarioField.getText();
        if (nombre == null || nombre.isBlank()) {
            System.out.println("⚠️ Usuario vacío");
            return;
        }

        // Guardar usuario en SesionService
        SesionService.getInstancia().iniciarSesion(nombre);

        // Cambiar a la pantalla principal
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/PrincipalView.fxml"));
            Scene principalScene = new Scene(loader.load());

            // Obtener la ventana actual y cambiar la escena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(principalScene);
            stage.setTitle("Gestor de Gastos - Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
