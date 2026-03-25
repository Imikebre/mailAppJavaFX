package com.example.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a single email entity within the mail system.
 * <p>
 * This class serves as a Data Transfer Object (DTO) shared between the client and the server.
 * It encapsulates all necessary information for an email,
 * content, and server-side metadata like unique identifiers and timestamps.
 * </p>
 * <p>
 * <b>Design Considerations:</b>
 * <ul>
 * <li> <b>Duplicate Handling:</b> The primary constructor automatically filters duplicate
 * recipient addresses using a {@link java.util.HashSet}. </li>
 * <li> <b>Thread Safety & Encapsulation:</b> The {@link #getRecipientsList()} method returns
 * a defensive copy of the recipients list, preventing external callers from modifying
 * the internal state of the email object. </li>
 * </ul>
 * </p>
 * @author Michele Brescia
 */

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