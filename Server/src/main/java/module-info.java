module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires com.example.common;
    requires java.desktop;
    requires jdk.jfr;


    opens com.example.server to javafx.fxml;
    exports com.example.server;
}