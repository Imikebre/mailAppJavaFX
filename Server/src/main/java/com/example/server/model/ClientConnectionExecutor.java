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
 * Executes a Client request and closes the connection afterward
 *
 * <p> This executor handles the network communication and relies on the main model to perform the following operations:
 * <ul>
 * <li>{@link ServerModel#checkUserExists(String)}</li>
 * <li>{@link ServerModel#getUserMailbox(String, boolean)}</li>
 * <li>{@link ServerModel#sendMail(Email)}</li>
 * <li>{@link ServerModel#deleteMail(String, int)}</li>
 * </ul>
 * Request Protocol is structured as follows :
 * <ul>
 *     <li>REQUEST_TYPE</li>
 *     <li>requestor_user_mail</li>
 * </ul>
 * </p>
 */
class ClientConnectionExecutor implements Runnable {
    private Socket incoming;
    private ServerModel model;
    private long threadId;

    ClientConnectionExecutor(Socket socket,ServerModel model) {
        this.incoming = socket;
        this.model = model;
    }

    @Override
    public void run() {
        threadId = Thread.currentThread().getId();

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

    /**
     * <p>
     *     Login protocol :
     *     <ul>
     *         <li>
     *             Additional sends by client
     *             <ul>
     *                 <li>None</li>
     *             </ul>
     *         </li>
     *         <li>
     *             Server Reply
     *             <ul>
     *                 <li> OK : in case of success </li>
     *                 <li> ERR : in case of non success </li>
     *             </ul>
     *         </li>
     *     </ul>
     * </p>
     */
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
    /**
     * <p>
     * Update protocol:
     * <ul>
     * <li>
     * Server Reply:
     * <ul>
     * <li> {@code START} : marks the beginning of the mailbox data </li>
     * <li>
     * For each email in the mailbox, the following 6 lines are sent:
     * <ul>
     * <li> {@code Sender} </li>
     * <li> {@code Recipients} </li>
     * <li> {@code Subject} </li>
     * <li> {@code Body} (with newlines escaped as {@code \\n}) </li>
     * <li> {@code SentDate} </li>
     * <li> {@code ID} </li>
     * </ul>
     * </li>
     * <li> {@code END} : marks the end of the mailbox data </li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     * @param out         the PrintWriter connected to the client socket
     * @param userMailbox the list of emails to be sent to the client
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    private void updateRequest(PrintWriter out , ArrayList<Email> userMailbox) throws IOException {

        out.println("START");

        if(userMailbox.isEmpty()){
            out.println("END");
        }
        else{
            for(Email email : userMailbox){
                out.println(email.getSender());
                out.println(email.getRecipients());
                out.println(email.getSubject());
                out.println(email.getBody().replace("\n", "\\n"));
                out.println(email.getSentDate());
                out.println(email.getId());
            }

            out.println("END");
        }

        model.setLogString(threadId + " - Completed request with result : \"OK\"");
    }

    /**
     * Handles the server-side protocol for receiving and processing a client's request to send an email.
     * <p>
     * Send Mail protocol:
     * <ul>
     * <li>
     * Client Request format:
     * <ul>
     * <li> {@code START} : marks the beginning of the request </li>
     * <li> {@code Sender} : the email address of the sender </li>
     * <li> {@code Recipients} : comma-separated list of recipient email addresses </li>
     * <li> {@code Subject} : the subject of the email </li>
     * <li> {@code Body} : the body of the email (with newlines escaped as {@code \n}) </li>
     * <li> {@code END} : marks the end of the request </li>
     * </ul>
     * </li>
     * <li>
     * Server Reply:
     * <ul>
     * <li> {@code OK} : if the email was successfully processed and sent </li>
     * <li> {@code ERR} followed by an error message : if the protocol is violated, a keyword is missing, or a {@link ModelException} occurs </li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param out the {@link PrintWriter} used to send the response back to the client
     * @param in  the {@link Scanner} used to read the incoming request lines from the client socket
     * @throws IOException if an I/O error occurs while communicating over the socket
     */
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

    /**
     * Handles the server-side protocol for receiving and processing a client's request to delete an email.
     * <p>
     * Delete Mail protocol:
     * <ul>
     * <li>
     * Client Request format:
     * <ul>
     * <li> {@code START} : marks the beginning of the request </li>
     * <li> {@code Mailbox Account} : the email address of the account owning the mailbox </li>
     * <li> {@code Email ID} : the unique integer identifier of the email to be deleted </li>
     * <li> {@code END} : marks the end of the request </li>
     * </ul>
     * </li>
     * <li>
     * Server Reply:
     * <ul>
     * <li> {@code OK} : if the email was successfully deleted </li>
     * <li> {@code ERR} followed by an error message : if the protocol is violated, the ID is invalid, or a {@link ModelException} occurs </li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param out the {@link PrintWriter} used to send the response back to the client
     * @param in  the {@link Scanner} used to read the incoming request lines from the client socket
     * @throws IOException if an I/O error occurs while communicating over the socket
     */
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
                model.deleteMail(mailboxAccount, Integer.parseInt(id)); // Throws number format exception
                out.println("OK");
                model.setLogString(threadId + " - Completed request with result : \"OK\"");
            }
            catch (ModelException | NumberFormatException e){
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
