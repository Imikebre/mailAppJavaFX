package com.example.client.utility;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * A utility class responsible for creating and displaying modal popup windows.
 * It handles both standard notifications and error alerts based on the specified type,
 * blocking the underlying user interface until the popup is closed by the user.
 * @author Michele Brescia
 */
public class PopupManager {
    @FXML
    Label infoList;
    @FXML
    Button button;

    public  PopupManager() {}

    public void showView(Stage parentStage, String title, String info, String alertType) throws IOException {
        FXMLLoader loader;
        if(alertType.equals("ERROR"))
            loader = new FXMLLoader(PopupManager.class.getResource("/com/example/client/popup.fxml"));
        else
            loader = new FXMLLoader(PopupManager.class.getResource("/com/example/client/notification.fxml"));

        loader.setController(this);
        Parent root = loader.load(); // fxml root

        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL); // Blocks other scenes
        alertStage.initOwner(parentStage);
        alertStage.setAlwaysOnTop(true);
        alertStage.setScene(new Scene(root));
        alertStage.setTitle(title);

        if(!info.isEmpty()) this.infoList.setText(info);
        else{
            this.infoList.visibleProperty().setValue(false);
            this.infoList.setManaged(false);
        }

        button.setOnAction(e -> {alertStage.close();});
        alertStage.showAndWait(); // Block FX thread until the window is closed
    }
}
