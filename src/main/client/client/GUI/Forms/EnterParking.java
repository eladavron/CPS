package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.DateTimeCombo;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Inits;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Validation;
import entity.Message;
import entity.Order;
import entity.PreOrder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * The controller for entering a parking session.
 */
public class EnterParking implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<PreOrder> cmbOrder;

    @FXML
    private ComboBox<String> cmbExitMinute;

    @FXML
    private Button btnCreate;

    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private DatePicker exitDate;

    @FXML
    private ComboBox<Integer> cmbCar;

    private DateTimeCombo _exitDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Inits.initCars(cmbCar);
                Inits.initOrders(cmbOrder);
            }
        });
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
        if (cmbCar.getSelectionModel().getSelectedIndex() < 0) //Makes sure car ID is selected
        {
            Validation.showError(cmbCar, "Please select a valid car number!");
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
        Integer parkingLotNumber = CPSClientGUI.getSession().getParkingLot().getParkingLotID();
        Order newOrder = new Order(CPSClientGUI.getSession().getUser().getUID(), cmbCar.getValue(),exitTime,parkingLotNumber);
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
        cmbCar.getSelectionModel().select(selectedOrder.getCarID());
        _exitDateTime.setDateTime(selectedOrder.getEstimatedExitTime());
        validateForm();
    }

    @FXML
    void returnToMain(ActionEvent event) throws IOException {
        CPSClientGUI.goBack(true);
    }
}
