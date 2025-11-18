module proyecto_tds.gastos {
    requires javafx.controls;
    requires javafx.fxml;

    opens proyecto_tds.gastos to javafx.fxml;
    exports proyecto_tds.gastos;
}
