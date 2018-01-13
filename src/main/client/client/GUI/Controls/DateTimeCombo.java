package client.GUI.Controls;

import client.GUI.Helpers.Validation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * A container class for functions on date-time pickers which are panes with a datetimepicker and two combo boxes.
 * Basically, whenever you have a DatePicker, a ComboBox for Hours and one for Minutes, use this class to encompass them.
 * Unfortunately this has no GUI elements - it's just a container for existing elements.
 * @author Elad Avron
 */
public class DateTimeCombo {

    private ComboBox<String> _cmbHours;
    private ComboBox<String> _cmbMinutes;
    private DatePicker _datePicker;


    /**
     * Primary constructor.
     * Binds the controls together as one entity, and initiates the ComboBoxes.
     * @param datePicker The Date picker component.
     * @param cmbHours A ComboBox for hours. Will be initiated with 24 consecutive strings.
     * @param cmbMinutes A ComboxBox for minutes. Will be initiated with 60 consecutive strings.
     */
    public DateTimeCombo(DatePicker datePicker, ComboBox<String> cmbHours, ComboBox<String> cmbMinutes) {
        _cmbHours = cmbHours;
        _datePicker = datePicker;
        _cmbMinutes = cmbMinutes;

        _cmbHours.getItems().clear();
        _cmbMinutes.getItems().clear();

        //Init hours
        for (int i=0; i<24;i++)
        {
            _cmbHours.getItems().add((i<10 ? "0" : "") + i);
        }

        //Init minutes
        for (int i = 0; i < 60 ; i++)
        {
            _cmbMinutes.getItems().add((i<10 ? "0" : "") + i);
        }
    }

    //region Getters and setters

    public DatePicker getDatePicker() {
        return _datePicker;
    }

    public ComboBox<String> getComboHours() {
        return _cmbHours;
    }

    public ComboBox<String> getComboMinutes() {
        return _cmbMinutes;
    }

    /**
     * Returns the Date currently showing on the controls.
     * @return The Date (and time) represented by the collective.
     */
    public Date getDateTime()
    {
        if (_datePicker.getValue() == null) {
            Validation.showError(_datePicker, "Please select valid date!");
            return null;
        }
        int hoursToAdd = _cmbHours.getSelectionModel().getSelectedIndex();
        int minutesToAdd = _cmbMinutes.getSelectionModel().getSelectedIndex();
        LocalDate date = _datePicker.getValue();
        Instant instant = Instant.from(date.atStartOfDay(ZoneId.systemDefault()));
        Date returnDate = Date.from(instant);
        Calendar cal = Calendar.getInstance();
        cal.setTime(returnDate);
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        cal.add(Calendar.MINUTE, minutesToAdd);
        return cal.getTime();
    }

    /**
     * Sets all controls to represent the supplied Date.
     * @param dateToSet Date to set.
     */
    public void setDateTime(Date dateToSet)
    {
        LocalDate date = dateToSet.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToSet);
        _datePicker.setValue(date);
        _cmbHours.getSelectionModel().select(cal.get(Calendar.HOUR_OF_DAY));
        _cmbMinutes.getSelectionModel().select(cal.get(Calendar.MINUTE));
    }

    public void setHours(int hour)
    {
        if (hour < 0 || hour > 23)
        {
            throw new RuntimeException("Hours must be between 0 and 23");
        }
        _cmbHours.getSelectionModel().select(hour);
    }

    public void setMinutes(int minutes)
    {
        if (minutes < 0 || minutes > 59)
        {
            throw new RuntimeException("Minutes must be between 0 and 59");
        }
        _cmbHours.getSelectionModel().select(minutes);
    }

    public int getHours()
    {
        return _cmbHours.getSelectionModel().getSelectedIndex();
    }

    public int getMinutes()
    {
        return _cmbMinutes.getSelectionModel().getSelectedIndex();
    }

    //endregion

    /**
     * Highlight the controls and show an error message next to the rightmost control.
     * @param message The error message to display.
     */
    public void showError(String message)
    {
        Validation.highlightControl(_datePicker, null);
        Validation.highlightControl(_cmbHours, null);
        Validation.showError(_cmbMinutes, message);
    }
}
