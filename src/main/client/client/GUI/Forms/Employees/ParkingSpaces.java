package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import entity.ParkingLot;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ParkingSpaces implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private TreeTableView<?> tableMain;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO: TEST: POPULATE WITH CURRENT PARKING LOT
        ParkingLot TEMPDUMMY = CPSClientGUI.getSession().getParkingLot();
        for (int h = 0; h < TEMPDUMMY.getHeight(); h++) //Get all spaces in a floor
        {

        }
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }


}
