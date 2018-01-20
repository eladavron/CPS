package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import javafx.scene.control.ChoiceDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Despite not having its own interface, this class binds together all the methods related to exiting parking.
 */
public class ExitParking {
    /**
     * Starts the process of exiting parking by querying the server for all open orders by this user.
     * The server will reply with a list of orders, which this method will filter for orders that are
     * both currently "active" (read: parked) and in the parking lot the user is currently logged on to.
     * In case more than one car is parked in the parking lot, will display a selection box.
     */
    public static void exitParkingStart()
    {
        WaitScreen waitScreen = new WaitScreen();
        Integer UID = CPSClientGUI.getSession().getUserId();
        Integer parkingLotID = CPSClientGUI.getSession().getParkingLotID();
        Message queryOrdersMsg = new Message(Message.MessageType.QUERY, Message.DataType.ORDER, UID, CPSClientGUI.getSession().getUserType(), parkingLotID);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                ArrayList orders = getMessage().getData();
                if (orders == null || orders.size() <= 0) //No active orders.
                {
                    waitScreen.showError("No cars parked!", "We didn't find your car(s) in this parking lot.");
                }
                else if (orders. size() == 1) //One active order
                {
                    waitScreen.hide();
                    Order foundOrder = (Order) orders.get(0);
                    exitParkingFinish(foundOrder);
                }
                else //More than one active order
                {
                    waitScreen.cancelTimeout();
                    HashMap<Integer, Order> orderMap = new HashMap<Integer, Order>(); //Map the car number to the order
                    ArrayList<Integer> choices = new ArrayList<>();
                    for (Object order : orders)
                    {
                        Integer carID = ((Order)order).getCarID();
                        orderMap.put(carID, (Order)order);
                        choices.add(carID);
                    }
                    ChoiceDialog<Integer> dialog = new ChoiceDialog<Integer>(choices.get(0), choices);
                    dialog.setTitle("Choose Car");
                    dialog.setHeaderText("Multiple cars found!");
                    dialog.setContentText("Select car to retrieve: ");
                    Optional<Integer> result = dialog.showAndWait();
                    waitScreen.hide();
                    result.ifPresent(integer -> exitParkingFinish(orderMap.get(integer))); //Once car selected - start taking it out
                }
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker queryOrders = new MessageTasker(queryOrdersMsg, onSuccess, onFailed, "Looking for your car...");
        queryOrders.setWaitScreen(waitScreen);
        waitScreen.run(queryOrders);
    }

    /**
     * Finish the process of taking your car out.
     * @param order the active order to end.
     */
    private static void exitParkingFinish(Order order)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message finishOrderMsg = new Message(Message.MessageType.END_PARKING, Message.DataType.ORDER, order);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                String exitMsg = "Thank you for using CPS car parking service!\nYour car is waiting for you.";
                if (getMessage().getData() != null && getMessage().getData().get(0) != null && getMessage().getDataType().equals(Message.DataType.PRIMITIVE))
                {
                    exitMsg+="\nBy the way, you've been refunded for " + getMessage().getData().get(0) + " NIS.";
                }
                waitScreen.showSuccess("Thank you!",exitMsg);
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskFinish = new MessageTasker(finishOrderMsg, onSuccess, onFailed, "Retrieving car...");
        taskFinish.setWaitScreen(waitScreen);
        waitScreen.run(taskFinish);
    }
}
