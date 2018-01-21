package TaskScheduler;

import controller.Controllers;
import entity.PreOrder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static controller.Controllers.*;
import static entity.Subscription.SubscriptionType.FULL;
import static utils.TimeUtils.*;


/**
 * A class which periodically checks every minute for cars late for reserved orders and send reminders
 * Scheduled to run on the 30th minute of every hour
 */
public class PeriodicLateCheck extends scheduledTask{

    private static final long MAXIMUM_BREACH_TIME = 30*MINUTES_IN_MS;

    /**
     * Starts scheduling for late checks to parking
     */
    public void execute() {

        Runnable runnable = () -> {

            printScheduledTaskExecution("Checking for late orders");
            for (PreOrder thisPreOrder : orderController.getAllPreOrders())
            {                                                  //BreachPeriod==between 0 and 30 mins after estimated.
                if (!thisPreOrder.isMarkedLate() && isEstimatedEntryTimeBreachedByZeroToThirtyMins(thisPreOrder))
                {
                    if (customerController.sendEntryTimeBreachedNotification(thisPreOrder))
                    {
                        thisPreOrder.setMarkedLate();
                    }
                }
                else if(needToCancelOrderDueToEntryTimeBreached(thisPreOrder))
                {
                    try {
                        orderController.deleteOrder(thisPreOrder.getOrderID());
                    } catch (SQLException e) {
                        if (Controllers.IS_DEBUG_CONTROLLER)
                        {
                            e.printStackTrace();
                        }
                        System.err.println("An error occurred processing that command.");
                    }
                    printScheduledTaskExecution("Order #" + thisPreOrder.getOrderID() + " was deleted due to late customer");
                }
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.MINUTES);

    }


    /**
     * Checks if estimated arrival time is breached by param milliSecs
     * @param milliSecs period in milliseconds
     * @param preOrder the preorder
     * @return
     */
    private static boolean isEstimatedEntryTimeBreachedBy(long milliSecs, PreOrder preOrder) {
        boolean isEntryTimeBreached = (new Date().getTime() - preOrder.getEstimatedEntryTime().getTime() > milliSecs);
        return  isEntryTimeBreached;
    }

    /**
     * Checks if estimated arrival time is in breach window of 0-30 mins running late
     * @param preOrder the preorder
     * @return true if estimated arrival time is breached
     */
    private static boolean isEstimatedEntryTimeBreachedByZeroToThirtyMins(PreOrder preOrder){
        long now                = new Date().getTime();
        long estimatedEntryTime = preOrder.getEstimatedEntryTime().getTime();
        long breachTime         = now - estimatedEntryTime;
        return breachTime > 0 && breachTime < MAXIMUM_BREACH_TIME;
    }

    /**
     * Checks if order needs to be cancelled due to late entry to parking lot
     * @param preOrder
     * @return true if customer did not confirm she will arrive and time breach > 30. Otherwise false
     */
    private static boolean needToCancelOrderDueToEntryTimeBreached(PreOrder preOrder) {
        return !preOrder.isLateArrivalConfirmedByCustomer()
                                && isEstimatedEntryTimeBreachedBy(MAXIMUM_BREACH_TIME, preOrder);

    }

}