package com.example.client;

import com.example.client.exceptions.MailException;
import com.example.client.model.ClientModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michele Brescia
 */
public class MailAppTest {

    static class Counter{
        AtomicInteger count = new AtomicInteger(0);
        public void increment(){
            count.incrementAndGet();
        }
        public int get(){
            return count.get();
        }
    }

    /**
     * Aiding class to load JAVAFX toolkit, necessary to use some of the Client Model data structures
     * Invoking this results in blocking the current thread
     */
    public static class FXApp extends Application {
        @Override
        public void start(Stage stage) {} // Empty to prevent any UI window from opening while the toolkit runs in background
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> Application.launch(FXApp.class)).start();
        Thread.sleep(1000); // Giving time to the JavaFX thread to initialize

        int N_TEST_USERS = 10;
        List<String> users = new ArrayList<>();

        for (int i = 0; i <= N_TEST_USERS; i++) // Defining a list of test users
            users.add(i + "@" + i + ".com");

        List<Thread> threads = new ArrayList<>();
        int N_SESSIONS_PER_USER = 2; // Number of multiple sessions launched for every user

        Counter sendCount = new Counter();

        for (int s = 0; s < N_SESSIONS_PER_USER; s++) {  // Loops N_SESSIONS
            for (String user : users) { // Loops N users x N_SESSIONS
                List<String> otherUsers = new ArrayList<>(users);
                otherUsers.remove(user);
                Thread t = new Thread(new UserTest(user, otherUsers, sendCount));
                threads.add(t);
                t.start();
            }
        }

        for (Thread t : threads) // waits for test Threads to finish
            t.join();

        int numUsers = users.size();
        int theoreticalSends = N_SESSIONS_PER_USER * numUsers * ((numUsers - 1) * 2 + 1);

        System.out.println("Test completed");
        System.out.println("Mail sent " + sendCount.get() + " Against a theoretical amount of " + theoreticalSends);
        Platform.exit();
        System.exit(0);
    }
}

/**
 * Test class to test concurrence while sending, requesting updates and deleting emails
 * @author Michele Brescia
 */
class UserTest implements Runnable{
    String user;
    List<String> users;
    MailAppTest.Counter sendCount;

    /**
     * Constructs a new UserTest instance for a specific user.
     * @param user user's mail
     * @param users others test users' mails
     */
    public UserTest(String user, List<String> users, MailAppTest.Counter sendCount) {
        this.user = user;
        this.users = users;
        this.sendCount = sendCount;
    }

    @Override
    public void run(){
        ClientModel model = new ClientModel();
        model.setUserMail(user);

        Thread t1 = new Thread(() -> {
            for(int i = 0; i < users.size() ; i++)
                try{
                    model.sendMail(List.of(users.get(i)), "Mail to " + i + " by thread " + Thread.currentThread(), "Test");
                    sendCount.increment();
                }catch(MailException e){
                    System.err.println(e.getMessage());
                }
        });

        Thread t2 = new Thread(() -> {
            for(int i = 0; i < users.size() ; i++)
                try{
                    model.sendMail(List.of(users.get(i)), "Mail to " + i + " by thread " + Thread.currentThread(), "Test");
                    sendCount.increment();
                }catch(MailException e){
                    System.err.println(e.getMessage());
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
            sendCount.increment();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }


    }
}
/**
 * @author Michele Brescia
 */
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