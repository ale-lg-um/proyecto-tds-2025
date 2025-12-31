module gestorgastos.app_gastos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires com.calendarfx.view;
    
    // Dependencias de Jackson
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    
    //Para poder abrir Excels
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    

    // Exportar el paquete principal para iniciar la App
    exports gestorgastos.app_gastos;
    
    // Exportar el modelo para que sea visible por el resto de la app
    exports gestorgastos.model; 

    // PERMISOS DE REFLEXIÓN (Aquí estaba el problema)
    
    // Abrimos el controlador a JavaFX para que funcionen los @FXML
    opens gestorgastos.controller to javafx.fxml;
    
    // ¡IMPORTANTE! 
    // Abrimos el modelo a JavaFX (para tablas) Y a Jackson (para el JSON)
    opens gestorgastos.model to javafx.fxml, com.fasterxml.jackson.databind;
    
    opens gestorgastos.services to javafx.fxml;
}