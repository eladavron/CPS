package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import entity.User;
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
     * TODO: Maybe filter messages on server side?
     */
    public static void exitParkingStart()
    {
        WaitScreen waitScreen = new WaitScreen();
        User user = CPSClientGUI.getSession().getUser();
        Message queryOrdersMsg = new Message(Message.MessageType.QUERY, Message.DataType.ORDER, user.getUID(), user.getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                ArrayList orders = getMessage().getData();
                if (orders == null || orders.size() <= 0) //No active orders.
                {
                    waitScreen.showError("No cars parked!", "We didn't find your car(s) in our system.");
                }
                else if (orders.size() == 1) //One active order
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
        MessageTasker queryOrders = new MessageTasker("Connecting...",
                "Looking for you car(s)...",
                "Car(s) found!",
                "No cars found!",
                queryOrdersMsg,
                onSuccess,
                onFailed);
        queryOrders.setWaitScreen(waitScreen);
        waitScreen.run(queryOrders);
    }

    /**
     * Finish the process of taking your car out.
     * @param order the active order to end.
     * TODO: Handle charging in case of debt if server returns "FAILED" due to debt. May be a good idea to add a MessageType.DEBT_EXISTS.
     */
    private static void exitParkingFinish(Order order)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message finishOrderMsg = new Message(Message.MessageType.END_PARKING, Message.DataType.ORDER, order);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showSuccess("Thank you!","Thank you for using CPS car parking service!\nYour car is waiting for you.");
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskFinish = new MessageTasker("Finding car...",
                "Retrieving car...",
                "Car retrieved!",
                "Failed!",
                finishOrderMsg,
                onSuccess,
                onFailed);
        taskFinish.setWaitScreen(waitScreen);
        waitScreen.run(taskFinish);
    }
}
