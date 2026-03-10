package com.example.server;

import com.example.common.Email;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerModel {
    private final Map<String, MailAccount> users = new HashMap<>();
    private long idCounter = 0;
    private final List<Long> idAvailable = new ArrayList<>();
    private final String mailBoxesDir = System.getProperty("user.home") + "/mailAppJavaBresciaP3/data/mailBoxes";
    private final String dataDir = System.getProperty("user.home") + "/mailAppJavaBresciaP3/data";


    public ServerModel(){
        users.put("mario@example.com", new MailAccount("mario@example.com", new ArrayList<>(List.of(
                new Email("anna@example.com", List.of("mario@example.com"), "Ciao Mario!", "Come stai? Ci vediamo domani?", LocalDateTime.now()),
                new Email("boss@example.com", List.of("mario@example.com"), "Meeting lunedì", "Ricordati del meeting alle 9.", LocalDateTime.now().minusDays(1))
        ))));

        users.put("anna@example.com", new MailAccount("anna@example.com", new ArrayList<>(List.of(
                new Email("mario@example.com", List.of("anna@example.com"), "Re: Ciao!", "Sto bene grazie, ci vediamo domani!", LocalDateTime.now()),
                new Email("boss@example.com", List.of("anna@example.com"), "Progetto", "Come procede il progetto?", LocalDateTime.now().minusHours(3))
        ))));

        users.put("boss@example.com", new MailAccount("boss@example.com", new ArrayList<>(List.of(
                new Email("mario@example.com", List.of("boss@example.com"), "Aggiornamento", "Il progetto è in linea con i tempi.", LocalDateTime.now()),
                new Email("anna@example.com", List.of("boss@example.com"), "Ottimo lavoro", "Complimenti per i risultati!", LocalDateTime.now().minusDays(2))
        ))));
    }

    public void registerUser(String username){ users.put(username, new MailAccount(username)); }

    public void deleteUser(String username){ users.remove(username); }

    public void deleteMail(Email email){
        MailAccount user = users.get(email.getSender());

        idAvailable.add(email.getId());
        user.removeMailFromBox(email);
    }

    public void sendMail(Email email) {
        List<String> usersNotRegistered = mailAdressesCheck(email);

        if ( usersNotRegistered.isEmpty() ){
            if(idAvailable.isEmpty()){
                email.setId(idCounter);
                idCounter++;
            }
            else{
                email.setId(idAvailable.getFirst());
                idAvailable.removeFirst();
            }
            email.setSentDate(LocalDateTime.now());

            return; // TODO send mail
        }

        return; // TODO send error message
    }

    private void createUserFolder(String mailAddress){
        File dir = new File(mailBoxesDir + mailAddress);
        dir.mkdirs(); // crea anche le cartelle intermedie
    }

    /*private void saveMailToFile(Email email){
        File file = new File(mailBoxesDir + email.getSender(), "mail" + email.getId() + ".json");
        FileWriter fw = new FileWriter(file);
        Gson gson = new Gson();
        fw.write(email.toJson(email));
        fw.close();
    }*/ // TODO

    private List<String> mailAdressesCheck(Email email) {
        List<String> usersNotRegistered = new ArrayList<>();
        List<String> mails = email.getRecipientsList();
        mails.add(email.getSender());

        for( String mail : mails )
            if ( !users.containsKey(mail))
                usersNotRegistered.add(mail);

        return usersNotRegistered;
    }

    private void saveMailBox(){

    }
}
