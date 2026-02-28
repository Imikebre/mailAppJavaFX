package com.example.client;

import com.example.client.controller.LoginController;
import com.example.client.model.ClientModel;
import com.example.client.model.LoginModel;
import com.example.common.Email;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ClientApplication extends Application {
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/login.fxml"));
        Parent root = loader.load(); // prima il load!

        LoginController controller = loader.getController(); // poi prendi il controller
        LoginModel model = new LoginModel();

        controller.setModel(model);

        Scene scene = new Scene(root);
        stage.setTitle("");
        stage.setScene(scene);
        stage.show();

    }
}
