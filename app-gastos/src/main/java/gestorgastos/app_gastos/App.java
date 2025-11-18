package gestorgastos.app_gastos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("InicioView.fxml"));
    	Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Gestor de Gastos - Inicio");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
