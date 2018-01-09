package utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    /**
     * An enum of types of time units.
     */
    public static enum Units {
        DAYS(Calendar.DAY_OF_MONTH), HOURS(Calendar.HOUR_OF_DAY), MINUTES(Calendar.MINUTE);

        private final int value;
        private Units(int value)
        {
            this.value = value;
        }
    };

    public static Date addTime(Date original, Units unit, int num)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(original);
        int field;
        cal.add(unit.value, num);
        return cal.getTime();
    }
}
