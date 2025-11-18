module gestorgastos.app_gastos {
    requires javafx.controls;
    requires javafx.fxml;

    exports gestorgastos.app_gastos; // ✅ permite que JavaFX acceda a App.java
    opens gestorgastos.controller to javafx.fxml; // ✅ permite cargar controladores FXML
}
