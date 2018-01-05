package client.GUI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Message;
import entity.Order;
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

public class EnterParkingController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox cmbOrder;

    @FXML
    private ComboBox cmbExitMinute;

    @FXML
    private Button btnCheckAvailable;

    @FXML
    private ComboBox cmbParkingLot;

    @FXML
    private Button btnCreate;

    @FXML
    private TextField txtCarID;

    @FXML
    private Label lblAvailability;

    @FXML
    private ComboBox cmbExitHour;

    @FXML
    private DatePicker exitDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnBack.setTooltip(new Tooltip("Back"));
        cmbParkingLot.getItems().clear();
        cmbParkingLot.getItems().addAll("Temporary Sample 1", "Temporary Sample 2");//TODO: Populate from database!

        cmbExitMinute.getItems().clear();
        for (int i = 0; i < 60; i++) //Minute Fill
        {
            cmbExitMinute.getItems().add((i<10 ? "0" : "") + i);
        }

        cmbExitHour.getItems().clear();
        for (int i = 0; i < 24; i++) //Hour Fill
        {
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
        if (exitDate.getValue() == null) //Makes sure dates and times are selected
        {
            // TODO: IMPORTANT: check validity with server
            Helpers.showError(exitDate, "Please select valid dates for exit!");
            validate = false;
        }
        if (validate) //Form valid, now check times
        {
            Date entry = new Date();
            Date exit = Helpers.getDateFromControls(exitDate, cmbExitHour, cmbExitMinute);
            if (exit.before(entry)) //Exit date is before entry
            {
                Helpers.showError(cmbExitMinute, "You can't exit in the past!");
                Helpers.highlightControl(cmbExitHour);
                Helpers.highlightControl(exitDate);
                validate = false;
            } else if (TimeUnit.HOURS.convert((exit.getTime() - entry.getTime()), TimeUnit.MILLISECONDS) < 1) {
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

    /**
     * A new parking session is created by creating an "Order".
     * @param event the action of clicking the "Enter Parking" button.
     */
    @FXML
    void enterParking(ActionEvent event) {
        Date exitTime = Helpers.getDateFromControls(exitDate, cmbExitHour, cmbExitMinute);
        Order newOrder = new Order(0, Integer.valueOf(txtCarID.getText()),exitTime,0);

        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(newOrder);
            Message newMessage = new Message(Message.MessageType.CREATE, Message.DataType.ORDER, data);
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
