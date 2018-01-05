package client.GUI;

import com.fasterxml.jackson.core.JsonProcessingException;
import entity.Message;
import entity.Order;
import entity.ParkingLot;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class EnterParkingController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<Order> cmbOrder;

    @FXML
    private ComboBox<String> cmbExitMinute;

    @FXML
    private Button btnCheckAvailable;

    @FXML
    private ComboBox<ParkingLot> cmbParkingLot;

    @FXML
    private Button btnCreate;

    @FXML
    private TextField txtCarID;

    @FXML
    private Label lblAvailability;

    @FXML
    private ComboBox<String> cmbExitHour;

    @FXML
    private DatePicker exitDate;

    private DateTimeCombo _exitDateTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        Helpers.initParkingLots(cmbParkingLot);
        Helpers.initOrders(cmbOrder);
        cmbOrder.valueProperty().addListener(new ChangeListener<Order>() {
                                                 @Override
                                                 public void changed(ObservableValue<? extends Order> observable, Order oldValue, Order newValue) {
                                                     fillOrder(null);
                                                 }
                                             }
        );
        _exitDateTime = new DateTimeCombo(exitDate, cmbExitHour, cmbExitMinute);
    }

    @FXML
    boolean validateForm(ActionEvent event) {
        Helpers.clearAllHighlighted();
        boolean validate = true;
        if (cmbParkingLot.getValue() == null) //Makes sure parking lot is selected
        {
            Helpers.showError(cmbParkingLot, "Please select parking lot!");
            validate = false;
        }
        if (!txtCarID.getText().matches("\\d{7,8}")) //Makes sure car ID is entered and valid
        {
            Helpers.showError(txtCarID, "Please enter a valid car number!");
            validate = false;
        }
        validate = Helpers.validateTimes(null, _exitDateTime) && validate;
        if (validate)
        {
            //TODO: IMPORTANT: Check actual availability with Server
            lblAvailability.setTextFill(Color.GREEN);
            lblAvailability.setText("Available!");
            btnCreate.setDisable(false);
            return true;
        }
        else
        {
            lblAvailability.setTextFill(Color.RED);
            lblAvailability.setText("Please fix form and try again.");
            btnCreate.setDisable(true);
            return false;
        }
    }

    /**
     * A new parking session is created by creating an "Order".
     * @param event the action of clicking the "Enter Parking" button.
     */
    @FXML
    void enterParking(ActionEvent event) {
        if (!validateForm(event))
            return;
        Date exitTime = _exitDateTime.getDateTime();
        Order newOrder = new Order(0, Integer.valueOf(txtCarID.getText()),exitTime,0);

        try {
            Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.ORDER, newOrder);
            Helpers.sendToServer(newMessage);
        }
        catch (JsonProcessingException je)
        {
            lblAvailability.setText("An error occurred!");
            lblAvailability.setTextFill(Color.RED);
        }
    }

    /**
     * Fills an order based on a selection.
     * @param event
     */
    @FXML
    void fillOrder(ActionEvent event) {
        Order selectedOrder = cmbOrder.getValue();
        if (selectedOrder == null) //Clear
        {
            //TODO: Clear Form
        } else
        {
            cmbParkingLot.getSelectionModel().select(selectedOrder.getParkingLotNumber()); //TODO: Should select actual object!
            txtCarID.setText(selectedOrder.getCarID().toString());
            _exitDateTime.setDateTime(selectedOrder.getEstimatedExitTime());
            validateForm(event);
        }

    }

    @FXML
    void returnToMain(ActionEvent event) throws IOException {
        CustomerScreenController.backToMain();
    }
}
