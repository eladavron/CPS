package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.SubscriptionCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Refreshable;
import entity.Message;
import entity.Subscription;
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
 * A GUI Controller for the Manage Subscriptions screen.
 */
public class ManageSubscriptions extends GUIController implements Initializable, Refreshable {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnRefresh;

    @FXML
    private ListView<Subscription> listViewSubs;

    private ObservableList<Subscription> _subList = FXCollections.observableArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listViewSubs.setCellFactory(new Callback<ListView<Subscription>, ListCell<Subscription>>() {
            @Override
            public ListCell<Subscription> call(ListView<Subscription> param) {
                return new SubscriptionCell();
            }
        });
        listViewSubs.setItems(_subList);
        querySubscriptions();
    }

    /**
     * Queries the server for any subscriptions this user has.
     */
    private void querySubscriptions()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message querySubs = new Message(Message.MessageType.QUERY, Message.DataType.SUBSCRIPTION, CPSClientGUI.getLoggedInUserID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _subList.clear();
                ArrayList subs = getMessage().getData();
                if (subs.size() == 0) //Empty list
                {
                    waitScreen.showSuccess("You have no active subscriptions!",
                            "You can add a new subscription using the  \"New\" button.", 5);
                }
                else
                {
                    for (Object sub : subs)
                    {
                        if (sub instanceof Subscription) {
                            _subList.add((Subscription) sub);
                        }
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
        MessageTasker queryOrders = new MessageTasker(querySubs, onSuccess, onFailed, "Checking subscription...");
        waitScreen.run(queryOrders);
    }

    /**
     * Handles the "New..." click event.
     * @param event The click event.
     */
    @FXML
    void addSubscription(ActionEvent event)
    {
        CPSClientGUI.changeGUI("Forms/Customers/NewSubscription.fxml", this); //For now, only this control should access this so it's not moved to the main.
    }

    /**
     * Handles the "Refresh" button click event.
     * @param event The button click event.
     */
    @FXML
    void refreshSubs(ActionEvent event) {
        querySubscriptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        querySubscriptions();
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
