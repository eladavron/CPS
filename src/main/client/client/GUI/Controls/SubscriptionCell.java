package client.GUI.Controls;

import Exceptions.NotImplementedException;
import client.GUI.Forms.Customers.ManageSubscriptions;
import client.GUI.Helpers.ErrorHandlers;
import entity.Subscription;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import utils.StringUtils;

import java.io.IOException;
import java.util.Date;

public class SubscriptionCell extends ListCell<Subscription> {

    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    private ManageSubscriptions _parent;

    public SubscriptionCell(ManageSubscriptions parent)
    {
        _parent = parent;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DeletableCell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException io) {
            ErrorHandlers.GUIError(io, false);
        }
        btnDelete.setText("Extend...");
    }

    @Override
    protected void updateItem(Subscription item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
        {
            setGraphic(null);
        }
        else
        {
            if (item != null)
            {
                Subscription.SubscriptionType type = item.getSubscriptionType();
                Integer carID = item.getCarID();
                Date expiration = item.getExpiration();
                String subType = StringUtils.SubscriptionTypeName(type);
                lblText.setText(subType + " subscription:\n"
                        + "Registered car: " + item.getCarID() + "\n"
                        + "Expiration date: " + item.getExpiration().toString());
                btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        extendSubscription(item);
                    }
                });
                setGraphic(paneRow);
            }
        }
    }

    private void extendSubscription(Subscription item)
    {
        throw new NotImplementedException("Not yet implemented extension.");
    }
}
