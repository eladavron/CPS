package client.GUI.Forms;

import Exceptions.NotImplementedException;
import client.GUI.CPSClientGUI;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A controller for the main customer screen.
 */
public class CustomerScreenController implements Initializable {

    @FXML
    private Button btnCreateOrder;

    @FXML
    private Button btnEnterParking;

    @FXML
    private Button btnFileComplaint;

    @FXML
    private Button btnCheckComplaint;

    @FXML
    private Button btnExitParking;

    @FXML
    private Button btnManageSubs;

    @FXML
    private Button btnEditOrder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (CPSClientGUI.getSession().getUser() != null)
            CPSClientGUI.setStatus("Logged in as " + CPSClientGUI.getSession().getUser().getName(), Color.GREEN);
        //TODO: Add parking lot name to the status.
        //TODO: IMPORTANT: Hide enter/exit parking buttons for remote logins!
    }

    /**
     * Handles clicking any button in the Customer screen.
     * @param event the click event.
     */
    @FXML
    void handleCustomerButton(ActionEvent event) {
        String filename;
        if (event.getSource() == btnEnterParking) {
            filename = CPSClientGUI.ENTER_PARKING;
        } else if (event.getSource() == btnCreateOrder) {
            filename = CPSClientGUI.NEW_PREORDER;
        } else if (event.getSource() == btnExitParking) {
            exitParkingStart();
            return;
        }
        else {
            throw new NotImplementedException(event.getSource().toString());
        }
        CPSClientGUI.changeGUI(filename);
    }

    /**
     * Starts the process of exiting parking by querying the server for all open orders by this user.
     * The server will reply with a list of orders, which this method will filter for orders that are
     * both currently "active" (read: parked) and in the parking lot the user is currently logged on to.
     * In case more than one car is parked in the parking lot, will display a selection box.
     * TODO: Maybe filter messages on server side?
     */
    private void exitParkingStart()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryOrdersMsg = new Message(Message.MessageType.QUERY, Message.DataType.ORDER, CPSClientGUI.getSession().getUser());
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
        waitScreen.run(queryOrders);
    }

    /**
     * Finish the process of taking your car out.
     * @param order the active order to end.
     * TODO: Handle charging in case of debt if server returns "FAILED" due to debt. May be a good idea to add a MessageType.DEBT_EXISTS.
     */
    private void exitParkingFinish(Order order)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message finishOrderMsg = new Message(Message.MessageType.DELETE, Message.DataType.ORDER, order);
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
        waitScreen.run(taskFinish);
    }
}