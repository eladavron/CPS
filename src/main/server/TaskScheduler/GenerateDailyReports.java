package TaskScheduler;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static controller.Controllers.parkingController;
import static utils.TimeUtils.HOURS_IN_MS;
import static utils.TimeUtils.getNumMilliSecsTillHour;


/**
 * Class which schedules daily generation of parking lot status reports
 */
public class GenerateDailyReports extends scheduledTask {

    private final int REPORTS_GENERATION_HOUR = 9;

    /**
     * Start scheduling for generating reports
     */
    public void execute() {

        Runnable runnable = () -> {

            printScheduledTaskExecution("Generating daily parking lots report");
            try {
                parkingController.genTaskerDailyReports();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable,  //TASK DID NOT START - DEBUGGING
                             getNumMilliSecsTillHour(REPORTS_GENERATION_HOUR),
                            24 * HOURS_IN_MS,
                                    TimeUnit.MILLISECONDS);
    }
}
