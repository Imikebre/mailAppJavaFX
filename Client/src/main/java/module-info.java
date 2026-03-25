module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires com.example.common;
    requires java.desktop;
    requires java.logging;

    opens com.example.client to javafx.fxml;
    exports com.example.client;
    exports com.example.client.controller;
    opens com.example.client.controller to javafx.fxml;
    exports com.example.client.model;
    opens com.example.client.model to javafx.fxml;
    exports com.example.client.utility;
    opens com.example.client.utility to javafx.fxml;
}