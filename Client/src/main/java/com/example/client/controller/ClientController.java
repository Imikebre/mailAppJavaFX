package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.client.utility.MailPreviewCell;
import com.example.client.utility.PopupManager;
import com.example.common.Email;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Main client application controller.
 * @author Michele Brescia
 */
public class ClientController {
    @FXML
    public Label mailfield;
    @FXML
    private ListView<Email> listView;
    @FXML
    private Button sendMail;
    /** Displays a message if no mails are present in the user's inbox */
    @FXML
    private Label emptyLabel;
    /** Displays the icon if the client is connected to the server */
    @FXML
    private ImageView connectedIcon;
    /** Displays the icon if the client is connected to the server */
    @FXML
    private ImageView disconnectedIcon;
    /** Only Model used in the client application */
    private ClientModel model;
    /**
     * Flags the end of the setup routine in order to avoid receiving notifications for already present mails.
     * True if setup routine is completed
     * False if setup routine is not completed
     */
    private SimpleBooleanProperty setupProperty = new SimpleBooleanProperty(false);
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setModel(ClientModel model) {
        this.model = model;
        mailfield.setText(model.getUserMail());
        emptyLabel.visibleProperty().bind(model.mailIsEmptyProperty());
        connectedIcon.visibleProperty().bind(model.getIsConnectedProperty());
        disconnectedIcon.visibleProperty().bind(connectedIcon.visibleProperty().not());
        setupProperty.bind(model.getSetupProperty());
    }

    public void startClient(){
        try{
            model.updateMailBox("true"); // Get-all mode
            model.startPolling();
        }catch(MailException e){
            try{
                new PopupManager().showView(stage, "Couldn't setup mailbox", e.getMessage(), "ERROR");
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

        sendMail.setOnAction(e -> { new MailSendController(model); });

        model.getSelectedMailProperty().addListener((ListChangeListener<Email>) c -> {
            while(c.next())
                if (c.wasAdded()) { new MailViewDetails(model, c.getAddedSubList().getFirst()); }
        });

        setupMailNotification();
        setupMailPreviewCell();
    }

    private void setupMailNotification() {
        model.getAllMail().addListener((ListChangeListener<Email>) c -> {
            while(c.next())
                if (c.wasAdded() && setupProperty.getValue()) {
                    List<? extends Email> newMails =  c.getAddedSubList();
                        try{
                            if (newMails.size() == 1)  // shows only the sender
                                new PopupManager().showView(new Stage(), "New mail!", "New mail from: " + newMails.getFirst().getSender(),"Notification");
                            else if (newMails.size() > 1) // Shows how many mails have been received
                                new PopupManager().showView(new Stage(), "New mails!", "You received " + newMails.size() + " new emails.", "Notification");
                        }catch(IOException ex){
                            System.err.println(ex.getMessage());
                        }
                }
        });
    }

    private void setupMailPreviewCell() {
        listView.setItems(model.getAllMail());
        listView.setCellFactory(list -> new MailPreviewCell(model));
    }

}
