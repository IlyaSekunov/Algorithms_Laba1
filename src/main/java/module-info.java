module ru.sekunovilya.algorithms_laba1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.sekunovilya.algorithms_laba1 to javafx.fxml;
    exports ru.sekunovilya.algorithms_laba1;
}