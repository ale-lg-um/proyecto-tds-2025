package gestorgastos.controller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class CreditosController {

    @FXML private VBox contenedorTexto;
    @FXML private Pane areaRecorte; // El panel dentro del ticket
    @FXML private Pane panelFondo; // El panel grande de fondo
    @FXML private Button btnVolver;

    private final Random random = new Random();

    @FXML
    public void initialize() {
        configurarAnimacionTicket();
        iniciarLluviaDeDinero();
    }

    private void configurarAnimacionTicket() {
        // Hacemos que el texto solo sea visible dentro del área blanca del ticket
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(areaRecorte.widthProperty());
        clip.heightProperty().bind(areaRecorte.heightProperty());
        areaRecorte.setClip(clip);

        // Animación
        double alturaInicio = 400; // Empieza abajo
        double alturaFin = -600;   // Termina arriba

        TranslateTransition transition = new TranslateTransition();
        transition.setNode(contenedorTexto);
        transition.setDuration(Duration.seconds(15)); // Velocidad de lectura
        transition.setFromY(alturaInicio);
        transition.setToY(alturaFin);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(Animation.INDEFINITE); // Se repite para siempre
        
        transition.play();
    }

    private void iniciarLluviaDeDinero() {
        // Creamos un generador de monedas cada X milisegundos
        Timeline lluvia = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            crearMonedaCayendo();
        }));
        lluvia.setCycleCount(Animation.INDEFINITE);
        lluvia.play();
    }

    private void crearMonedaCayendo() {
        // Creamos un símbolo de Euro o Dólar
        Label moneda = new Label(random.nextBoolean() ? "€" : "$");
        
        // Estilo aleatorio
        moneda.setTextFill(Color.web("#85e3a5", 0.6)); // Verde clarito semitransparente
        moneda.setFont(Font.font("Arial", 20 + random.nextInt(30))); // Tamaño variable
        
        // Posición inicial aleatoria en X
        moneda.setLayoutX(random.nextInt(800));
        moneda.setLayoutY(-50); // Empieza arriba fuera de pantalla

        panelFondo.getChildren().add(moneda);

        // Animación de caída
        TranslateTransition caida = new TranslateTransition();
        caida.setNode(moneda);
        caida.setDuration(Duration.seconds(3 + random.nextInt(4))); // Entre 3 y 7 segundos en caer
        caida.setToY(700); // Cae hasta abajo
        caida.setInterpolator(Interpolator.LINEAR);
        
        // Cuando termine de caer, borramos el objeto para no llenar la memoria
        caida.setOnFinished(event -> panelFondo.getChildren().remove(moneda));
        
        caida.play();
    }

    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestorgastos/app_gastos/InicioView.fxml"));
            Scene inicioScene = new Scene(loader.load());

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(inicioScene);
            stage.setTitle("Gestor de Gastos - Inicio");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}