package com.example.common;

import java.time.LocalDateTime;
import java.util.List;

public class Email {
    private String sender;
    private List<String> recipients;
    private String subject;
    private String body;
    private boolean read = false;
    private long id;
    private LocalDateTime sentDate;

    public Email(String sender, List<String> recipients, String subject, String body, LocalDateTime sentDate) {
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
    }

    public Email(String sender, List<String> recipients, String subject, String body) {
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
    }

    public boolean isRead() {return read;}
    public void setRead() {this.read = true;}

    public void setId(long id) {this.id = id;}
    public long getId() {return id;}
    public LocalDateTime getSentDate() {return sentDate;}
    public void setSentDate(LocalDateTime sentDate) {this.sentDate = sentDate;}

    public String getSender() {return sender;}
    public String getRecipients() { return String.join(", ", recipients);}
    public String getSubject() {return subject;}
    public String getBody() {return body;}

    public List<String> getRecipientsList() {return recipients;}

    @Override
    public String toString() {
        return"Sender: "+sender+"\nRecipient: "+ getRecipients() + "\nSubject: "+subject+"\nBody: "+body;
    }
}