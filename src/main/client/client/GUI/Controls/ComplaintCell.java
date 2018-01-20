package client.GUI.Controls;

import client.GUI.Forms.Customers.ManageComplaints;
import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Complaint;
import entity.Message;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * A custom class representing a complaint in a ListView.
 */
public class ComplaintCell extends ListCell<Complaint>{

    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    private ManageComplaints _parent;

    /**
     * Constructor which sets the screen that created the list as parent for refreshing purposes.
     * @param parent The {@link ManageComplaints} view that created the list this cell populates.
     */
    public ComplaintCell(ManageComplaints parent)
    {
        _parent = parent;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DeletableCell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException io) {
            ErrorHandlers.GUIError(io, false);
        }
    }

    /**
     * {@inheritDoc}
     */
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
                lblText.setText(item.getGUIString());

                btnDelete.setVisible(item.getStatus().equals(Complaint.ComplaintStatus.OPEN));
                btnDelete.setText("Cancel");
                btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        cancelComplaint(item);
                    }
                });
                setGraphic(paneRow);
            }
        }
    }

    /**
     * Cancels the complaint this row represents.
     * @param complaint The complaint to cancel. Technically redundant.
     */
    private void cancelComplaint(Complaint complaint)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message cancelComplaint = new Message(Message.MessageType.DELETE, Message.DataType.COMPLAINT_PRE_CUSTOMER, complaint);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.setOnClose(()->_parent.queryComplaints());
                waitScreen.hide();
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };

        MessageTasker cancelTasker = new MessageTasker(cancelComplaint, onSuccess, onFailed);
        waitScreen.run(cancelTasker);
    }
}