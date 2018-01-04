package client.GUI;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A class for external helper methods for the GUI.
 * @author Elad Avron
 */
public class Helpers {

    private static ArrayList<Node> _highlightedControllers = new ArrayList<>();

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

    /**
     * Gets a full date object from a DatePicker and two ComboBoxes.
     * @param datePicker A DatePicker.
     * @param hours A ComboBox - MUST have 24 members to work.
     * @param minutes A ComboBox - MUST have 60 members to work.
     * @return A date comprised of the supplied date, hours, and minutes.
     */
    public static Date getDateFromControls(DatePicker datePicker, ComboBox hours, ComboBox minutes)
    {
        int hoursToAdd = hours.getSelectionModel().getSelectedIndex();
        int minutesToAdd = minutes.getSelectionModel().getSelectedIndex();
        LocalDate date = datePicker.getValue();
        Instant instant = Instant.from(date.atStartOfDay(ZoneId.systemDefault()));
        Date exitTime = Date.from(instant);
        Calendar cal = Calendar.getInstance();
        cal.setTime(exitTime);
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        cal.add(Calendar.MINUTE, minutesToAdd);
        return cal.getTime();
    }
}
