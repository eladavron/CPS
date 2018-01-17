package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.Inits;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Complaint;
import entity.Message;
import entity.Order;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

import static entity.Message.DataType.COMPLAINT;
import static entity.Message.MessageType.CREATE;

public class NewComplaint implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private ComboBox<Order> cmbOrder;

    @FXML
    private Button btnSubmit;

    @FXML
    private TextArea txtDetails;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> Inits.initOrders(cmbOrder));
    }

    @FXML
    void submitComplaint(ActionEvent event) {
        WaitScreen waitScreen = new WaitScreen();
        Complaint newComplaint = new Complaint(CPSClientGUI.getSession().getUserId(),
                cmbOrder.getValue() != null ? cmbOrder.getValue().getOrderID() : -1,
                txtDetails.getText());
        Message newComplaintMsg = new Message(CREATE, COMPLAINT, newComplaint);
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
                waitScreen.showDefaultError();
            }
        };
        MessageTasker submitComplaintTask = new MessageTasker(newComplaintMsg, onSuccess, onFailure, "Complaining...");
        waitScreen.run(submitComplaintTask);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }


}
