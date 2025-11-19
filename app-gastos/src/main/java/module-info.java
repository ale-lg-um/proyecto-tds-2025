module gestorgastos.app_gastos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
	requires javafx.base;

    exports gestorgastos.app_gastos; // ← esto permite que JavaFX acceda a App
    opens gestorgastos.controller to javafx.fxml;
    opens gestorgastos.model to javafx.fxml;
    opens gestorgastos.services to javafx.fxml;
}
