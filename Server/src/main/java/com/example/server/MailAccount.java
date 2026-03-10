package com.example.server;

import com.example.common.Email;

import java.util.ArrayList;
import java.util.List;

public class MailAccount {
    private String mailAddress;
    private List<Email> mailBox;
    private List<Email> inBox;

    public MailAccount(String mailAddress, List<Email> mailBox) {
        this.mailAddress = mailAddress;
        this.mailBox = mailBox;
        inBox = new ArrayList<>();
    }
    public MailAccount(String mailAddress) {
        this.mailAddress = mailAddress;
        this.mailBox = new ArrayList<>();
        inBox = new ArrayList<>();
    }

    public String getMailAddress() { return mailAddress; }
    public List<Email> getMailBox() { return mailBox; }
    public void addMailToBox(Email mailBox) { this.mailBox.add(mailBox); }
    public void removeMailFromBox(Email mailBox) { this.mailBox.remove(mailBox); }

    public boolean authenticate(String mailAddress ) { return mailAddress.equals(this.mailAddress); }

}
