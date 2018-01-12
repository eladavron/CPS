package client.GUI.Forms;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import entity.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
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

    @FXML
    private TitledPane paneParking;

    @FXML
    private VBox customerRoot;

    private Session _session;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (CPSClientGUI.getSession() != null)
            _session = CPSClientGUI.getSession();

        CPSClientGUI.setStatus("Logged in as " + CPSClientGUI.getSession().getUser().getName()
                    + " to " + CPSClientGUI.getSession().getParkingLot().getLocation(), Color.GREEN);

        if (_session.getParkingLot().getUID() == -1) //Remote login
        {
            customerRoot.getChildren().remove(paneParking);
        }
    }

    /**
     * Handles clicking any button in the Customer screen.
     * @param event the click event.
     */
    @FXML
    void handleCustomerButton(ActionEvent event) throws NotImplementedException {
        String filename;
        if (event.getSource() == btnEnterParking) {
            filename = CPSClientGUI.ENTER_PARKING;
        } else if (event.getSource() == btnCreateOrder) {
            filename = CPSClientGUI.NEW_PREORDER;
        } else if (event.getSource() == btnExitParking) {
            ExitParking.exitParkingStart();
            return;
        } else if (event.getSource() == btnEditOrder){
            filename = CPSClientGUI.VIEW_PREORDERS;
        }

        else {
            //TODO: Handle?
            throw new NotImplementedException(event.getSource().toString());
        }
        CPSClientGUI.changeGUI(filename);
    }




}