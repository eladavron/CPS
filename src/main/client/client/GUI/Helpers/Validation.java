package client.GUI.Helpers;

import client.GUI.Controls.DateTimeCombo;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A class for common validators
 */
public class Validation {
    //region GUI Helpers
    private static HashMap<Node, Tooltip> _errorControls = new HashMap<>();

    public static boolean carNumber(String number)
    {
        return number.matches("\\d{7,8}");
    }

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
        if (_errorControls.containsKey(control))
        {
            removeHighlight(control);
        }
        Tooltip tooltip = new Tooltip(message);
        tooltip.setAutoHide(true);
        Bounds boundInScene = control.localToScreen(control.getBoundsInLocal());
        tooltip.show(control.getScene().getWindow(), boundInScene.getMaxX() + 5, boundInScene.getMinY());
        highlightControl(control, tooltip);
    }

    /**
     * Highlights a control in red and sets it to revert to its previous form once clicked on.
     * @param control The control to highlight.
     */
    public static void highlightControl(Node control, Tooltip tooltip)
    {
        control.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 10px");
        _errorControls.put(control, tooltip);
        control.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tooltip != null)
                    tooltip.hide();
                removeHighlight(control);
            }
        });
    }

    public static void removeHighlight(Node control)
    {
        control.setStyle("");
        if (_errorControls.containsKey(control))
        {
            if (_errorControls.get(control) != null)
                _errorControls.get(control).hide();
            _errorControls.remove(control);
        }

    }

    public static void clearAllHighlighted()
    {
        Set<Node> allErrors = _errorControls.keySet();
        for (Node node : allErrors)
        {
            removeHighlight(node);
        }
        _errorControls.clear();
    }

    public static boolean notEmpty(Node...fields) {
        boolean validate = true;
        for (Node node : fields) {
            if (node instanceof TextField) {
                if (((TextField)node).getText().matches("^\\s*$")) {
                    showError(node, "This field can not be empty!");
                    validate = false;
                }
            }
            else if (node instanceof ComboBox)
            {
                if (!node.isDisable() && ((ComboBox)node).getSelectionModel().getSelectedIndex() == -1)
                {
                    showError(node, "This field must be selected!");
                    validate = false;
                }
            }
        }
        return validate;
    }
}
