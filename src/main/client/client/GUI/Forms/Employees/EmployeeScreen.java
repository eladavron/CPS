package client.GUI.Forms.Employees;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import entity.Session;
import entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployeeScreen implements Initializable{

    @FXML
    private Button btnManageParkingSpaces;

    @FXML
    private TitledPane paneCS;

    @FXML
    private Button btnParkingLotInit;

    @FXML
    private TitledPane paneSpaces;

    @FXML
    private VBox employeeRoot;

    @FXML
    private TitledPane paneParkinglot;

    @FXML
    private TitledPane paneManagement;

    @FXML
    private Button btnParkingLotStatus;

    private Session _session;

    private User.UserType _type;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (CPSClientGUI.getSession() != null) {
            _session = CPSClientGUI.getSession();
            _type = _session.getUserType();
        }

        String status;
        switch (_type)
        {
            case EMPLOYEE:
                status = "an employee in this branch";
                employeeRoot.getChildren().remove(paneManagement);
                break;
            case MANAGER:
                status = "the manager of this branch";
                break;
            case SUPERMAN:
                //TODO: Add company manager panel
                status = "the general manager of this company";
                break;
            default:
                status = "not supposed to be here...";
                //TODO: Maybe throw them out?
        }
        CPSClientGUI.setStatus(String.format("Logged in as %s to %s.\nYou are %s.",
                _session.getUser().getName(),
                _session.getParkingLot().getLocation(),
                status), Color.GREEN);
    }

    @FXML
    void handleEmployeeButton(ActionEvent event) {
        throw new NotImplementedException(((Button) event.getSource()).getText());
    }
}