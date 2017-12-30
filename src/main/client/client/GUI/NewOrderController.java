package client.GUI;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Elad Avron
 */
public class NewOrderController implements Initializable {

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
    void checkAvailability(ActionEvent event) {
        if (cmbParkingLot.getValue() == null) //Makes sure parking lot is selected
        {
            lblAvailability.setTextFill(Color.RED);
            lblAvailability.setText("Please select parking lot!");
            btnCreate.setDisable(true);
        }
        else if (txtCarID.getText().equals("")) //Makes sure car ID is entered. //TODO: Make sure car ID is valid.
        {
            lblAvailability.setTextFill(Color.RED);
            lblAvailability.setText("Please enter a valid car number!");
            btnCreate.setDisable(true);
        }
        else if (entryDate.getValue() == null || exitDate.getValue() == null) //Makes sure dates and times are selected
        {
            // TODO: IMPORTANT: check validity with server
            // TODO: Check for minimum of 1 hour parking
            lblAvailability.setTextFill(Color.RED);
            lblAvailability.setText("Please select valid dates for entry and exit");
            btnCreate.setDisable(true);
        }
        else
        {
            lblAvailability.setTextFill(Color.GREEN);
            lblAvailability.setText("Available!");
            btnCreate.setDisable(false);
        }
    }

    @FXML
    void createOrder(ActionEvent event) {
        int exitHour = cmbExitHour.getSelectionModel().getSelectedIndex();
        int exitMinute = cmbExitMinute.getSelectionModel().getSelectedIndex();
        Date exitTime = Helpers.getDateFromLocalDate(exitDate.getValue(), exitHour, exitMinute);
        Order newOrder = new Order("John Doe", Integer.valueOf(txtCarID.getText()), exitTime); //TODO: Get Name from User

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(newOrder);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Go back to main menu?");
        alert.setContentText("Your order will not be saved and any data you entered will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        {
            CPSClientGUI.getInstance().changeGUI("CustomerScreen.fxml");
        }
    }


}
