package utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    /**
     * @param time The time object to add days to.
     * @param daysToAdd the amount of days to add to "time"
     * @return an updated time.
     */
    public static Date addTimeToDate(Date time, Integer daysToAdd)
    {
        Calendar cal = Calendar.getInstance(); //Gets a Calendar instance
        cal.setTime(time); //Sets the instance to the now object you created earlier
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd); //Adds 30 days to the calendar, which will now be at today+30
        return cal.getTime(); //Returns a Date object from the Calendar
    }

}
