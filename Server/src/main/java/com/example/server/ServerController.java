package com.example.server;

import com.example.server.model.ServerModel;
import com.example.server.exceptions.ServerModelException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerController {
    @FXML
    public Button userAdd;
    @FXML
    TextArea console;
    @FXML
    Label usersRegistered;
    @FXML
    TextField userMail;
    @FXML
    ToggleButton toggle;

    private ServerModel model;

    public void setModel(ServerModel model) {
        this.model = model;

        this.model.getLogStringProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals("Shutting down")) {
                console.setStyle("-fx-text-fill: red;");
            }
            if(newValue.equals("Going live")) {
                console.setStyle("-fx-text-fill: black;");
            }
            console.appendText("\n"+newValue+"\n");
        });

        usersRegistered.textProperty().bind(this.model.getUsersRegisteredProperty().asString());

        toggle.setOnAction(event -> {
            if(toggle.getText().equals("Shutdown")){
                model.shutdown();
                toggle.setText("Go Live");
            }
            else if(toggle.getText().equals("Go Live")){
                model.goLive();
                toggle.setText("Shutdown");
            }
        });
    }

    public void setStage(Stage stage){
        stage.setOnCloseRequest(event -> {
            try{
                model.saveState();
            } catch (ServerModelException e) {
                console.appendText(e.getMessage());
            }
        });
    }

    @FXML
    public void addUser() {

        try{
            model.addUser(userMail.getText());
        }
        catch(ServerModelException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("User Creation Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            hideMailPrompt();
            return;
        }

        userMail.clear();
        userMail.setVisible(false);
        userAdd.setVisible(false);
    }

    @FXML
    public void showMailPrompt() {
        userMail.setVisible(true);
        userAdd.setVisible(true);
        userMail.managedProperty().bind(userMail.visibleProperty());
        userAdd.managedProperty().bind(userAdd.visibleProperty());
    }

    public void hideMailPrompt() {
        userMail.setVisible(false);
        userAdd.setVisible(false);
    }
    @FXML
    public void clearView() {
        console.clear();
        console.appendText("Console has been cleared\n");
    }
}
