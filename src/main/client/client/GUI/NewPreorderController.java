package client.GUI;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Message;
import entity.PreOrder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * A controller for creating new Preorders.
 * @author Elad Avron
 */
public class NewPreorderController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox cmbEntryMinute;

    @FXML
    private ComboBox cmbExitMinute;

    @FXML
    private ComboBox cmbEntryHour;

    @FXML
    private ComboBox cmbParkingLot;

    @FXML
    private TextField txtCarID;

    @FXML
    private Button btnCreate;

    @FXML
    private Label lblAvailability;

    @FXML
    private ComboBox cmbExitHour;

    @FXML
    private DatePicker exitDate;

    @FXML
    private DatePicker entryDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        cmbParkingLot.getItems().clear();
        cmbParkingLot.getItems().addAll("Temporary Sample 1", "Temporary Sample 2");//TODO: Populate from database!

        cmbEntryMinute.getItems().clear();
        cmbExitMinute.getItems().clear();
        for (int i = 0; i < 60; i++) //Minute Fill
        {
            cmbEntryMinute.getItems().add((i<10 ? "0" : "") + i);
            cmbExitMinute.getItems().add((i<10 ? "0" : "") + i);
        }

        cmbExitHour.getItems().clear();
        cmbEntryHour.getItems().clear();
        for (int i = 0; i < 24; i++) //Hour Fill
        {
            cmbEntryHour.getItems().add((i<10 ? "0" : "") + i);
            cmbExitHour.getItems().add((i<10 ? "0" : "") + i);
        }
    }

    @FXML
    void validateForm(ActionEvent event) {
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
        if (entryDate.getValue() == null)
        {
            Helpers.showError(entryDate, "Please select valid dates for entry!");
            validate = false;
        }
        if (exitDate.getValue() == null) //Makes sure dates and times are selected
        {
            Helpers.showError(exitDate, "Please select valid dates for exit!");
            validate = false;
        }
        if (validate) //Form valid, now check times
        {
            Date entry = Helpers.getDateFromControls(entryDate,cmbEntryHour, cmbEntryMinute);
            Date exit = Helpers.getDateFromControls(exitDate, cmbExitHour, cmbExitMinute);
            if (exit.before(entry)) //Exit date is before entry
            {
                Helpers.showError(cmbExitMinute, "You can't exit before you enter!");
                Helpers.highlightControl(cmbExitHour);
                Helpers.highlightControl(exitDate);
                validate = false;
            }
            else if (TimeUnit.HOURS.convert((exit.getTime() - entry.getTime()), TimeUnit.MILLISECONDS) < 1)
            {
                Helpers.showError(cmbExitMinute, "You can't park for less than 1 hour!");
                Helpers.highlightControl(cmbExitHour);
                Helpers.highlightControl(exitDate);
                validate = false;
            }
        }
        if (validate)
        {
            //TODO: IMPORTANT: Check actual availability with Server
            lblAvailability.setTextFill(Color.GREEN);
            lblAvailability.setText("Available!");
            btnCreate.setDisable(false);
        }
        else
        {
            lblAvailability.setTextFill(Color.RED);
            lblAvailability.setText("Please fix form and try again.");
            btnCreate.setDisable(true);
        }
    }

    @FXML
    void createOrder(ActionEvent event) {
        Date entryTime = Helpers.getDateFromControls(entryDate, cmbEntryHour, cmbEntryMinute);
        Date exitTime = Helpers.getDateFromControls(exitDate, cmbExitHour, cmbExitMinute);
        PreOrder newOrder = new PreOrder(0, Integer.valueOf(txtCarID.getText()), exitTime, 0, 0.0, entryTime); //TODO: Get Name from User, get Parking Lot from form, yada yada

        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(newOrder);
            Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.PREORDER, data);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(newMessage);
            CPSClientGUI.getInstance().sendToServer(json);
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
