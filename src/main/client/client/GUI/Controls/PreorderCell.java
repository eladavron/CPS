package client.GUI.Controls;

import client.GUI.Forms.ManagePreorders;
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

public class PreorderCell extends ListCell<PreOrder>{


    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    ManagePreorders _parent;

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
                lblText.setText("Order No. " + item.getOrderID() + "\n"
                        + "From: " + item.getEstimatedEntryTime() + "\n"
                        + "To: " + item.getEstimatedExitTime() +"\n"
                        + "Car No. "+  item.getCarID());
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

    private void deletePreorder(PreOrder preOrder)
    {
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        _parent.queryPreorders();
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

        MessageTasker taskDelete = new MessageTasker("Connecting...",
                "Cancelling order...",
                "Order cancelled!",
                "Failed to cancel order!",
                deleteMessage, onSuccess, onFailure);
        waitScreen.run(taskDelete);
    }
}
