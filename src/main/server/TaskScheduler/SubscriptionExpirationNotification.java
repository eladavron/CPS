package TaskScheduler;

import entity.Subscription;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static TaskScheduler.scheduledTask.printScheduledTaskExecution;
import static controller.Controllers.customerController;
import static controller.Controllers.subscriptionController;
import static utils.TimeUtils.*;

/**
 * Scheduled task for sending expiry notification for subscription
 */
public class SubscriptionExpirationNotification {

    /**
     * starts the scheduled task
     */
    public void execute() {

        Runnable runnable = () -> {

            printScheduledTaskExecution("Checking for subscriptions expirations");
            for (Subscription subscription : subscriptionController.getSubscriptionsMap().values())
            {
                long subscriptionExpirationTime = subscription.getExpiration().getTime();
                long now = new Date().getTime();

                if (subscriptionExpirationTime - now > 7*DAYS_IN_MS)
                {
                    customerController.sendSubscriptionUpcomingExpiryNotification(subscription);
                    printScheduledTaskExecution("Subscription #" + subscription.getSubscriptionID()
                    + "Upcoming expiry notification send to customer " + customerController.getCustomer(subscription.getUserID()).getName()) ;
                }
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable,
                         getMilliSecsUntilNextRoundHour() + 30*MINUTES_IN_MS,
                            1*HOURS_IN_MS,
                                    TimeUnit.MILLISECONDS);
    }


}
