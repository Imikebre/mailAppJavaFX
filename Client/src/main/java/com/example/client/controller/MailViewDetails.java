package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
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
import java.util.logging.Logger;

public class MailViewDetails {
    ClientModel model;
    Logger logger = Logger.getLogger("MailClient");
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

    public MailViewDetails(ClientModel clientModel, Email email) {
        this.model = clientModel;
        setView(email);
        logger.info("Mail view has been created");
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

        stage.setOnCloseRequest(event -> {
            new Thread(() -> {
                model.deselectMail(email);
            }).start();
        });


        senderLabel.setText(email.getSender());
        recipientsLabel.setText(email.getRecipients());
        subjectLabel.setText(email.getSubject());
        bodyArea.setText(email.getBody());

        deleteEmail.setOnAction(event -> { // TODO check
            new Thread(() -> {
                model.deselectMail(email);
                try{
                    model.forceDeleteMail(email);
                    Platform.runLater(()->{stage.close();}); // UI handling
                }
                catch(MailException e){
                    Platform.runLater(()->{ // UI handling
                        try{
                            new PopupManager(stage, "Couldn't delete email", e.getMessage(), "ERROR");
                        }
                        catch(IOException ex){
                            ex.printStackTrace();
                        }
                    });
                    model.selectMail(email); // Return to selected state
                }
            }).start();
        });

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

        forward.setOnAction(event -> {
            new MailSendController(model, List.of(""), "FWD : " + email.getSubject(), ("\n ─────────────────────────── \n FWD from : " + email.getSender() + "\n" + email.getBody()));
        });

        stage.show();
    }
}
