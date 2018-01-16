package scheduledTasks;

import java.sql.Timestamp;

public abstract class scheduledTask {

    protected abstract void execute();

    protected static void logScheduledTask(String string)
    {
        System.out.println(new Timestamp(System.currentTimeMillis()) + "  [SCHEDULED TASK] " + string);
    }



}
