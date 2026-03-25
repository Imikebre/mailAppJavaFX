package com.example.client.model;
import com.example.client.exceptions.MailException;
import com.example.common.Email;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Core model for the Mail Client application.
 * <p>
 * This class serves as the central data store for the client-side application,
 * managing the list of emails and UI-bound properties. It also handles all
 * network communications with the remote server using a custom socket protocol.
 * </p>
 * @author Michele Brescia
 */
public class ClientModel {
    private static final int serverPort = 1430;
    private static final String ip = "127.0.0.1";

    private String mailAddress=""; // user mail address
    private final ObservableList<Email> mails = FXCollections.observableArrayList(); // mails stored as incoming and note deleted
    private final ObservableList<Email> selectedMails = FXCollections.observableArrayList(); // mails open in view mode
    private final SimpleBooleanProperty emptyProperty = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty connectedProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty setupProperty = new SimpleBooleanProperty(false);

    public ClientModel() {}

    public void setUserMail(String mailAddress) {
        if(this.mailAddress.isEmpty())
            this.mailAddress = mailAddress;
    }

    public String getUserMail() { return this.mailAddress; }
    public SimpleBooleanProperty getSetupProperty() { return this.setupProperty; }
    public SimpleBooleanProperty mailIsEmptyProperty() { return emptyProperty; }
    public SimpleBooleanProperty getIsConnectedProperty() { return connectedProperty; }
    public ObservableList<Email> getSelectedMailProperty() {
        return this.selectedMails;
    }

    public ObservableList<Email> getAllMail() {
        return this.mails;
    }

    public void deleteMail(Email email) throws MailException {
        if(selectedMails.contains(email))
            throw new MailException("Mail is opened in view mode, please close the window before deleting");

        forceDeleteMail(email);
    }

    /**
     * @Warning This method is used to the JavaFX App
     */
    public void TestDeleteAllMails() throws MailException {
        List<Email> snap;

        synchronized (this.mails) {
            snap = new ArrayList<>(this.mails);
        }

        for (Email email : snap) {
            forceDeleteMail(email);
        }
    }

    /**
     * Deletes a mail even if it is in a "selected state"
     * @param email
     * @throws MailException
     */
    public void forceDeleteMail(Email email) throws MailException{

        try ( Socket socket = new Socket(ip, serverPort) ){
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("DELETE");
            out.println(mailAddress);
            out.println("START");
            out.println(mailAddress);
            out.println(email.getId());
            out.println("END");

            String message = in.nextLine();
            if(message.equals("OK")){
                Platform.runLater(()->{
                    this.mails.remove(email);

                    if(mails.isEmpty())
                        emptyProperty.setValue(true);
                });
            }
            else if (message.equals("ERR"))
                throw new MailException(in.nextLine());
            else
                throw new MailException("Could not interpret server message" + message);
        }
        catch (IOException  e){
            throw new MailException("Could not connect to the server : " + e.getMessage());
        }
    }

    public void selectMail(Email email) {
        Platform.runLater(()->{
            if (selectedMails.contains(email))
                return;
            selectedMails.add(email);
        });
    }

    public void deselectMail(Email email) {
        Platform.runLater(()->{
            selectedMails.remove(email);
        });
    }

    public void updateMailBox(String mode) throws MailException {
        try ( Socket socket = new Socket(ip, serverPort) ){

            Platform.runLater(()->{
                if(!connectedProperty.getValue())
                    connectedProperty.setValue(true);
            });

            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("UPDATE");
            out.println(mode);
            out.println(mailAddress);

            String message = in.nextLine();

            if(message.equals("ERR")){
                if (mode.equals("true")) { Platform.runLater(() -> setupProperty.setValue(true)); }
                throw new MailException(in.nextLine());
            }

            if(message.equals("START")){

                message = in.nextLine();

                if(message.equals("END")){
                    if (mode.equals("true")) { Platform.runLater(() -> setupProperty.setValue(true)); }
                    return;
                }

                while(!message.equals("END")){
                    final String sender;
                    final String recipients;
                    final String subject;
                    final String body;
                    final String sentDate;
                    final long id;

                    sender = message;
                    recipients = in.nextLine();
                    subject = in.nextLine();
                    body = in.nextLine().replace("\\n", "\n");
                    sentDate = in.nextLine();
                    id = Long.parseLong(in.nextLine());
                    message = in.nextLine();

                    List<String> recipientsList = Arrays.asList(recipients.split(","));

                    Platform.runLater(() ->{
                        mails.add(new Email(sender, recipientsList, subject, body, sentDate, id));
                        emptyProperty.setValue(mails.isEmpty());
                    });
                }

                if (mode.equals("true")) { Platform.runLater(() -> setupProperty.setValue(true)); }
            }
            else{
                throw new MailException("Could not interpret server message" + message);
            }

        }
        catch (IOException  e){
            if(connectedProperty.getValue())
                connectedProperty.setValue(false);
            if (mode.equals("true")) { Platform.runLater(() -> setupProperty.setValue(true)); }
            throw new MailException("Could not connect to the server : " + e.getMessage());
        }
    }

    public void sendMail (List<String> recipients, String subject, String body) throws MailException{

        String validationResult = validateMail(recipients, subject, body);

        if(!validationResult.isEmpty())
            throw new MailException(validationResult);

        try ( Socket socket = new Socket(ip, serverPort) ){
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("SEND");
            out.println(mailAddress);
            out.println("START");
            out.println(mailAddress);
            out.println(String.join(", ", recipients));
            out.println(subject);
            out.println(body.replace("\n", "\\n"));
            out.println("END");

            String message = in.nextLine();
            if(message.equals("OK"))
                return;
            else if (message.equals("ERR"))
                throw new MailException(in.nextLine());
            else
                throw new MailException("Could not interpret server message" + message);
        }
        catch (IOException  e){
            throw new MailException("Could not connect to the server : " + e.getMessage());
        }
    }

    private String validateMail(List<String> recipients, String subject, String body){
        String errorMessage = "";

        if(recipients.isEmpty() || (recipients.size() == 1 && recipients.getFirst().isEmpty())){
            errorMessage = errorMessage + "Recipient address is empty\n";
        }
        else{
            if(recipients.stream().distinct().count() != recipients.size())
                errorMessage = errorMessage + "Duplicate recipients found\n";

            for(String recipient : recipients) {
                if(!recipient.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")){
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

    public void isValidAddress(String mail) throws MailException{
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if( !mail.matches(regex))
            throw new MailException("Please insert a valid mail address : example@at.mail.com");

        try ( Socket socket = new Socket(ip, serverPort) ){
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("LOGIN");
            out.println(mail);

            if(in.nextLine().equals("ERR"))
                throw new MailException(in.nextLine());
        }
        catch (IOException  e){
            throw new MailException("Could not connect to the server : " + e.getMessage());
        }
    }

    public void startPolling() {
        Thread pollingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    updateMailBox("false");
                } catch (InterruptedException e) {
                    break;
                }
                catch (MailException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}
