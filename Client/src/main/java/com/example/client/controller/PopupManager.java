package com.example.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PopupManager {
    @FXML
    Label errorList;
    @FXML
    Button button;

    public  PopupManager(Stage parentStage, String title, String errors) throws IOException {
        this(parentStage, title, errors, "Notification");
    }

    public PopupManager(Stage parentStage, String title, String errors, String alertType) throws IOException {
        FXMLLoader loader;
        if(alertType.equals("ERROR"))
            loader = new FXMLLoader(PopupManager.class.getResource("/com/example/client/popup.fxml"));
        else
            loader = new FXMLLoader(PopupManager.class.getResource("/com/example/client/notification.fxml"));

        loader.setController(this);
        Parent root = loader.load();

        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initOwner(parentStage);
        alertStage.setAlwaysOnTop(true);
        alertStage.setScene(new Scene(root));
        alertStage.setTitle(title);

        if(!errors.isEmpty()) this.errorList.setText(errors);
        else this.errorList.visibleProperty().setValue(false);

        button.setOnAction(e -> ((Stage) button.getScene().getWindow()).close());
        alertStage.setOnCloseRequest(e -> ((Stage) button.getScene().getWindow()).close());

        alertStage.showAndWait();

    }
}
