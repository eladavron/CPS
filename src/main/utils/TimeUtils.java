package utils;

import Exceptions.NotImplementedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static final long SECONDS_IN_MS = 1000;
    public static final long MINUTES_IN_MS = 60 * SECONDS_IN_MS;
    public static final long HOURS_IN_MS   = 60 * MINUTES_IN_MS;
    public static final long DAYS_IN_MS    = 24 * HOURS_IN_MS;

    /**
     * Returns time to target hour in miliseconds
     * @param targetHour the target hour in 24 hours (midnight is 0)
     * @return Time in ms until target hour
     */
    public static long getNumMilliSecsTillHour(int targetHour)
    {
        long msToTargetHour = LocalDateTime.now().until(LocalDate.now().atTime(targetHour,0), ChronoUnit.MILLIS);
        return msToTargetHour > 0 ? msToTargetHour : (msToTargetHour + 24 * HOURS_IN_MS);
    }

    /**
     * calculates time in milliseconds until the next round hour
     * @return millisecs until next hour
     */
    public static long getMilliSecsUntilNextRoundHour()
    {
        Calendar calendar = Calendar.getInstance();
        Date time         = new Date();
        long now          = new Date().getTime();

        calendar.setTime(time);
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime() - now;
    }

    /**
     * An enum of types of time units.
     */
    public static enum Units {
        DAYS(Calendar.DAY_OF_MONTH), HOURS(Calendar.HOUR_OF_DAY), MINUTES(Calendar.MINUTE);

        private final int value;
        Units(int value)
        {
            this.value = value;
        }
    };

    /**
     * Add time in a given unit to a Date.
     * @param original The date to add the time to
     * @param unit The units of time to add
     * @param num How many to add
     * @return The result.
     */
    public static Date addTime(Date original, Units unit, int num)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(original);
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
    {   //TODO : CHECK THIS!
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
