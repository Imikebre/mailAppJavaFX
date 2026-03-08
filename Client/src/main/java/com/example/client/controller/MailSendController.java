package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MailSendController {
    private ClientModel model;
    private Stage stage;
    private List<String> defaultRecipients;
    private String defaultSubject;
    private String defaultBody;

    @FXML
    private TextField subjectBox;
    @FXML
    private TextField recipientsBox;
    @FXML
    private TextArea bodyBox;
    @FXML
    private Button sendMail;

    public MailSendController(ClientModel model) {
        this(model, List.of(""), "", "");
    }

    public MailSendController(ClientModel model, List<String> recipients, String subject, String body) {
        this.model = model;
        this.defaultRecipients = recipients;
        this.defaultSubject = subject;
        this.defaultBody = body;
        setView();
    }


    public void setView() {
        FXMLLoader loader = new FXMLLoader(MailSendController.class.getResource("/com/example/client/email-send.fxml"));
        Parent fxmlroot;
        loader.setController(this);

        try{
            fxmlroot = loader.load();
        }catch(IOException e){
            System.err.println("Could not load MailSend.fxml : " + e.getMessage());
            e.printStackTrace();
            return;
        }
        stage = new Stage();
        Scene scene = new Scene(fxmlroot);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        sendMail.setOnAction(e -> {
            sendMail();
        });

        subjectBox.setText(defaultSubject);
        recipientsBox.setText(String.join(", ", defaultRecipients));
        bodyBox.setText(defaultBody);

        stage.show();
    }

    private void sendMail() {
        String subject = subjectBox.getText();
        List<String> recipients = Arrays.asList(recipientsBox.getText().trim().split("\\s*,\\s*"));
        String body = bodyBox.getText();

        try{
            model.sendMail(recipients, subject, body);
            try{
                new PopupManager(stage, "Mail sent!", "");
                stage.close();
            } catch (IOException e) {
                System.err.println("Could not open pop-up " + e.getMessage());
                e.printStackTrace();
                stage.close();
            }
        }
        catch(MailException e){
            try{
                new PopupManager(stage, "Could not send mail!", e.getMessage());
            } catch (IOException x) {
                System.err.println("Could not open pop-up following mail sending errors" + x.getMessage());
                x.printStackTrace();
                stage.close();
            }
        }

    }
}
