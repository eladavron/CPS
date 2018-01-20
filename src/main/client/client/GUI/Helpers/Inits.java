package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import entity.*;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.util.ArrayList;

import static entity.Message.DataType.*;
import static entity.Order.OrderStatus.PRE_ORDER;

/**
 * A class for common inits along with those who require to the server.
  */
public class Inits {

    /**
     * Populates a combo box with open (pre)orders.
     * @param comboBox The combo box to populate.
     */
    public static void initPreorders(ComboBox<PreOrder> comboBox)
    {
        comboBox.setConverter(new StringConverter<PreOrder>() {
            @Override
            public String toString(PreOrder object) {
                return String.format("Order No. %d", object.getOrderID());
            }

            @Override
            public PreOrder fromString(String string) {
                return null;
            }
        });

        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                comboBox.getItems().clear();
                comboBox.setItems(null);
                comboBox.setItems(FXCollections.observableArrayList());
                for (Object object : getMessage().getData())
                {
                    if (object instanceof PreOrder && ((PreOrder) object).getOrderStatus().equals(PRE_ORDER))
                    {
                        if (((PreOrder) object).getParkingLotNumber().equals(CPSClientGUI.getSession().getParkingLotID()))
                            comboBox.getItems().add((PreOrder) object);
                    }
                }
                if (comboBox.getItems().size() == 0) //Empty list
                {
                    comboBox.setPromptText("No Orders on record.");
                    comboBox.setDisable(true);
                }
                waitScreen.hide();
            }
        };

        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Error querying server!", "Could not get required information from the server." + "\n" + getErrorString());
            }
        };
        User user = new User();
        if (CPSClientGUI.getSession() != null) {
            user = CPSClientGUI.getSession().getUser();
        }

        Message query = new Message(Message.MessageType.QUERY, PREORDER, user.getUID(), user.getUserType());
        MessageTasker queryPreorders = new MessageTasker(query, onSuccess, onFailure);
        waitScreen.run(queryPreorders);
    }

    /**
     * Inits an "Orders" ComboBox with all the orders that belong to this user.
     * If there aren't any, disables the control.
     * @param comboBox The ComboBox to init.
     */
    public static void initOrders(ComboBox<Order> comboBox)
    {
        comboBox.setConverter(new StringConverter<Order>() {
            @Override
            public String toString(Order object) {
                if (object != null)
                    return "Order No. " + object.getOrderID();
                else
                    return null;
            }

            @Override
            public Order fromString(String string) {
                return null;
            }
        });
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                comboBox.getItems().clear();
                comboBox.setItems(null);
                comboBox.setItems(FXCollections.observableArrayList());
                for (Object obj : getMessage().getData())
                {
                    comboBox.getItems().add((Order) obj);
                }
                if (comboBox.getItems().size() == 0)
                {
                    comboBox.setPromptText("No history on record.");
                    comboBox.setDisable(true);
                }
                waitScreen.hide();
            }
        };

        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        User user = new User();
        if (CPSClientGUI.getSession() != null) {
            user = CPSClientGUI.getSession().getUser();
        }

        Message query = new Message(Message.MessageType.QUERY, ORDER, user.getUID(), user.getUserType());
        MessageTasker queryOrders = new MessageTasker(query, onSuccess, onFailure);
        waitScreen.run(queryOrders);
    }

    /**
     * Inits a "Subscriptions" ComboBox with all the subscriptions that belong to this user.
     * If there aren't any, disables the control.
     * @param comboBox The ComboBox to init.
     */
    public static void initSubscriptions(ComboBox<Subscription> comboBox)
    {
        comboBox.setConverter(new StringConverter<Subscription>() {
            @Override
            public String toString(Subscription object) {
                if (object != null)
                    return object.getSubscriptionType() + " subscription #" + object.getSubscriptionID();
                else
                    return null;
            }

            @Override
            public Subscription fromString(String string) {
                return null;
            }
        });

        WaitScreen waitScreen = new WaitScreen();
        Message querySubs = new Message(Message.MessageType.QUERY, Message.DataType.SUBSCRIPTION, CPSClientGUI.getLoggedInUserID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                comboBox.getItems().clear();
                comboBox.setItems(null);
                comboBox.setItems(FXCollections.observableArrayList());
                for (Object obj : getMessage().getData())
                {
                    if (obj instanceof Subscription) {
                        comboBox.getItems().add((Subscription) obj);
                    }
                }
                if (comboBox.getItems().size() == 0)
                {
                    comboBox.setPromptText("No Subscriptions registered.");
                    comboBox.setDisable(true);
                }
                else
                {
                    comboBox.setDisable(false);
                    comboBox.setPromptText("Select Subscription...");
                }
                waitScreen.hide();
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskSubs = new MessageTasker(querySubs, onSuccess, onFailure);
        waitScreen.run(taskSubs);
    }


    /**
     * Inits a {@link client.GUI.Forms.Employees.ManageParkingSpaces} tab pane with the parking spaces in the supplied parking lot.<br>
     * Each tab is a floor, and inside it is a table denoting the rows and columns of that floow.
     * @param pane The TabPane that displays the Parking Lot.
     * @param parkingLot The Parking Lot that will be displayed.
     */
    public static void initParkingSpacesView(TabPane pane, ParkingLot parkingLot)
    {
        for (int h = 0; h < parkingLot.getHeight(); h++)
        {
            Tab newTab = new Tab();
            TableView<ParkingSpace> floor = new TableView<>();
        }
    }

    /**
     * Inits a pair of ComboBoxes representing hours and minutes.
     * @param hour The hours ComboBox.
     * @param minutes The minutes ComboBox.
     */
    public static void initTimeSelection(ComboBox<String> hour, ComboBox<String> minutes)
    {
        hour.getItems().clear();
        minutes.getItems().clear();

        //Init hours
        for (int i=0; i<24;i++)
        {
            hour.getItems().add((i<10 ? "0" : "") + i);
        }

        //Init minutes
        for (int i = 0; i < 60 ; i++)
        {
            minutes.getItems().add((i<10 ? "0" : "") + i);
        }
        hour.getSelectionModel().select(0);
        minutes.getSelectionModel().select(0);
    }

    /**
     * Inits a "Cars" ComboBox with all the cars that belong to this user.
     * If there aren't any, disables the control (though that shouldn't happen).
     * If there's only one, selects it automatically.
     * @param comboBox The ComboBox to init.
     */
    public static void initCars(ComboBox<Integer> comboBox)
    {
        comboBox.getItems().clear();
        if (CPSClientGUI.getSession().getUser().getUserType().equals(User.UserType.CUSTOMER))
        {
            ArrayList<Integer> carList = CPSClientGUI.getSession().getCustomer().getCarIDList();
            for (Integer car : carList)
            {
                comboBox.getItems().add(car);
            }

            //Select default
            if (comboBox.getItems().size() == 0)
            {
                comboBox.setDisable(true);
                comboBox.setPromptText("You have no car in your account.");
            }
            else if (comboBox.getItems().size() == 1)
            {
                comboBox.getSelectionModel().select(0);
                comboBox.setDisable(false);
            }
            else
            {
                comboBox.getSelectionModel().clearSelection();
                comboBox.setDisable(false);
                comboBox.setPromptText("Select car...");
            }
        }
    }

    /**
     * Populates a combo box with available parking lots.
     * @param comboBox The combo box to populate
     */
    public static void initParkingLots(ComboBox<ParkingLot> comboBox)
    {
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                comboBox.getItems().clear();
                comboBox.setItems(null);
                comboBox.setItems(FXCollections.observableArrayList());
                for (Object obj : getMessage().getData())
                {
                    comboBox.getItems().add((ParkingLot) obj);
                }
                if (comboBox.getItems().size() == 0)
                {
                    comboBox.setPromptText("No Parking Lots on record.");
                    comboBox.setDisable(true);
                }
                else
                {
                    comboBox.getSelectionModel().clearSelection();
                    comboBox.setDisable(false);
                    comboBox.setPromptText("Select Parking Lot...");
                }
                waitScreen.hide();
            }
        };

        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showError("Error querying server!", "Could not get required information from the server." + "\n" + getErrorString());
            }
        };
        User user = new User();
        if (CPSClientGUI.getSession() != null) {
            user = CPSClientGUI.getSession().getUser();
        }

        Message query = new Message(Message.MessageType.QUERY, PARKING_LOT_LIST, user.getUID(), user.getUserType());
        MessageTasker queryParkinglots = new MessageTasker(query, onSuccess, onFailure);
        waitScreen.run(queryParkinglots);
    }
}
