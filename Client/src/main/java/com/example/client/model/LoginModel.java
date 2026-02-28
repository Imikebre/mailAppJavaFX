package com.example.client.model;

import javafx.beans.property.SimpleStringProperty;

public class LoginModel {
    private final SimpleStringProperty mail = new SimpleStringProperty();

    public SimpleStringProperty getMailProperty(){
        return mail;
    }

    public boolean validateAddress(String mailAddress){
        if(mailAddress != null)
            if(mailAddress.contains("@"))
                if(mailAddress.contains(".")){
                    mail.setValue(mailAddress);
                    return true;
                }
        return false;
    }
}
