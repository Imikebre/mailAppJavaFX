package com.example.server.model;

import com.example.common.Email;
import com.example.server.model.exceptions.ModelException;
import com.google.gson.Gson;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the data persistence layer for the email server.
 * <p>
 * This class handles saving and restoring the server state, including
 * global metadata (ID counters) and individual user mailboxes, using
 * JSON serialization via the Gson library.
 * </p>
 *
 * @author Michele Brescia
 */
class StorageManager {
    private final String mailBoxesDir = System.getProperty("user.home") + "/mailAppJavaBresciaP3/data/mailBoxes/";
    private final String dataDir = System.getProperty("user.home") + "/mailAppJavaBresciaP3/data";
    private final Gson gson = new Gson();

    String getDataDir(){
        return dataDir;
    }

    void saveState(StorageData data) throws ModelException {
        saveDataToFile(data);
    }

    void saveMailToFile(String owner, Email mail) throws ModelException {
         File file = new File(mailBoxesDir + owner, "mail" + mail.getId() + ".json");

         try{
             saveObjectToFile(file, mail);
         } catch (IOException e) {
             throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED, "Could not save mail to file");
         }
    }

    void saveDataToFile(StorageData data) throws ModelException {
         File file = new File(dataDir + "/ServerDataModelInstance.json");

         try{
             saveObjectToFile(file, data);
         } catch (IOException e) {
             throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED, "Could not save data to file");
         }
    }

    StorageData restoreServerState(String fileRegex) throws ModelException {
        File file = new File(dataDir + "/ServerDataModelInstance.json");

        try{
            StorageData data =  restoreObjectFromFile(file, StorageData.class);

            data.users = new ArrayList<>();
            File dir = new File(mailBoxesDir );

            File[] users = dir.listFiles();

            if(users == null) return data;

            for (File f : users) {
                if(f.getName().matches(fileRegex)) {
                    data.users.add(f.getName());
                    System.out.println("user: " + f.getName());
                }
            }
            return data;
        } catch (FileNotFoundException e) {
            throw new ModelException(ModelException.ErrorCode.FILE_NOT_FOUND, "");
        }catch (IOException e) {
            throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED, "Could not restore data from file");
        }
    }

    ArrayList<Email> restoreMailBoxFromFile(String owner, List<Long> ids) throws ModelException {
        File dir = new File(mailBoxesDir + "/" + owner);
        File[] files = dir.listFiles();
        ArrayList<Email> mails = new ArrayList<>();

        if(files == null)
            throw new ModelException(ModelException.ErrorCode.USER_NOT_FOUND, "");

        for (File file : files) {
            try  {
                if(ids == null || ids.contains(Long.parseLong(file.getName().replace("mail", "").replace(".json", ""))))
                    mails.add(restoreObjectFromFile(file, Email.class));
            }
            catch (FileNotFoundException e) { // In case of file deletion
                continue;
            }
            catch (IOException e) {
                throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED, "Could not restore Email from file");
            }
        }
        return mails;
    }

    void deleteMailFromFile(String owner, int id) throws ModelException {
         File file = new File(mailBoxesDir + owner, "mail" + id + ".json");

         if(! file.delete() )
             throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED,"Could not delete file: " + file.getName() + "\n");
    }

    void createUserFolder(String mailAddress) throws ModelException {
         File dir = new File(mailBoxesDir + mailAddress);

         if( ! dir.mkdirs())
             throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED,"Could not create mailbox directory");
    }

    static class StorageData {
        transient ArrayList<String> users; // Won't be serialized by Gson
        long idCounter;

        StorageData( long id) {
            idCounter = id;
        }
    }

    private <T> T restoreObjectFromFile(File file, Class<T> clazz) throws IOException {
        try (FileReader fr = new FileReader(file)) {
            return gson.fromJson(fr, clazz);
        }
    }

    private void saveObjectToFile(File file, Object obj) throws IOException {
        try ( FileWriter fw = new FileWriter(file) ){ // Try-With-Resources will autoclose
            fw.write(gson.toJson(obj));
        }
    }
}
