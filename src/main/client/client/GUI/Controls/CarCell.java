package client.GUI.Controls;

import client.GUI.CPSClientGUI;
import client.GUI.Forms.Customers.ManageCars;
import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
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

public class CarCell extends ListCell<Integer>{

    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    ManageCars _parent;

    public CarCell(ManageCars parent)
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
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
        {
            setGraphic(null);
        }
        else {
            if (item != null) {
                lblText.setText("Car No. " + item.toString());
                btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        deleteCar(item);
                    }
                });
                setGraphic(paneRow);
            }
        }
    }

    private void deleteCar(Integer car)
    {
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                {
                    waitScreen.setOnClose(()->_parent.queryServerForCars());
                    waitScreen.hide();
                }
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(this.getErrorString());
            }
        };
        Message deleteMessage = new Message(Message.MessageType.DELETE,
                Message.DataType.CARS,
                CPSClientGUI.getLoggedInUserID(),
                car);

        MessageTasker taskDelete = new MessageTasker("Connecting...",
                "Deleting Car...",
                "Car Removed!",
                "Failed to remove car!",
                deleteMessage, onSuccess, onFailure);
        waitScreen.run(taskDelete);
    }
}
