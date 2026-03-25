package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.client.utility.PopupManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A controller for the "sending" routine of an Email object.
 * @author Michele Brescia
 */
public class MailSendController {
    private final ClientModel model;
    private Stage stage;
    private final List<String> defaultRecipients;
    private final String defaultSubject;
    private final String defaultBody;

    @FXML
    private TextField subjectBox;
    @FXML
    private TextField recipientsBox;
    @FXML
    private TextArea bodyBox;
    @FXML
    private Button sendMail;

    /**
     * Constructor fo the "Default" sending mode
     * @param model application model
     */
    public MailSendController(ClientModel model) {
        this(model, List.of(""), "", "");
    }

    /**
     * Constructor for the replyALL, reply and forward sending modes
     * @param model application model
     */
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

        sendMail.setOnAction(e -> sendMail());

        subjectBox.setText(defaultSubject);
        recipientsBox.setText(String.join(", ", defaultRecipients)); // Follows sendMail method's protocol
        bodyBox.setText(defaultBody);

        stage.show();
    }

    private void sendMail() {
        String subject = subjectBox.getText();
        List<String> recipients = Arrays.asList(recipientsBox.getText().trim().split("\\s*,\\s*"));
        String body = bodyBox.getText();

        sendMail.setDisable(true);

        new Thread(() -> {
            try{
                model.sendMail(recipients, subject, body);
                Platform.runLater(() -> { // UI operations run by JavaFX thread
                    try{
                        new PopupManager().showView(stage, "Mail sent!", " To : " + recipients.toString(),"Notification");
                        stage.close();
                    } catch (IOException e) {
                        System.err.println("Could not open pop-up " + e.getMessage());
                        e.printStackTrace();
                        stage.close();
                    }
                });
            }
            catch(MailException e){
                Platform.runLater(() -> { // UI operations run by JavaFX thread
                    sendMail.setDisable(false);
                    try{
                        new PopupManager().showView(stage, "Could not send mail!", e.getMessage(), "ERROR");
                    } catch (IOException x) {
                        System.err.println("Could not open pop-up following mail sending errors" + x.getMessage());
                        x.printStackTrace();
                        stage.close();
                    }
                });
            }
        }).start();


    }
}
