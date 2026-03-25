package com.example.client;

import com.example.client.controller.LoginController;
import com.example.client.model.ClientModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 *  Starts the Mail Client application by starting the initial Scene and the login Controller and model
 *  @author Michele Brescia
 */
public class ClientApplication extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource("/com/example/client/login.fxml"));
        Parent fxmlroot = loader.load();

        LoginController controller = loader.getController(); // Instance automatically created by JAVA FX through fx:controller
        ClientModel model = new ClientModel();

        controller.setModel(model);
        controller.setStage(stage);

        Scene scene = new Scene(fxmlroot); // Dimension are already set in the fxml
        stage.setTitle("");
        stage.setScene(scene);
        stage.show();
    }
}
