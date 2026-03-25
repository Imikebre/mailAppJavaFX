package com.example.server.model;

import com.example.common.Email;
import com.example.server.model.exceptions.ModelException;
import com.example.server.exceptions.ServerModelException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerModel {
    private final StorageManager storageManager = new StorageManager();
    private final Map<String, List<Long>> usersInbox = new ConcurrentHashMap<>();

    private final AtomicLong idCounter = new AtomicLong(0);
    private final SimpleIntegerProperty usersRegistered = new SimpleIntegerProperty(0);

    private final SimpleStringProperty logString = new SimpleStringProperty("");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    Thread serverThread;
    ClientConnectionHandler handler;

    private static final int PORT = 1430;

    private static final String mailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";


    public ServerModel() throws IOException {
        try{
            StorageManager.StorageData storageData = storageManager.restoreServerState(mailRegex);
            for (String user : storageData.users){
                usersInbox.put(user, new ArrayList<>());
            }

            idCounter.set(storageData.idCounter);
            usersRegistered.setValue(usersInbox.size());
        }catch (ModelException e){
            if(e.getCode() == ModelException.ErrorCode.OPERATION_FAILED)
                logString.setValue(e.getMessage());

            logString.setValue("Data not found, proceeding with blank values");
            usersRegistered.setValue(0);
        }
        goLive();
    }

    public SimpleIntegerProperty getUsersRegisteredProperty(){ return usersRegistered; }
    public SimpleStringProperty getLogStringProperty(){ return logString; }
    void setLogString(String logString){
        Platform.runLater(() -> this.logString.setValue(LocalDateTime.now().format(formatter)+ " - " + logString));
    }

    void sendMail(Email email) throws ModelException {
        List<String> usersNotRegistered = mailAddressesCheck(email);

        if ( usersNotRegistered.isEmpty() ){
            email.setId(idCounter.getAndIncrement());

            email.setSentDate(LocalDateTime.now().toString());

            for(String recipient : email.getRecipientsList()){
                storageManager.saveMailToFile(recipient, email);
                List<Long> inbox = usersInbox.get(recipient);

                synchronized(inbox){
                    inbox.add(email.getId());
                }
            }
        }
        else
            throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED, "Invalid recipients : " + String.join(", ", usersNotRegistered));
    }

    private List<String> mailAddressesCheck(Email email) {
        List<String> usersNotRegistered = new ArrayList<>();
        List<String> mails = email.getRecipientsList();
        mails.add(email.getSender());

        for( String mail : mails )
            if ( !usersInbox.containsKey(mail))
                usersNotRegistered.add(mail);

        return usersNotRegistered;
    }

    /** WORKS **/

    public void shutdown(){
        logString.setValue("Shutting down");
        handler.stopServer();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void goLive(){
        logString.setValue("Going live");
        serverThread = new Thread(()->{
            try {
                handler = new ClientConnectionHandler(this, PORT);
                handler.run();
            }
            catch (ModelException e) {
                logString.setValue("Couldn't start ClientConnectionHandler : " + e.getMessage());
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void saveState () throws ServerModelException{
        try{
            shutdown();
            System.out.println("Server saving state");
            storageManager.saveState(new StorageManager.StorageData(idCounter.get()));
        }
        catch (ModelException e){
            logString.setValue(e.getMessage());
            throw new ServerModelException("");
        }
    }

    public void addUser(String mail) throws ServerModelException{

        if(mail.matches(mailRegex)){
            try{ storageManager.createUserFolder(mail); }
            catch (ModelException e){
                logString.setValue(e.getMessage());
                throw new ServerModelException("Couldn't create user");
            }

        }
        else{
            throw new ServerModelException("Invalid mail address");
        }
        usersInbox.put(mail, new ArrayList<>());
        usersRegistered.setValue(usersRegistered.getValue() + 1);
    }

    /**
     *
     * @param user user from which the mailbox should be retrieved
     * @param mode if true retrieves all mails, otherwise only newly arrived
     * @return A list of the mails retrieved
     * @throws ModelException in case of non success
     */

    ArrayList<Email> getUserMailbox(String user, boolean mode) throws ModelException{
        List<Long> param;
        List<Long> inbox = usersInbox.get(user);

        synchronized(inbox) {
            if (mode) {
                param = null;
            } else {
                param = new ArrayList<>(usersInbox.get(user));
                inbox.clear();
            }
        } // Releases Lock to prevent other threads being stuck waiting for storageManager to finish

        return storageManager.restoreMailBoxFromFile(user, param);
    }

    void deleteMail(String owner, int id) throws ModelException{
        synchronized (usersInbox.get(owner)){
            storageManager.deleteMailFromFile(owner, id);
        }
    }

    synchronized boolean checkUserExists(String mailAddress){
        return usersInbox.containsKey(mailAddress);
    }

}


