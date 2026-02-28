package com.example.client.controller;

import com.example.client.model.ClientModel;
import com.example.client.model.LoginModel;
import com.example.common.Email;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginController {
    @FXML
    public Button loginButton;
    @FXML
    private TextField loginMail;
    @FXML
    private Label textLabel;

    private String mail = new String();

    LoginModel model;
    Stage stage;
    ClientModel clientModel;

    public void setModel(LoginModel model) {
        this.model = model;
        clientModel = new ClientModel();
        clientModel.getMailProperty().bind(model.getMailProperty()); // bind
    }

    @FXML
    public void onLogin(){
        mail = loginMail.getText();

        if(!model.validateAddress(mail)){
            textLabel.setText("Invalid Mail Address format, try again ( example@mail.com )");
            textLabel.setStyle("-fx-text-fill: red;");
            loginMail.clear();

            return;
        }
        try{
            setScene();
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    public void setScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/client-view.fxml"));
        Parent root = loader.load(); // prima il load!

        ClientController controller = loader.getController();
        controller.setModel(clientModel);

        ObservableList<Email> testEmails = FXCollections.observableArrayList(
                new Email("anna@example.com", "mario@example.com", "boss@example.com", "Presentazione cliente", "Allego le slide per la presentazione di lunedì."),
                new Email("mario@example.com", "boss@example.com", "anna@example.com", "Stato avanzamento", "Ti aggiorno sullo stato del progetto: siamo in linea con i tempi."),
                new Email("boss@example.com", "mario@example.com", "", "Ottimo lavoro", "Complimenti per i risultati raggiunti questo mese!")
        );

        clientModel.getMailsFromFile(testEmails);

        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(event -> {
            clientModel.addMail(new Email("PROVA", "mario@example.com", "", "Ottimo lavoro", "Complimenti per i risultati raggiunti questo mese!"));
        });
        pause.play();

        Stage stage = (Stage) loginMail.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("");
        stage.setScene(scene);
        stage.show();
    }
}
