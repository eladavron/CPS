package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ComplaintCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Complaint;
import entity.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ManageComplaints implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnNew;

    @FXML
    private ListView<Complaint> listViewComplaint;

    private ObservableList<Complaint> _complaintList = FXCollections.observableArrayList();

    private ManageComplaints _this;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _this = this;
        listViewComplaint.setItems(_complaintList);
        listViewComplaint.setCellFactory(new Callback<ListView<Complaint>, ListCell<Complaint>>() {
            @Override
            public ListCell<Complaint> call(ListView<Complaint> param) {
                return new ComplaintCell(_this);
            }
        });
        Platform.runLater(this::queryComplaints);
    }

    @FXML
    void refreshList(ActionEvent event) {
        queryComplaints();
    }

    public void queryComplaints()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryComplaints = new Message(Message.MessageType.QUERY, Message.DataType.COMPLAINT, CPSClientGUI.getLoggedInUserID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _complaintList.clear();
                ArrayList subs = getMessage().getData();
                if (subs.size() != 0) //Empty list
                {
                    for (Object sub : subs)
                    {
                        _complaintList.add((Complaint) sub);
                    }
                    waitScreen.hide();
                }
            }
        };

        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker queryOrders = new MessageTasker(queryComplaints, onSuccess, onFailed, "Checking for complaints...");
        waitScreen.run(queryOrders);
    }

    @FXML
    void createComplaint(ActionEvent event) {
        CPSClientGUI.changeGUI(CPSClientGUI.NEW_COMPLAINT);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(true);
    }
}
