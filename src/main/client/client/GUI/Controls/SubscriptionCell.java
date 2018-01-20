package client.GUI.Controls;

import client.GUI.Forms.Customers.ManageSubscriptions;
import client.GUI.Helpers.ErrorHandlers;
import entity.Subscription;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a single row in the {@link ManageSubscriptions} view's ListView.
 */
public class SubscriptionCell extends ListCell<Subscription> {

    @FXML
    private BorderPane paneRow;

    @FXML
    private Button btnDelete;

    @FXML
    private Label lblText;

    /**
     * Default Constructor.
     * Loads the row body from the FXML.
     */
    public SubscriptionCell()
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DeletableCell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException io) {
            ErrorHandlers.GUIError(io, false);
        }
        btnDelete.setText("Extend...");
    }

    /**
     * {@inheritDoc}
     */
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
                ArrayList<Integer> carsID = item.getCarsID();
                Date expiration = item.getExpiration();
                String subType = StringUtils.SubscriptionTypeName(type);
                lblText.setText(subType + " subscription No. " + item.getSubscriptionID() + ":\n"
                        + "Registered car: " + carsID + "\n"
                        + "Expiration date: " + item.getExpiration().toString());
                paneRow.setRight(null);
                setGraphic(paneRow);
            }
        }
    }
}
