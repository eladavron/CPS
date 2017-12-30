package client.GUI;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * A class for external helper methods for the GUI.
 * @author Elad Avron
 */
public class Helpers {

    /**
     * Displays a tooltip message next to the supplied control.
     * @param parent Control to display tooltip message next to.
     * @param message Message to show.
     */
    public static void showTooltip(Node parent, String message)
    {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setAutoHide(true);
        Bounds boundInScene = parent.localToScreen(parent.getBoundsInLocal());
        tooltip.show(parent.getScene().getWindow(), boundInScene.getMaxX() + 5, boundInScene.getMinY());
    }

    /**
     * Converts a "LocalDate" instance to a "Date" instance.
     * Since LocalDate only contains a date and NOT a time, also adds hours and minutes.
     * @param localDate LocalDate instance to convert.
     * @param hoursToAdd Hours to add.
     * @param minutesToAdd Minutes to add.
     * @return Date instance.
     */
    public static Date getDateFromLocalDate(LocalDate localDate, int hoursToAdd, int minutesToAdd)
    {
        Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        Date exitTime = Date.from(instant);
        Calendar cal = Calendar.getInstance();
        cal.setTime(exitTime);
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        cal.add(Calendar.MINUTE, minutesToAdd);
        return cal.getTime();
    }
}
