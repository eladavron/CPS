package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ParkingLotViewController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ParkingSpaces implements Initializable {

//    @FXML
//    private Button btnApply;
//
//    @FXML
//    private Button btnRevert;


    @FXML
    private Button btnBack;


    @FXML
    private TabPane tabMain;

    private SimpleBooleanProperty _dirty = new SimpleBooleanProperty(false);

    private ParkingLotViewController _controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        btnApply.disableProperty().bindBidirectional(_dirty);
//        btnRevert.disableProperty().bindBidirectional(_dirty);
        _controller = new ParkingLotViewController(CPSClientGUI.getSession().getParkingLot(), tabMain);
        _controller.init();
    }


    @FXML
    void applyChanges(ActionEvent event) {

    }

    @FXML
    void revertChanges(ActionEvent event) {

    }

    private void setDirty(boolean dirty)
    {
        _dirty.set(dirty);
    }

    private boolean isDirty()
    {
        return _dirty.get();
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(isDirty());
    }


}
