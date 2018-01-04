package client.GUI;

import Exceptions.NotImplementedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

public class CustomerScreenController {

    @FXML
    private Button btnCreateOrder;

    @FXML
    private Button btnEnterParking;

    @FXML
    private Button btnFileComplaint;

    @FXML
    private Button btnCheckComplaint;

    @FXML
    private Button btnExitParking;

    @FXML
    private Button btnManageSubs;

    @FXML
    private Button btnEditOrder;

    @FXML
    void handleCustomerButton(ActionEvent event) throws IOException {

        String filename = "";
        if (event.getSource() == btnEnterParking)
        {
            filename = "EnterParking.fxml";
        }
        else if (event.getSource() == btnCreateOrder)
        {
            filename = "NewPreorder.fxml";
        }
        else
        {
            throw new NotImplementedException(event.getSource().toString());
        }
        CPSClientGUI.getInstance().changeGUI(filename);
    }


    static void backToMain() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Go back to main menu?");
        alert.setContentText("Any data you entered will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        {
            CPSClientGUI.getInstance().changeGUI("CustomerScreen.fxml");
        }
    }
}
