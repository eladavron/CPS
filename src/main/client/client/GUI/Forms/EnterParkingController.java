package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.DateTimeCombo;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Queries;
import client.GUI.Helpers.Validation;
import entity.Message;
import entity.Order;
import entity.ParkingLot;
import entity.PreOrder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * The controller for entering a parking session.
 */
public class EnterParkingController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<PreOrder> cmbOrder;

    @FXML
    private ComboBox<String> cmbExitMinute;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLot;

    @FXML
    private Button btnCreate;

    @FXML
    private TextField txtCarID;

    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private DatePicker exitDate;

    private DateTimeCombo _exitDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Queries.initParkingLots(cmbParkingLot, false);
        Queries.initOrders(cmbOrder);
        cmbOrder.valueProperty().addListener((observable, oldValue, newValue) -> fillOrder());
        _exitDateTime = new DateTimeCombo(exitDate, cmbExitHour, cmbExitMinute);
    }

    /**
     * Validates the form. Highlights any problematic fields and displays an error message if fails.
     * @return true if form is valid, false otherwise.
     */
    private boolean validateForm() {
        Validation.clearAllHighlighted();
        boolean validate = true;
        if (cmbParkingLot.getValue() == null) //Makes sure parking lot is selected
        {
            Validation.showError(cmbParkingLot, "Please select parking lot!");
            validate = false;
        }
        if (!txtCarID.getText().matches("\\d{7,8}")) //Makes sure car ID is entered and valid
        {
            Validation.showError(txtCarID, "Please enter a valid car number!");
            validate = false;
        }
        return Validation.validateTimes(null, _exitDateTime) && validate;
    }



    /**
     * A new parking session is created by creating an "Order".
     * @param event the action of clicking the "Enter Parking" button.
     */
    @FXML
    void enterParking(ActionEvent event) {
        if (!validateForm())
            return;

        WaitScreen waitScreen = new WaitScreen();
        Date exitTime = _exitDateTime.getDateTime();
        Integer parkingLotNumber = cmbParkingLot.getSelectionModel().getSelectedItem().getParkingLotID();
        Order newOrder = new Order(CPSClientGUI.getSession().getUser().getUID(), Integer.valueOf(txtCarID.getText()),exitTime,parkingLotNumber);
        newOrder.setOrderID(0);
        Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.ORDER, newOrder);

        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Order order = (Order) getMessage().getData().get(0);
                waitScreen.showSuccess("Car Parked!", "Order details:\n" +
                        "Customer ID: " + order.getCostumerID() + "\n" +
                        "Order ID: " + order.getOrderID() +"\n" +
                        "Parking Lot ID: " + order.getParkingLotNumber() +"\n" +
                        "Parking Start: " + order.getEntryTime() + "\n" +
                        "Estimated Exit: " + order.getEstimatedExitTime());
                waitScreen.redirectOnClose(CPSClientGUI.CUSTOMER_SCREEN);
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Parking Failed!", getErrorString());
            }
        };
        MessageTasker createOrder = new MessageTasker("Attempting to park...",
                "Checking availability...",
                "Parking successful!",
                "Parking failed!",
                newMessage,
                onSuccess,
                onFailure);
        waitScreen.run(createOrder, 10);
    }

    /**
     * Fills the order once selected in the drop down menu
     */
    private void fillOrder() {
        PreOrder selectedOrder = cmbOrder.getValue();
        cmbParkingLot.getSelectionModel().select(selectedOrder.getParkingLotNumber()); //TODO: Should select actual object!
        txtCarID.setText(selectedOrder.getCarID().toString());
        _exitDateTime.setDateTime(selectedOrder.getEstimatedExitTime());
        validateForm();

    }

    @FXML
    void returnToMain(ActionEvent event) throws IOException {
        CPSClientGUI.goBack(true);
    }
}
