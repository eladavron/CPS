package TaskScheduler;

import java.sql.Timestamp;

/**
 * Abstract class for scheduled tasks
 */
public abstract class scheduledTask {

    /**
     * abstract method for beginning scheduled execution
     */
    protected abstract void execute();

    /**
     * Prints execution message for scheduled tasks
     * @param string
     */
    protected static void printScheduledTaskExecution(String string)
    {
        System.out.println(new Timestamp(System.currentTimeMillis()) + "  [SCHEDULED TASK] " + string);
    }

}
