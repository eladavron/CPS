package client.GUI;


import com.fasterxml.jackson.core.JsonProcessingException;
import entity.Message;
import entity.ParkingLot;
import entity.PreOrder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
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
    private ComboBox <String>cmbExitHour;

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
        Helpers.initParkingLots(cmbParkingLot);
        _entryDateTime = new DateTimeCombo(entryDatePicker, cmbEntryHour, cmbEntryMinute);
        _exitDateTime = new DateTimeCombo(exitDatePicker, cmbExitHour, cmbExitMinute);
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

        //Validate times:
        validate =  Helpers.validateTimes(_entryDateTime, _exitDateTime) && validate;

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

    @FXML
    void createOrder(ActionEvent event) {
        if (!validateForm(event))
            return;

        Date entryTime = _entryDateTime.getDateTime();
        Date exitTime = _exitDateTime.getDateTime();
        PreOrder newOrder = new PreOrder(0, Integer.valueOf(txtCarID.getText()), exitTime, 0, 0.0, entryTime); //TODO: Get Name from User, get Parking Lot from form, yada yada

        try {
            Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.PREORDER, newOrder);
            Helpers.sendToServer(newMessage);
        }
        catch (JsonProcessingException je)
        {
            lblAvailability.setText("An error occurred!");
            lblAvailability.setTextFill(Color.RED);
        }

    }

    @FXML
    void returnToMain(ActionEvent event) throws IOException {
        CustomerScreenController.backToMain();
    }


}
