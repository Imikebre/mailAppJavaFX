package com.example.client.controller;

import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ClientController {
    @FXML
    public Label mailfield;
    @FXML
    private VBox mailListContainer;

    ClientModel model;

    public void setModel(ClientModel model) {
        this.model = model;
        mailfield.setText(model.getMailProperty().getValue());
        // carica le mail già presenti
        aggiornaLista(model);

        // ascolta i cambiamenti futuri
        model.getAllMail().addListener((ListChangeListener<Email>) change -> {
            aggiornaLista(model);
        });
    }

    private void aggiornaLista(ClientModel model) {
        mailListContainer.getChildren().clear();
        model.getAllMail().forEach(email -> {
            Label label = new Label(email.getSender() + " - " + email.getSubject());
            mailListContainer.getChildren().add(label);
        });
    }

}
