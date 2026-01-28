package gestorgastos.controller;

import gestorgastos.model.Cuenta;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.Consumer;

public class GestorNavegacion {

    public static <T> void navegar(Stage stageActual, String fxml, String titulo, Consumer<T> inicializador) {
        try {
            FXMLLoader loader = new FXMLLoader(GestorNavegacion.class.getResource("/gestorgastos/app_gastos/" + fxml));
            Parent root = loader.load();

            if (inicializador != null) {
                T controller = loader.getController();
                inicializador.accept(controller);
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            
            if (fxml.contains("Visualizacion")) {
                stage.setScene(new Scene(root, 1100, 750));
                stage.centerOnScreen();
            } else {
                stage.setScene(new Scene(root));
            }
            
            stage.show();

            if (stageActual != null) stageActual.close();

        } catch (IOException e) {
            e.printStackTrace();
            GestorDialogos.mostrarError("Error de Navegación", "No se pudo cargar la vista: " + fxml);
        }
    }

    public static <T> T abrirModal(String fxml, String titulo, Consumer<T> inicializador) {
        try {
            FXMLLoader loader = new FXMLLoader(GestorNavegacion.class.getResource("/gestorgastos/app_gastos/" + fxml));
            Parent root = loader.load();

            if (inicializador != null) {
                T controller = loader.getController();
                inicializador.accept(controller);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            GestorDialogos.mostrarError("Error", "No se pudo abrir el formulario.");
            return null;
        }
    }
}