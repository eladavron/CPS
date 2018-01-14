package client.GUI.Helpers;

import client.GUI.Controls.DateTimeCombo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    public static void highlightControl(Node control, Tooltip tooltip) {
        control.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 10px");
        _errorControls.put(control, tooltip);
        if (control instanceof TextField) {
            ((TextField) control).textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (tooltip != null)
                        tooltip.hide();
                    ((TextField) control).textProperty().removeListener(this);
                    removeHighlight(control);
                }
            });
        } else if (control instanceof ComboBox) {
            ((ComboBox) control).valueProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    if (tooltip != null)
                        tooltip.hide();
                    ((ComboBox) control).valueProperty().removeListener(this);
                    removeHighlight(control);
                }
            });
        } else if (control instanceof DatePicker)
        {
            ((DatePicker)control).valueProperty().addListener(new ChangeListener<LocalDate>() {
                @Override
                public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                    if (tooltip != null)
                        tooltip.hide();
                    ((DatePicker) control).valueProperty().removeListener(this);
                    removeHighlight(control);
                }
            });
        }
        else if (control instanceof FlowPane)
        {
            for (Node subControl : ((FlowPane) control).getChildren())
            {
                if (subControl instanceof CheckBox)
                {
                    ((CheckBox) subControl).selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (tooltip!= null)
                                tooltip.hide();
                            removeHighlight(control);
                        }
                    });
                }
            }
        }
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
        ArrayList<Node> allErrors = new ArrayList<Node>(_errorControls.keySet());
        for (Node node : allErrors)
        {
            removeHighlight((Node)node);
        }
        _errorControls.clear();
    }

    public static boolean emailValidation(TextField email)
    {
        if (email.getText().equals("u")) //MASTER USER
        {
            return true;
        }
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email.getText())) //Not a valid email address
        {
            Validation.showError(email, "Not a valid email address!");
            return false;
        }
        return true;
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
