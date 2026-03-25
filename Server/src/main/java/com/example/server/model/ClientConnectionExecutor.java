package com.example.server.model;

import com.example.server.model.exceptions.ModelException;
import com.example.common.Email;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Calls model methods :
 * - OK model.checkUserExists(message)
 * - OK model.getUserMailbox(message,mode)
 * - OK model.sendMail(new Email(sender, recipientsList, subject, body));
 * - model.deleteMail(mailboxAccount, Integer.parseInt(id));
 */
class ClientConnectionExecutor implements Runnable {
    private Socket incoming;
    private ServerModel model;
    private long threadId = Thread.currentThread().getId();

    ClientConnectionExecutor(Socket socket,ServerModel model) {
        this.incoming = socket;
        this.model = model;
    }

    @Override
    public void run() {
        try{
            model.setLogString(threadId + " - Handling request ...");

            Scanner in = new Scanner(incoming.getInputStream());
            PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

            String message = in.nextLine().trim();

            switch (message){
                case "LOGIN":
                    message = in.nextLine().trim();
                    model.setLogString(threadId + " - Request Type = LOGIN");
                    model.setLogString(threadId + " - Requested login from user : " + message);
                    loginRequest(out, model.checkUserExists(message));
                    break;
                case "SEND":
                    message = in.nextLine().trim();
                    model.setLogString(threadId + " - Request Type = SEND");
                    model.setLogString(threadId + " - Requested send from user : " + message);
                    sendMailRequest(out, in);
                    break;
                case "UPDATE" :
                    boolean mode = Boolean.parseBoolean(in.nextLine().trim());
                    message = in.nextLine().trim();
                    model.setLogString(threadId + " - Request Type = UPDATE");
                    model.setLogString(threadId + " - Requested setup from user : " + message );
                    try{
                        updateRequest(out, model.getUserMailbox(message,mode));
                    } catch (ModelException e) {
                        out.println("ERR");
                        out.println(e.getMessage());
                        model.setLogString(threadId + " - Completed request with result : \"ERR\"" + e.getMessage());
                    }
                    break;
                case "DELETE":
                    message = in.nextLine().trim();
                    model.setLogString(threadId + " - Request Type = DELETE");
                    model.setLogString(threadId + " - Requested delete from user : " + message);
                    deleteMailRequest(out, in);
                    break;
                default:
                    out.println("ERR");
                    out.println("Code request not recognised");
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loginRequest(PrintWriter out ,boolean authResult) throws IOException {
       if(authResult){
           out.println("OK");
           model.setLogString(threadId + " - Completed request with result : \"OK\"");
       }
       else{
           out.println("ERR");
           out.println("Authentication Failed, insert a mail address associated with an existing user");
           model.setLogString(threadId + " - Completed request with result : \"ERR\"");
       }
    }

    private void updateRequest(PrintWriter out , ArrayList<Email> userMailbox) throws IOException {

        out.println("START");

        if(userMailbox.isEmpty()){
            out.println("END");
        }

        for(Email email : userMailbox){
            out.println(email.getSender());
            out.println(email.getRecipients());
            out.println(email.getSubject());
            out.println(email.getBody().replace("\n", "\\n"));
            out.println(email.getSentDate());
            out.println(email.getId());
        }

        out.println("END");
        model.setLogString(threadId + " - Completed request with result : \"OK\"");
    }

    private void sendMailRequest(PrintWriter out, Scanner in) throws IOException {

        if(!in.nextLine().equals("START")){
            out.println("ERR");
            out.println("Server could not send mail request : START keyword missing");
            model.setLogString(threadId + " - Completed request with result : \"ERR\"");
            return;
        }

        String sender = in.nextLine();
        String recipients = in.nextLine();
        String subject = in.nextLine();
        String body = in.nextLine().replace("\\n", "\n");

        List<String> recipientsList = Arrays.stream(recipients.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        model.setLogString(threadId + " - Request to send mail :" +
                "\n\t From: " + sender +
                "\n\t To: " + recipients +
                "\n\t Subject: " + subject +
                "\n\t Body: " + body);

        if(in.nextLine().equals("END"))
            try{
                model.sendMail(new Email(sender, recipientsList, subject, body));
                out.println("OK");
                model.setLogString(threadId + " - Completed request with result : \"OK\"");
            }
            catch (ModelException e){
                out.println("ERR");
                out.println(e.getMessage());
                model.setLogString(threadId + " - Completed request with result : \"ERR\"");
            }
        else{
            out.println("ERR");
            out.println("Server could not send mail request : END keyword missing");
            model.setLogString(threadId + " - Completed request with result : \"ERR\"");
        }

    }

    private void deleteMailRequest(PrintWriter out, Scanner in) throws IOException {
        if(!in.nextLine().equals("START")){
            out.println("ERR");
            out.println("Server could not delete mail request : START keyword missing");
            model.setLogString(threadId + " - Completed request with result : \"ERR\"");
            return;
        }

        String mailboxAccount = in.nextLine();
        String id = in.nextLine();

        if(in.nextLine().equals("END"))
            try{
                model.deleteMail(mailboxAccount, Integer.parseInt(id));
                out.println("OK");
            }
            catch (ModelException e){
                out.println("ERR");
                out.println(e.getMessage());
                model.setLogString(threadId + " - Completed request with result : \"ERR\"");
            }
        else {
            out.println("ERR");
            out.println("Server could not delete mail request : END keyword missing");
            model.setLogString(threadId + " - Completed request with result : \"ERR\"");
        }
    }

}
