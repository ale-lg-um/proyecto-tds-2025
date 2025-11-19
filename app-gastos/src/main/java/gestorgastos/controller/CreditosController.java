package gestorgastos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class CreditosController {

	@FXML
	private TextArea textoCred;
	
	@FXML
	private Button btnVolver;
	
	
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
	        System.out.println("❌ Error al volver a InicioView: " + e.getMessage());
	    }
	}
	
	

	
	
}
