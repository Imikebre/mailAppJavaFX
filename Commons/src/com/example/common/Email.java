package com.example.common;

public class Email {
    private String sender;
    private String recipient;
    private String ccs;
    private String subject;
    private String body;

    public Email(String sender, String recipient, String ccs, String subject, String body) {
        this.sender = sender;
        this.recipient = recipient;
        this.ccs = ccs;
        this.subject = subject;
        this.body = body;
    }

    public String getSender() {return sender;}
    public String getRecipient() {return recipient;}
    public String getCcs() {return ccs;}
    public String getSubject() {return subject;}
    public String getBody() {return body;}

    @Override
    public String toString() {
        return"Sender: "+sender+"\nRecipient: "+recipient+ "\nCCS : "+ccs+"\nSubject: "+subject+"\nBody: "+body;
    }
}