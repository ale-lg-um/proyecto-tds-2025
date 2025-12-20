package gestorgastos.app_gastos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // La consola CLI NO se inicia aquí. Se iniciará cuando entres en una cuenta.
        scene = new Scene(loadFXML("InicioView"), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Gestor de Gastos - Inicio");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
/*
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
*/
