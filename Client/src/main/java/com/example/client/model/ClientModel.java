package com.example.client.model;
import com.example.client.exceptions.MailException;
import com.example.common.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ClientModel {
    private String mailAddress=""; // user mail address
    private final ObservableList<Email> mails = FXCollections.observableArrayList(); // mails stored as incoming and note deleted
    private final ObservableList<Email> selectedMails = FXCollections.observableArrayList(); // mails open in view mode

    public ClientModel() {}

    public void setUserMail(String mailAddress) {
        if(this.mailAddress.isEmpty())
            this.mailAddress = mailAddress;
    }

    public String getUserMail() { return this.mailAddress; }

    public ObservableList<Email> getSelectedMailProperty() {
        return this.selectedMails;
    }

    public ObservableList<Email> getAllMail() {
        return this.mails;
    }

    public void addMail(Email email) {
        this.mails.addFirst(email);
    }
    public void deleteMail(Email email) {
        this.mails.remove(email);
    }

    public void selectMail(Email email) {
        if (selectedMails.contains(email))
            return;
        selectedMails.add(email);
    }
    public void deselectMail(Email email) { selectedMails.remove(email); }


    public void getMailsFromFile(ObservableList<Email> mails) {
        this.mails.addAll(mails);
    }

    public void sendMail (List<String> recipients, String subject, String body) throws MailException{

        String validationResult = validateMail(recipients, subject, body);

        if(!validationResult.isEmpty())
            throw new MailException(validationResult);
        else{
            Email outGoingMail = new Email(mailAddress,recipients, subject, body);
            System.out.println(outGoingMail);
        }
    }

    private String validateMail(List<String> recipients, String subject, String body){
        String errorMessage = "";

        if(recipients.isEmpty() || (recipients.size() == 1 && recipients.getFirst().isEmpty())){
            errorMessage = errorMessage + "Recipient address is empty\n";
        }
        else{
            for(String recipient : recipients) {
                if(!isValidAddress(recipient)){
                    errorMessage = errorMessage + "Invalid mail address in one or more recipients\n";
                    break;
                }
            }
        }
        if(subject.isBlank())
            errorMessage = errorMessage + "Subject is empty\n";
        if(body.isBlank())
            errorMessage = errorMessage + "Body is empty";

        return errorMessage;
    }

    public boolean isValidAddress(String mail){
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return mail.matches(regex);
    }
}
