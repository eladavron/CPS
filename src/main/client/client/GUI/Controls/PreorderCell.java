package client.GUI.Controls;

import client.GUI.Forms.Customers.ManagePreorders;
import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.PreOrder;
import javafx.application.Platform;
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
 * A single row in a ListView representing a {@link PreOrder}
 */
public class PreorderCell extends ListCell<PreOrder>{

    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    private ManagePreorders _parent;

    /**
     * Constructor which sets the screen that created the list as parent for refreshing purposes.
     * @param parent The "Manage Preorders" view that created the list this cell populates.
     */
    public PreorderCell(ManagePreorders parent)
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
    protected void updateItem(PreOrder item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
        {
            setGraphic(null);
        }
        else
        {
            if (item != null)
            {
                lblText.setText("Order No. " + item.getOrderID()
                        + "\nParking Lot: " + item.getParkingLotNumber()
                        + "\nFrom: " + item.getEstimatedEntryTime()
                        + "\nTo: " + item.getEstimatedExitTime()
                        + "\nCar No. "+  item.getCarID());
                btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        deletePreorder(item);
                    }
                });
                setGraphic(paneRow);
            }
        }
    }

    /**
     * Deletes the PreOrder represented by this row
     * @param preOrder The PreOrder to delete (Technically redundant, but better safe than sorry).
     */
    private void deletePreorder(PreOrder preOrder)
    {
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        double refund = ((PreOrder)getMessage().getData().get(0)).getCharge();
                        if (refund > 0)
                        {
                            waitScreen.setOnClose(() -> _parent.queryPreorders());
                            waitScreen.showSuccess("You've been refunded!",
                                                "A refund by sum of " + refund + " will be sent by cheque!");
                        }
                        else
                        {
                            _parent.queryPreorders();
                            waitScreen.hide();
                        }
                    }
                });
            }
        };

        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(this.getErrorString());
            }
        };
        Message deleteMessage = new Message(Message.MessageType.DELETE,
                Message.DataType.PREORDER,
                preOrder);

        MessageTasker taskDelete = new MessageTasker(deleteMessage, onSuccess, onFailure, "Cancelling order...");
        waitScreen.run(taskDelete);
    }
}
