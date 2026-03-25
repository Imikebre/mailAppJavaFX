module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires com.example.common;
    requires java.desktop;
    requires jdk.jfr;
    requires com.google.gson;


    opens com.example.server to javafx.fxml, com.google.gson;
    exports com.example.server;
    exports com.example.server.model;
    opens com.example.server.model to com.google.gson, javafx.fxml;
    exports com.example.server.exceptions;
    opens com.example.server.exceptions to com.google.gson, javafx.fxml;
}