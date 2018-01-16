package client.GUI.Forms.Customers;


import client.GUI.CPSClientGUI;
import client.GUI.Controls.DateTimeCombo;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Inits;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Validation;
import entity.Message;
import entity.ParkingLot;
import entity.PreOrder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * A controller for creating new Preorders.
 * @author Elad Avron
 */
public class NewPreorder implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private DatePicker entryDatePicker;

    @FXML
    private DatePicker exitDatePicker;

    @FXML
    private ComboBox<String> cmbEntryMinute;

    @FXML
    private ComboBox<String> cmbExitMinute;

    @FXML
    private ComboBox<String> cmbEntryHour;

    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLot;

    @FXML
    private ComboBox<Integer> cmbCarID;

    @FXML
    private Button btnCreate;

    @FXML
    private Label lblAvailability;

    private DateTimeCombo _entryDateTime;
    private DateTimeCombo _exitDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Inits.initCars(cmbCarID);
                Inits.initParkingLots(cmbParkingLot);
            }
        });
        _entryDateTime = new DateTimeCombo(entryDatePicker, cmbEntryHour, cmbEntryMinute);
        _exitDateTime = new DateTimeCombo(exitDatePicker, cmbExitHour, cmbExitMinute);
    }

    /**
     * Validate the form
     * @return true if valid, false if not.
     */
    private boolean validateForm() {
        Validation.clearAllHighlighted();
        /*
         If full subscription, do not allow parking for more than 14 days.
         */
        return Validation.validateTimes(_entryDateTime, _exitDateTime)
                && Validation.notEmpty(cmbParkingLot, cmbCarID)
                && Validation.validateParkingLength(_entryDateTime,_exitDateTime, cmbCarID.getValue());
    }

    /**
     * Create the order and send it to the server
     * @param event the button click event.
     */
    @FXML
    void createOrder(ActionEvent event) {
        if (!validateForm())
            return;

        WaitScreen waitScreen = new WaitScreen();
        Date entryTime = _entryDateTime.getDateTime();
        Date exitTime = _exitDateTime.getDateTime();
        int parkingLotNumber = cmbParkingLot.getSelectionModel().getSelectedItem().getParkingLotID();
        PreOrder newOrder = new PreOrder(CPSClientGUI.getLoggedInUserID(), cmbCarID.getValue(), exitTime, parkingLotNumber , 0.0, entryTime); //TODO: Figure out charge
        newOrder.setOrderID(0);
        Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.PREORDER, newOrder);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                PreOrder preorder = (PreOrder) getMessage().getData().get(0);
                waitScreen.showSuccess("Order created successfully!", preorder.toGUIString());
                waitScreen.setOnClose(new Runnable() {
                    @Override
                    public void run() {
                        CPSClientGUI.goBack(false);
                    }
                });
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Order failed!", getErrorString());
            }
        };
        MessageTasker createOrder = new MessageTasker(newMessage, onSuccess, onFailure, "Reserving...");
        waitScreen.run(createOrder, 10);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }


}
