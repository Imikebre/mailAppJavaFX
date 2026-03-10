package com.example.client.controller;

import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class LoginController {
    @FXML
    public Button loginButton;
    @FXML
    private TextField loginMail;
    @FXML
    private Label textLabel;

    ClientModel model;

    public void setModel(ClientModel model) {
        this.model = model;
    }


    @FXML
    public void onLogin(){
        if(!model.isValidAddress(loginMail.getText())) { //TODO
            try{
                new PopupManager(new Stage(), "Could not login", "Please insert a valid mail address : example@at.mail.com");
            } catch (IOException e) {
                System.err.println("Could not open popup: " + e.getMessage());
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
        controller.setModel(model);

        testEmails(model);

        Stage stage = (Stage) loginMail.getScene().getWindow(); // get Stage from an FXML element
        stage.getScene().setRoot(fxmlroot); // Reuse previous scene
        stage.setTitle("");
        stage.setHeight(400);
        stage.setWidth(675);
        stage.setMaxWidth(675);
        stage.show();
    }

    private static void testEmails(ClientModel clientModel) {

        clientModel.getMailsFromFile(
                FXCollections.observableArrayList(
                        new Email("anna@example.com", List.of("mario@example.com, luca@libero.it, giovanni@edu.com, mario@example.com, luca@libero.it, giovanni@edu.com"), "Presentazione cliente", "Allego le slide per la presentazione di lunedì.", LocalDateTime.now()),
                        new Email("mario@example.com", List.of("boss@example.com"), "Stato avanzamento", "Ti aggiorno sullo stato del progetto: siamo in linea con i tempi.", LocalDateTime.now()),
                        new Email("boss@example.com", List.of("mario@example.com"), "Ottimo lavoro", "Complimenti per i risultati raggiunti questo mese!", LocalDateTime.now())
                )
        );

        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(event -> {
            clientModel.addMail(new Email("PROVA", List.of("mario@example.com"), "Ottimo lavoro", "Complimenti per i risultati raggiunti questo mese!", LocalDateTime.now()));
        });
        pause.play();
    }
}
