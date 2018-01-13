package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import entity.Message;
import entity.ParkingLot;
import entity.PreOrder;
import entity.User;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * A class for common queries to the server helper methods for the GUI.
  */
public class Queries {

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
        User user = null;
        if (CPSClientGUI.getSession() != null) {
            user = CPSClientGUI.getSession().getUser();
        }

        Message query = new Message(Message.MessageType.QUERY, type, user);
        MessageTasker queryParkingLots = new MessageTasker("Connecting...",
                "Getting information from server...",
                "Success!",
                "Failed!",
                query, onSuccess, onFailure);
        waitScreen.run(queryParkingLots);
    }
}
