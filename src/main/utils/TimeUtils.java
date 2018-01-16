package utils;

import Exceptions.NotImplementedException;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static final long SECONDS_IN_MS = 1000;
    public static final long MINUTES_IN_MS = 60*SECONDS_IN_MS;
    public static final long HOURS_IN_MS   = 60*MINUTES_IN_MS;

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

    /**
     * Calculates the difference between two days in whatever unit you choose.
     * @param one One of the dates.
     * @param two The other date.
     * @param units The time unit you want to calculate in.
     * @return the difference in the selected time unit.
     */
    public static long timeDifference(Date one, Date two, Units units)
    {
        long diff = Math.abs(one.getTime() - two.getTime());
        switch (units)
        {
            case DAYS:
                return TimeUnit.MILLISECONDS.toDays(diff);
            case HOURS:
                return TimeUnit.MILLISECONDS.toHours(diff);
            case MINUTES:
                return TimeUnit.MILLISECONDS.toMinutes(diff);
            default:
                throw new NotImplementedException("No such unit as " + units.toString());
        }
    }
}
