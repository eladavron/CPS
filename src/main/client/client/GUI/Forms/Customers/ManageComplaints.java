package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ComplaintCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Refreshable;
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

/**
 * A custom GUI controller for managing a user's Complaints.
 */
public class ManageComplaints extends GUIController implements Initializable, Refreshable {

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

    /**
    * {@inheritDoc}
     */
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

    /**
     * Handles the "Refresh List" button click.
     * @param event The button click event.
     */
    @FXML
    void refreshList(ActionEvent event) {
        queryComplaints();
    }

    /**
     * Queries the server for all complaints by this user and populates the list with the results.
     */
    public void queryComplaints()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryComplaints = new Message(Message.MessageType.QUERY, Message.DataType.COMPLAINT_PRE_CUSTOMER, CPSClientGUI.getLoggedInUserID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _complaintList.clear();
                ArrayList complaints = getMessage().getData();
                if (complaints.size() != 0) //Empty list
                {
                    for (Object complaint : complaints)
                    {
                        if (!((Complaint)complaint).getStatus().equals(Complaint.ComplaintStatus.CANCELLED))
                        {
                            _complaintList.add((Complaint) complaint);
                        }
                    }
                }
                waitScreen.hide();
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

    /**
     * Handles the "New Complaint" button click by redirecting the GUI to the {@link NewComplaint} screen.
     * @param event The click event.
     */
    @FXML
    void createComplaint(ActionEvent event) {
        CPSClientGUI.changeGUI(CPSClientGUI.NEW_COMPLAINT, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        queryComplaints();
    }

    /**
     * Goes back to the previous screen. The name is remnant of an older GUI scheme.
     * @param event the click event.
     */
    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(false);
    }
}
