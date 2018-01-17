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
                        comboBox.getItems().add((PreOrder) object);
                    }
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

        Message query = new Message(Message.MessageType.QUERY, ORDER, user.getUID(), user.getUserType());
        MessageTasker queryOrders = new MessageTasker(query, onSuccess, onFailure);
        waitScreen.run(queryOrders);
    }


    /**
     * Inits a parkins apces tab pane with the parking spaces in the supplied parking lot.
     * Each pane is a floor, and inside it is a table denoting the rows and columns of that floow.
     * @param pane
     * @param parkingLot
     */
    public static void initParkingSpacesView(TabPane pane, ParkingLot parkingLot)
    {
        for (int h = 0; h < parkingLot.getHeight(); h++)
        {
            Tab newTab = new Tab();
            TableView<ParkingSpace> floor = new TableView<>();
        }
    }

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
            if (comboBox.getItems().size() == 1)
            {
                comboBox.getSelectionModel().select(0);
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

        Message query = new Message(Message.MessageType.QUERY, PARKING_LOT, user.getUID(), user.getUserType());
        MessageTasker queryParkinglots = new MessageTasker(query, onSuccess, onFailure);
        waitScreen.run(queryParkinglots);
    }
}
