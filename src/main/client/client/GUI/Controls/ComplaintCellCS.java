package client.GUI.Controls;

import client.GUI.Forms.Employees.ManageComplaintsCS;
import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Complaint;
import entity.Message;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static entity.Complaint.ComplaintStatus.CANCELLED;
import static entity.Complaint.ComplaintStatus.OPEN;
import static entity.Message.MessageType.UPDATE;

public class ComplaintCellCS extends ListCell<Complaint> implements Initializable{

    @FXML
    private Button btnApply;

    @FXML
    private Label lblText;

    @FXML
    private ComboBox<Complaint.ComplaintStatus> cmbStatus;

    @FXML
    private BorderPane paneRow;

    private Complaint.ComplaintStatus _originalStatus;

    public ComplaintCellCS(ManageComplaintsCS parent)
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ComplaintCellCS.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException io) {
            ErrorHandlers.GUIError(io, false);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbStatus.setItems(FXCollections.observableArrayList(Complaint.ComplaintStatus.values()));
        cmbStatus.valueProperty().addListener(((observable, oldValue, newValue) -> {
            btnApply.setDisable(newValue.equals(_originalStatus));
            getItem().setStatus(newValue);
        }));
    }

    @Override
    protected void updateItem(Complaint item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
        {
            setGraphic(null);
        }
        else
        {
            if (item != null)
            {
                _originalStatus = item.getStatus();
                lblText.setText(item.getGUIString());
                cmbStatus.setValue(item.getStatus());
                cmbStatus.setDisable(item.getStatus().equals(CANCELLED));
                setGraphic(paneRow);
                if (!item.getStatus().equals(OPEN))
                {
                    cmbStatus.getItems().remove(OPEN);
                }
            }
        }
    }

    @FXML
    void updateStatus(ActionEvent event) {
        WaitScreen waitScreen = new WaitScreen();
        Message updateMessage = new Message(UPDATE, Message.DataType.COMPLAINT_PRE_CUSTOMER, this.getItem());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.hide();
            }
        };

        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskUpdate= new MessageTasker(updateMessage,onSuccess,onFailure,"Updating...");
        waitScreen.run(taskUpdate);
    }

}