package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ManageSubscription {

    @FXML
    private Button btnBack;

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }

}
