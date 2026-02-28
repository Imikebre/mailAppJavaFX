package com.example.client.model;
import com.example.common.Email;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientModel {
    private final SimpleStringProperty mailAddress = new SimpleStringProperty();
    private final ObservableList<Email> mails;

    public ClientModel() {
        this.mails = FXCollections.observableArrayList();
    }

    public SimpleStringProperty getMailProperty() {
        return this.mailAddress;
    }

    public void addMail(Email email) {
        this.mails.addFirst(email);
    }

    public void removeMail(Email email) {
        this.mails.remove(email);
    }

    public ObservableList<Email> getAllMail() {
        return this.mails;
    }

    public void sendMail(String recipient, String ccs, String subject, String body) {

    }

    public void getMailsFromFile(ObservableList<Email> mails) {
        this.mails.addAll(mails);
    }
}
