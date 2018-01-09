package client.GUI.Forms;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * A controller for the main customer screen.
 */
public class CustomerScreenController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (CPSClientGUI.getCurrentUser() != null)
            CPSClientGUI.setStatus("Logged in as " + CPSClientGUI.getCurrentUser().getName(), Color.GREEN);
    }

    /**
     * Handles clicking any button in the Customer screen.
     * @param event the click event.
     */
    @FXML
    void handleCustomerButton(ActionEvent event) {

        String filename;
        if (event.getSource() == btnEnterParking) {
            filename = CPSClientGUI.ENTER_PARKING;
        } else if (event.getSource() == btnCreateOrder) {
            filename = CPSClientGUI.NEW_PREORDER;
        } else {
            throw new NotImplementedException(event.getSource().toString());
        }
        CPSClientGUI.changeGUI(filename);
    }
}