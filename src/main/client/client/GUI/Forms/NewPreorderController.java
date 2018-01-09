package client.GUI.Forms;


import client.GUI.CPSClientGUI;
import client.GUI.Helpers.DateTimeCombo;
import client.GUI.Helpers.Common;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.RunnableWithMessage;
import entity.Message;
import entity.ParkingLot;
import entity.PreOrder;
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
public class NewPreorderController implements Initializable {

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
    private TextField txtCarID;

    @FXML
    private Button btnCreate;

    @FXML
    private Label lblAvailability;

    private DateTimeCombo _entryDateTime;
    private DateTimeCombo _exitDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Common.initParkingLots(cmbParkingLot);
        _entryDateTime = new DateTimeCombo(entryDatePicker, cmbEntryHour, cmbEntryMinute);
        _exitDateTime = new DateTimeCombo(exitDatePicker, cmbExitHour, cmbExitMinute);
    }

    private boolean validateForm() {
        Common.clearAllHighlighted();
        boolean validate = true;
        if (cmbParkingLot.getValue() == null) //Makes sure parking lot is selected
        {
            Common.showError(cmbParkingLot, "Please select parking lot!");
            validate = false;
        }
        if (!txtCarID.getText().matches("\\d{7,8}")) //Makes sure car ID is entered and valid
        {
            Common.showError(txtCarID, "Please enter a valid car number!");
            validate = false;
        }

        //Validate times:
        return Common.validateTimes(_entryDateTime, _exitDateTime) && validate;
    }

    @FXML
    void createOrder(ActionEvent event) {
        if (!validateForm())
            return;

        WaitScreen waitScreen = new WaitScreen();
        Date entryTime = _entryDateTime.getDateTime();
        Date exitTime = _exitDateTime.getDateTime();
        int parkingLotNumber = cmbParkingLot.getSelectionModel().getSelectedItem().getUID();
        PreOrder newOrder = new PreOrder(CPSClientGUI.getCurrentUser().getUID(), Integer.valueOf(txtCarID.getText()), exitTime, parkingLotNumber , 0.0, entryTime); //TODO: Figure out charge
        Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.PREORDER, newOrder);
        RunnableWithMessage onSuccess = new RunnableWithMessage() {
            @Override
            public void run() {
                PreOrder preorder = (PreOrder) getIncoming().getData().get(0);
                waitScreen.showSuccess("Order created successfully!", "Order details:\n" +
                        "Customer ID: " + preorder.getCostumerID() + "\n" +
                        "Order ID: " + preorder.getOrderID() +"\n" +
                        "Parking Lot ID: " + preorder.getParkingLotNumber() +"\n" +
                        "Parking Start: " + preorder.getEntryTime() + "\n" +
                        "Estimated Exit: " + preorder.getEstimatedExitTime());
                waitScreen.redirectOnClose(CPSClientGUI.CUSTOMER_SCREEN);
            }
        };
        RunnableWithMessage onFailure = new RunnableWithMessage() {
            @Override
            public void run() {
                waitScreen.showError("Order failed!", (String)getIncoming().getData().get(0));
            }
        };
        MessageTasker createOrder = new MessageTasker("Sending order...",
                "Reserving...",
                "Order created!",
                "Order failed!",
                newMessage,
                onSuccess,
                onFailure);
        waitScreen.run(createOrder, 10);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.backToMain();
    }


}
