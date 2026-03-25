package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.client.utility.PopupManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * A  Controller that handles the login routine using the ClientModel methods
 * It's responsible to configure the Client Controller to start the application.
 * @author Michele Brescia
 */
public class LoginController {
    @FXML
    public Button loginButton;
    @FXML
    private TextField loginMail;
    @FXML
    private Label textLabel;

    ClientModel model;

    Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setModel(ClientModel model) {
        this.model = model;
    }


    @FXML
    public void onLogin(){
        try { model.isValidAddress(loginMail.getText()); }
        catch ( MailException e ){
            try{
                new PopupManager().showView(new Stage(), "Could not login", e.getMessage(), "ERROR");
            } catch (IOException x) {
                System.err.println("Could not open popup: " + x.getMessage());
            }
            return;
        }

        model.setUserMail(loginMail.getText());

        try{
            setScene();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void setScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/com/example/client/client-view.fxml"));
        Parent fxmlroot = loader.load();

        ClientController controller = loader.getController();
        controller.setStage(stage);
        controller.setModel(model);
        controller.startClient();

        stage.getScene().setRoot(fxmlroot); // Reuse previous scene
        stage.setTitle("");
        stage.setHeight(400);
        stage.setWidth(675);
        stage.setMaxWidth(675);
        stage.show();
    }

}
