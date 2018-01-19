package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.*;
import entity.Complaint;
import entity.Message;
import entity.Order;
import entity.ParkingLot;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

import static entity.Message.DataType.COMPLAINT_PRE_CUSTOMER;
import static entity.Message.MessageType.CREATE;

public class NewComplaint extends GUIController implements Initializable {

    //region FXML Decelerations
    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<Order> cmbOrder;

    @FXML
    private Button btnSubmit;

    @FXML
    private TextArea txtDetails;


    @FXML
    private Button btnClear;


    @FXML
    private ComboBox<ParkingLot> cmbParkingLot;
    //endregion

    /**
     * Initialize GUI elements
     * @param location Overridden Parameter
     * @param resources Overridden Parameter
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> {
            Inits.initOrders(cmbOrder);
            Inits.initParkingLots(cmbParkingLot);
            btnClear.disableProperty().bind(cmbOrder.valueProperty().isNull());
            cmbOrder.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != oldValue)
                {
                    for (ParkingLot lot : cmbParkingLot.getItems())
                    {
                        if (lot.getParkingLotID().equals(newValue.getParkingLotNumber()))
                        {
                            cmbParkingLot.setValue(lot);
                            cmbParkingLot.setDisable(true);
                            return;
                        }
                    }
                }
            });
        });
    }

    /**
     * Handles the "Submit" button.
     * After validating all required fields have been selected, sends the server the complaint request.
     * @param event Click event.
     */
    @FXML
    void submitComplaint(ActionEvent event) {
        if (!Validation.notEmpty(txtDetails, cmbParkingLot))
            return;

        if (txtDetails.getText().toLowerCase().contains("drop tables"))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Not gonna fall for that!");
            alert.showAndWait();
            return;
        }

        WaitScreen waitScreen = new WaitScreen();
        Integer parkingLotID = cmbOrder.getValue() != null ? cmbOrder.getValue().getParkingLotNumber() : cmbParkingLot.getValue().getParkingLotID();
        Complaint newComplaint = new Complaint(CPSClientGUI.getSession().getUserId(),
                cmbOrder.getValue() != null ? cmbOrder.getValue().getOrderID() : -1,
                txtDetails.getText(),
                parkingLotID);
        Message newComplaintMsg = new Message(CREATE, COMPLAINT_PRE_CUSTOMER, newComplaint);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Complaint newComplaint = (Complaint) getMessage().getData().get(0);
                waitScreen.setGoBackOnClose(true);
                waitScreen.showSuccess("Your complaint has been registered!", newComplaint.getGUIString());

            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Something went wrong...","Ironically, something went wrong filing your complaint...");
            }
        };
        MessageTasker submitComplaintTask = new MessageTasker(newComplaintMsg, onSuccess, onFailure, "Complaining...");
        waitScreen.run(submitComplaintTask);
    }

    @FXML
    void clearOrder(ActionEvent event) {
        cmbParkingLot.setDisable(false);
        cmbParkingLot.getSelectionModel().clearSelection();
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }


}
