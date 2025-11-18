module um.gestorgastos.app_gastos {
    requires javafx.controls;
    requires javafx.fxml;

    opens um.gestorgastos.app_gastos to javafx.fxml;
    exports um.gestorgastos.app_gastos;
}
