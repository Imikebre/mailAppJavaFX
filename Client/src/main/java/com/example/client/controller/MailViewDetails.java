package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.client.utility.PopupManager;
import com.example.common.Email;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A mail preview controller whose main task is to provide a detailed view of an incoming Email object.
 * Through the UI buttons, it creates a MailSendController with pre-filled fields to speed up replying and forwarding operations
 * @author Michele Brescia
 */
public class MailViewDetails {
    ClientModel model;
    Stage stage;

    @FXML
    Button deleteEmail;
    @FXML
    Button replyAll;
    @FXML
    Button reply;
    @FXML
    Button forward;
    @FXML
    Label senderLabel;
    @FXML
    Label recipientsLabel;
    @FXML
    Label subjectLabel;
    @FXML
    TextArea bodyArea;

    /**
     * Constructs a MailViewDetails object
     * @param clientModel application model
     * @param email to be displayed
     */
    public MailViewDetails(ClientModel clientModel, Email email) {
        this.model = clientModel;
        setView(email);
    }

    private void setView(Email email) {
        FXMLLoader loader = new FXMLLoader(MailViewDetails.class.getResource("/com/example/client/email-view.fxml"));
        Parent fxmlroot;

        loader.setController(this);
        try{
            fxmlroot = loader.load();
        }catch(IOException e){
            System.err.println("Could not load MailView.fxml : " + e.getMessage());
            return;
        }

        stage = new Stage();
        Scene scene = new Scene(fxmlroot);
        stage.setScene(scene);

        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        stage.setOnCloseRequest(event -> { // Deselects mail on closing, allows MailPreviewCell to successfully delete said object
                model.deselectMail(email);
        });

        senderLabel.setText(email.getSender());
        recipientsLabel.setText(email.getRecipients());
        subjectLabel.setText(email.getSubject());
        bodyArea.setText(email.getBody());

        deleteEmail.setOnAction(event -> new Thread(() -> {
            try{
                model.forceDeleteMail(email); // force deletes to bypass selected mail restriction
                model.deselectMail(email);
                Platform.runLater(()-> stage.close()); // UI operations run by JavaFX thread
            }
            catch(MailException e){
                Platform.runLater(()->{ // UI operations run by JavaFX thread
                    try{
                        new PopupManager().showView(stage, "Couldn't delete email", e.getMessage(), "ERROR");
                    }
                    catch(IOException ex){
                        ex.printStackTrace();
                    }
                });
            }
        }).start());

        replyAll.setOnAction(event -> {
            List<String> recipients = new ArrayList<>(email.getRecipientsList());
            recipients.add(email.getSender());
            recipients.remove(model.getUserMail());

            new MailSendController(model, recipients , "RE : " + email.getSubject(), ("\n ─────────────────────────── \n Reply to : " + email.getSender() + "\n" + email.getBody()));
        });

        reply.setOnAction(event -> {
            List<String> recipients = List.of(email.getSender());
            new MailSendController(model, recipients, "RE : " + email.getSubject(), ("\n ─────────────────────────── \n Reply to : " + email.getSender() + "\n" + email.getBody()));
        });

        forward.setOnAction(event -> new MailSendController(model, List.of(""), "FWD : " + email.getSubject(), ("\n ─────────────────────────── \n FWD from : " + email.getSender() + "\n" + email.getBody())));

        stage.show();
    }
}
