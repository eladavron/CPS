package client.GUI;

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

    public DatePicker getDatePicker() {
        return _datePicker;
    }

    public ComboBox<String> getComboHours() {
        return _cmbHours;
    }

    public ComboBox<String> getComboMinutes() {
        return _cmbMinutes;
    }

    public Date getDateTime()
    {
        if (_datePicker.getValue() == null) {
            Helpers.showError(_datePicker, "Please select valid date!");
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

    public void setDateTime(Date dateToSet)
    {
        LocalDate date = dateToSet.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToSet);
        _datePicker.setValue(date);
        _cmbHours.getSelectionModel().select(cal.get(Calendar.HOUR_OF_DAY));
        _cmbMinutes.getSelectionModel().select(cal.get(Calendar.MINUTE));
    }

    public void showError(String message)
    {
        Helpers.highlightControl(_datePicker);
        Helpers.highlightControl(_cmbHours);
        Helpers.showError(_cmbMinutes, message);
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

}
