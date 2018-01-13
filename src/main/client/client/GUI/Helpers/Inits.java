package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import entity.Message;
import entity.ParkingLot;
import entity.PreOrder;
import entity.User;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.ArrayList;

/**
 * A class for common inits along with those who require to the server.
  */
public class Inits {

    /**
     * Populates a combo box with open orders.
     * @param comboBox The combo box to populate.
     */
    public static void initOrders(ComboBox<PreOrder> comboBox)
    {
        comboBox.getItems().clear();
        comboBox.setConverter(new StringConverter<PreOrder>() {
            @Override
            public String toString(PreOrder object) {
                return String.format("%d. %s - %s", object.getOrderID(), object.getEntryTime().toString(), object.getEstimatedExitTime().toString());
            }

            @Override
            public PreOrder fromString(String string) {
                return null;
            }
        });
        queryServer(Message.DataType.PREORDER, comboBox);
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
    public static void initParkingLots(ComboBox<ParkingLot> comboBox, boolean addRemote)
    {
        comboBox.getItems().clear();
        if (addRemote) {
            ParkingLot remote = new ParkingLot();
            remote.setParkingLotID(-1);
            remote.setLocation("Remote Login");
            comboBox.getItems().add(remote);
        }
        queryServer(Message.DataType.PARKING_LOT, comboBox);
    }

    private static void queryServer(Message.DataType type, ComboBox comboBox)
    {
        WaitScreen waitScreen = new WaitScreen();
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                comboBox.getItems().addAll(getMessage().getData());
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

        Message query = new Message(Message.MessageType.QUERY, type, user.getUID(), user.getUserType());
        MessageTasker queryParkingLots = new MessageTasker("Connecting...",
                "Getting information from server...",
                "Success!",
                "Failed!",
                query, onSuccess, onFailure);
        waitScreen.run(queryParkingLots);
    }
}
