package com.example.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Email {
    private final String sender;
    private final List<String> recipients;
    private final  String subject;
    private final String body;
    private long id;
    private String sentDate;

    public Email(String sender, List<String> recipients, String subject, String body, String sentDate, long id) {
        this.sender = sender;
        this.recipients = new ArrayList<>( new HashSet<>(recipients)); // Removes duplicates but could result in a modification of the original order
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
        this.id = id;
    }

    public Email(String sender, List<String> recipients, String subject, String body) {
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
        this.subject = subject;
        this.body = body;
    }

    public void setId(long id) {this.id = id;}
    public long getId() {return id;}
    public String getSentDate() {return sentDate;}
    public void setSentDate(String sentDate) {this.sentDate = sentDate;}

    public String getSender() {return sender;}
    public String getRecipients() { return String.join(", ", recipients);}
    public String getSubject() {return subject;}
    public String getBody() {return body;}

    public List<String> getRecipientsList() {
        return new ArrayList<>(this.recipients);
    }

    @Override
    public String toString() {
        return"Sender: "+sender+"\nRecipient: "+ getRecipients() + "\nSubject: "+subject+"\nBody: "+body;
    }
}