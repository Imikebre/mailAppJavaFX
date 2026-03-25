package com.example.client;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import com.example.common.Email;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailAppTest {
    public static class FXApp extends Application {
        @Override
        public void start(Stage stage) {} // non fa nulla
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> Application.launch(FXApp.class)).start();
        Thread.sleep(1000); // aspetta che si avvii

        int N_TEST_USERS = 10;

        List<String> users = new ArrayList<>();

        for (int i = 0; i <= N_TEST_USERS; i++)
            users.add(i + "@" + i + ".com");

        List<Thread> threads = new ArrayList<>();
        int N_SESSIONS_PER_USER = 2;

        for (int s = 0; s < N_SESSIONS_PER_USER; s++) {
            for (String user : users) {
                List<String> otherUsers = new ArrayList<>(users);
                otherUsers.remove(user);
                Thread t = new Thread(new UserTest(user, otherUsers));
                threads.add(t);
                t.start();
            }
        }

        for (Thread t : threads)
            t.join();

        System.out.println("Tutti i client hanno finito");
    }
}

class UserTest implements Runnable{
    String user;
    List<String> users;

    public UserTest(String user, List<String> users) {
        this.user = user;
        this.users = users;
    }


    @Override
    public void run(){
        ClientModel model = new ClientModel();
        model.setUserMail(user);

        for(int i = 0; i < users.size() ; i++){
            final int x = i;
            Thread t1 = new Thread(() -> {
                try{
                    model.sendMail(List.of(users.get(x)), "Mail to " + x + " by thread " + Thread.currentThread(), "Test");
                }catch(MailException e){
                    e.printStackTrace();
                }
            });

            Thread t2 = new Thread(() -> {
                try{
                    model.sendMail(List.of(users.get(x)), "Mail to " + x + " by thread " + Thread.currentThread(), "Test");
                }catch(MailException e){
                    e.printStackTrace();
                }
            });

            model.startPolling();
            t1.start();
            t2.start();
            try{
                t1.join();
                t2.join();
                Thread.sleep(5000); // waits for mails to be loaded
                model.TestDeleteAllMails();
                model.sendMail(List.of(user), "Self", "test");
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }
}

class MinimalStorageManager {
    static int N_TEST_USERS = 10;

    public static void main(String[] args) {
        for(int i = 0; i <= N_TEST_USERS; i++){
            try{
               createUserFolder(i + "@" + i + ".com");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    static void createUserFolder(String mailAddress) throws MailException {
        final String mailBoxesDir = System.getProperty("user.home") + "/mailAppJavaBresciaP3/data/mailBoxes/";

        File dir = new File(mailBoxesDir + mailAddress);

        if( ! dir.mkdirs())
            throw new MailException("could not create user folder");
    }
}