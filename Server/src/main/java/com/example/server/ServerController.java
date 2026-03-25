package com.example.server;

import com.example.server.model.ServerModel;
import com.example.server.exceptions.ServerModelException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Controller class for the Server's Graphical User Interface.
 * <p>
 * This class handles UI events, binds the view components to the {@link ServerModel},
 * and manages user interactions such as adding new users, toggling the server state,
 * and managing the system log console.
 * </p>
 *
 * @author Michele Brescia
 */
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
            if(console.getLength() > 50000 )
                console.clear();
            if(newValue.contains("Shutting down")) {
                console.setStyle("-fx-text-fill: red;");
            }
            if(newValue.contains("Going live")) {
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
        model.setLogString("Console has been cleared\n");
    }
    @FXML
    public void saveLog() {
        File logFile = new File(model.getDataDir() + "/server_log.txt");

        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write(console.getText());
            console.appendText("\n[SYSTEM] Log saved successfully to: " + logFile.getAbsolutePath() + "\n");
        } catch (IOException e) {
            console.appendText("\n[SYSTEM ERROR] Could not save log: " + e.getMessage() + "\n");
        }
    }
}
