package com.example.client.controller;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class MailPreviewCell extends ListCell<Email> {
    @FXML
    private Label emailPreview;
    @FXML
    private Label emailSender;
    @FXML
    private Label emailSubject;
    @FXML
    private Button emailDelete;
    @FXML
    private ImageView unreadBadge;
    @FXML
    Label emailDate;

    private final ClientModel model;
    private Parent fxmlroot;

    public MailPreviewCell(ClientModel model) {
        FXMLLoader loader = new FXMLLoader(MailPreviewCell.class.getResource("/com/example/client/email-preview.fxml"));
        loader.setController(this);
        try {
            fxmlroot = loader.load();
        } catch (IOException e) {
            System.err.println("Could not load MailPreviewCell.fxml : " + e.getMessage());
        }

        this.model = model;

        emailDelete.setOnAction(event -> { deleteMail(); event.consume(); });

        setOnMouseClicked(event -> {
                if (getItem() == null) return;
                model.selectMail(getItem());
                getItem().setRead();
                getListView().getSelectionModel().clearSelection(); //removes selection
            });
    }

    @Override
    protected void updateItem(Email item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        emailSender.setText(item.getSender());
        emailSubject.setText(item.getSubject());
        emailPreview.setText(item.getBody().substring(0, Math.min(50, item.getBody().length())));
        if(item.isRead())
            unreadBadge.setVisible(false);
        else
            unreadBadge.setVisible(true);

        if(item.getSentDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            String formatted = item.getSentDate().format(formatter);
            emailDate.setText(formatted);
        }
        else
            emailDate.setVisible(false);

        setGraphic(fxmlroot);
    }

    private void deleteMail() {
        try{
            model.deleteMail(getItem());
        }
        catch(MailException e){
            try{
                new PopupManager(new Stage(), "Could not delete mail", e.getMessage());
            } catch (IOException ex) {
                System.err.println("Could not delete mail: " + ex.getMessage());
            }
        }
    }
}
