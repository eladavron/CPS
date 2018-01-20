package client.GUI.Forms.Employees;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.ReportUtils;
import entity.Message;
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

import static client.GUI.CPSClientGUI.*;
import static entity.Message.DataType.PARKING_LOT;
import static entity.Message.DataType.PARKING_LOT_IMAGE;
import static entity.Message.MessageType.CREATE;
import static entity.Message.MessageType.QUERY;

/**
 * The main screen for Employees.
 * Dynamically adds and removes panels based on access levels.
 * Regular Employees will only see the "Employee Controls" Panel.
 * Branch Managers will ALSO see the "Manager" Panel.
 * A Customer Service employee will only see the "Employee Controls" and "Customer Service" Panels.
 * The Company Manager will see ALL panels.
 */
public class EmployeeScreen extends GUIController implements Initializable{

    @FXML
    private Button btnManageParkingSpaces;

    @FXML
    private TitledPane paneCS;

    @FXML
    private Button btnParkingLotInit;

    @FXML
    private Button btnReport;

    @FXML
    private TitledPane paneSuperman;

    @FXML
    private Button btnViewAll;

    @FXML
    private Button btnParkingLotImage;

    @FXML
    private VBox employeeRoot;

    @FXML
    private TitledPane paneParkinglot;

    @FXML
    private TitledPane paneManagement;

   @FXML
    private Button btnManageComplaintCS;

    private Session _session;

    private User.UserType _type;

    /**
     * {@inheritDoc}
     */
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
                employeeRoot.getChildren().removeAll(paneCS, paneSuperman);
                break;
            case CUSTOMER_SERVICE:
                status = "a Customer Service representative";
                employeeRoot.getChildren().removeAll(paneManagement, paneSuperman);
                break;
            case MANAGER:
                status = "the manager of this branch";
                employeeRoot.getChildren().removeAll(paneCS, paneSuperman);
                break;
            case SUPERMAN:
                status = "the general manager of this company";
                break;
            default:
                status = "not supposed to be here...";
        }
        CPSClientGUI.setStatus(String.format("Logged in as %s to %s.\nYou are %s.",
                _session.getUser().getName(),
                _session.getParkingLot().getLocation(),
                status), Color.GREEN);
    }

    /**
     * Handles the "Init Parking Lot" request by employees by sending a request to the server with the currently
     * logged on to Parking Lot ID.
     */
    private void initParkingLot()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message parkingLotInit = new Message(CREATE, PARKING_LOT, CPSClientGUI.getSession().getParkingLot().getParkingLotID());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showSuccess("Parking lot initialized!", "The parking lot has been initialized.", 5);
            };
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskInit = new MessageTasker(parkingLotInit,onSuccess,onFailure,"Initializing...");
        waitScreen.run(taskInit);
    }

    /**
     * Handles the various button click events by redirecting to the relevant GUI screen.
     * @param event The click events.
     */
    @FXML
    void handleEmployeeButton(ActionEvent event) {
        if (event.getSource() == btnManageParkingSpaces)
        {
            CPSClientGUI.changeGUI(PARKING_SPACES, this);
        } else if (event.getSource() == btnReport)
        {
            CPSClientGUI.changeGUI(MANAGE_REPORTS, this);
        }
        else if (event.getSource() == btnManageComplaintCS)
        {
            CPSClientGUI.changeGUI(MANAGE_COMPLAINTS_CS, this);
        }
        else if (event.getSource() == btnParkingLotInit)
        {
            initParkingLot();
        }
        else if (event.getSource() == btnViewAll)
        {
            CPSClientGUI.changeGUI(VIEW_ALL_REPORTS, this);
        }
        else if (event.getSource() == btnParkingLotImage)
        {
            WaitScreen waitScreen = new WaitScreen();
            Message queryImage = new Message(QUERY, PARKING_LOT_IMAGE, CPSClientGUI.getSession().getParkingLot().getParkingLotID());
            MessageRunnable onSuccess = new MessageRunnable() {
                @Override
                public void run() {
                    waitScreen.setOnClose(()-> ReportUtils.showReportPopup((String) getMessage().getData().get(0), "Parking Lot Image:"));
                    waitScreen.hide();
                }
            };
            MessageRunnable onFailure = new MessageRunnable() {
                @Override
                public void run() {
                    waitScreen.showDefaultError(getErrorString());
                }
            };
            MessageTasker taskQueryImage = new MessageTasker(queryImage, onSuccess, onFailure);
            waitScreen.run(taskQueryImage);
        }
        else
        {
            throw new NotImplementedException(((Button) event.getSource()).getText());
        }
    }
}