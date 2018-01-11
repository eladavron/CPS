package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import client.GUI.Forms.WaitScreen;
import entity.Message;
import entity.Order;
import entity.User;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A class for external helper methods for the GUI.
 * @author Elad Avron
 */
public class Common {

    //region GUI Helpers
    private static ArrayList<Node> _highlightedControllers = new ArrayList<>();

    /**
     * Validates that the times aren't reversed or in the past or something.
     * @param entry DateTimePicker for entry. Pass NULL if entry is now!
     * @param exit DateTimePicker for Exit. Can't be null!
     * @return True if valid, false if not (will also highlight problems in form GUI).
     */
    public static boolean validateTimes(DateTimeCombo entry, DateTimeCombo exit)
    {
        Date entryDate = new Date();
        Date exitDate = null;
        boolean validate = true;
        if (entry != null) //If we have an entry form
        {
            if (entry.getDateTime() != null) //Make sure entry date is actually selected
            {
                entryDate = entry.getDateTime();
            } else {
                validate = false;
            }
        }

        if (exit.getDateTime() != null) //Make sure exit date is actually selected
        {
            exitDate = exit.getDateTime();
        }
        else
        {
            validate = false;
        }

        /*
            Now we have to check that the selection aren't in the past or in the wrong order.
         */
        if (validate)
        {
            /*
                Validated passed, now check dates are in order.
             */
            if (entry != null && entryDate.before(new Date())) //Entry for preorder is in the past
            {
                entry.showError("Only cars of type \"Delorean\" can enter in the past!");
                validate = false;
            }
            else if (exitDate.before(entryDate)) //Exit date is before entry
            {
                if (entry == null) //No entry specified, so we're assuming now.
                    exit.showError("Only cars of type \"Delorean\" can exit in the past!");
                else //Assuming it's a preorder
                    exit.showError("Only cars of type \"Delorean\" can exit before they enter!");
                validate = false;
            }
            else if (TimeUnit.HOURS.convert((exitDate.getTime() - entryDate.getTime()), TimeUnit.MILLISECONDS) < 1)
            {
                exit.showError("You can't park for less than 1 hour!");
                validate = false;
            }
        }
        /*
        At this point, validate is true only if everything went ok throughout the form.
         */

        return validate;
    }
    /**
     * Displays a tooltip message next to the supplied control.
     * @param control Control to display tooltip message next to.
     * @param message Message to show.
     */
    public static void showError(Node control, String message)
    {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setAutoHide(true);
        Bounds boundInScene = control.localToScreen(control.getBoundsInLocal());
        tooltip.show(control.getScene().getWindow(), boundInScene.getMaxX() + 5, boundInScene.getMinY());
        highlightControl(control);
    }

    /**
     * Highlights a control in red and sets it to revert to its previous form once clicked on.
     * @param control The control to highlight.
     */
    public static void highlightControl(Node control)
    {
        control.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 10px");
        _highlightedControllers.add(control);
        control.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                _highlightedControllers.remove(control);
                control.setStyle("");
            }
        });
    }

    public static void clearAllHighlighted()
    {
        for (Node control : _highlightedControllers)
        {
            control.setStyle("");
        }
        _highlightedControllers.clear();
    }
    //endregion

    //region Server Queries
    /**
     * Populates a combo box with open orders.
     * @param comboBox The combo box to populate.
     */
    public static void initOrders(ComboBox comboBox)
    {
        comboBox.getItems().clear();
        comboBox.setConverter(new StringConverter<Order>() {
            @Override
            public String toString(Order object) {
                return String.format("%d. %s - %s", object.getOrderID(), object.getEntryTime().toString(), object.getEstimatedExitTime().toString());
            }

            @Override
            public Order fromString(String string) {
                return null;
            }
        });
        queryServer(Message.DataType.PREORDER, comboBox);
    }

    /**
     * Populates a combo box with available parking lots.
     * @param comboBox The combo box to populate
     */
    public static void initParkingLots(ComboBox comboBox)
    {
        ArrayList list = new ArrayList();
        comboBox.getItems().clear();
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
        User user = CPSClientGUI.getSession().getUser();
        Message query = new Message(Message.MessageType.QUERY, type, user);
        MessageTasker queryParkingLots = new MessageTasker("Connecting...",
                "Getting information from server...",
                "Success!",
                "Failed!",
                query, onSuccess, onFailure);
        waitScreen.run(queryParkingLots);
    }
    //endregion


}
