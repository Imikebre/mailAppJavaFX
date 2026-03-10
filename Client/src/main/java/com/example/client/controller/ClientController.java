package com.example.client.controller;

import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ClientController {
    @FXML
    public Label mailfield;
    @FXML
    private ListView<Email> listView;
    @FXML
    private Button sendMail;
    @FXML
    private Label emptyLabel;

    private ClientModel model;

    public void setModel(ClientModel model) {
        this.model = model;

        model.getSelectedMailProperty().addListener((ListChangeListener<Email>) c -> {
            c.next();
            if (c.wasAdded()) {
                new MailViewDetails(model, c.getAddedSubList().get(0));
            }
        });

        sendMail.setOnAction(e -> {
            new MailSendController(model);
        });

        mailfield.setText(model.getUserMail());
        emptyLabel.visibleProperty().bind(model.mailIsEmptyProperty());

        listView.setItems(model.getAllMail());
        listView.setCellFactory(list -> new MailPreviewCell(model));
    }

}
