package TaskScheduler;

import controller.Controllers;
import entity.Order;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static controller.Controllers.customerController;
import static controller.Controllers.orderController;
import static controller.Controllers.subscriptionController;
import static entity.Subscription.SubscriptionType.FULL;
import static utils.TimeUtils.DAYS_IN_MS;
import static utils.TimeUtils.HOURS_IN_MS;
import static utils.TimeUtils.getNumMilliSecsTillHour;

/**
 * Class which daily checks if a full sub. car is parked for over 14 days and orders a towing truck
 */
public class TwoWeeksParkBreach extends scheduledTask {

    private final static long   FULL_SUBSCRIPTION_MAX_PARK_PERIOD = 14*DAYS_IN_MS;
    private final static int    DEFAULT_TOWING_TIME               = 6;  //we call the towing car at 6am

    /**
     * Periodically checks on 6AM daily if a car is parked for over two weeks,
     */
    public void execute() {

        Runnable runnable = () -> {
            printScheduledTaskExecution("Checking for full subscriptions exceeding max allowed time of " +
                    FULL_SUBSCRIPTION_MAX_PARK_PERIOD/DAYS_IN_MS + " days");
            for (Order activeOrder : orderController.getAllActiveOrders()) {
                if (shouldFullSubscriptionCarBeTowedDueToTwoWeeksBreach(activeOrder)) {
                    customerController.sendMaxParkTimeBreachedTowingNotification(activeOrder);
                }
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable,
                                    getNumMilliSecsTillHour(DEFAULT_TOWING_TIME),
                            24 * HOURS_IN_MS,
                                    TimeUnit.MILLISECONDS);

    }

    /**
     * Decides if a full subscription car should be towed due to over 14 days constant parking
     * @param activeOrder
     * @return true if a car is parked for over 14 days and it has a full subscription, false otherwise
     */
    private boolean shouldFullSubscriptionCarBeTowedDueToTwoWeeksBreach(Order activeOrder) {
        return didActiveOrderBreachMaxParkingTime(activeOrder)
                && subscriptionController.doesExistsSubscriptionOfType(FULL, activeOrder.getCostumerID(), activeOrder.getCarID());
    }

    /**
     * Checks if an active order is in progress(car is parked) for over 14 days
     * @param activeOrder
     * @return true if car is parked for over 14 days, false otherwise
     */
    private static boolean didActiveOrderBreachMaxParkingTime(Order activeOrder){
        long now                = new Date().getTime();
        long actualEntryTime    = activeOrder.getEstimatedEntryTime().getTime();
        long parkingPeriod         = now - actualEntryTime;

        return parkingPeriod > FULL_SUBSCRIPTION_MAX_PARK_PERIOD;
    }
}
