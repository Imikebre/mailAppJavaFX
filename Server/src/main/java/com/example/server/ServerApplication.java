package com.example.server;

import com.example.server.model.ServerModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("server-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        ServerModel model = new ServerModel();
        ServerController controller = fxmlLoader.getController();

        controller.setModel(model);
        controller.setStage(stage);
        model.initialize();

        stage.setTitle("");
        stage.setScene(scene);
        stage.show();
    }
}
