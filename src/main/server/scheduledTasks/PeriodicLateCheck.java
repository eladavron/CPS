package scheduledTasks;

import entity.PreOrder;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static controller.Controllers.customerController;
import static controller.Controllers.orderController;
import static scheduledTasks.scheduledTask.logScheduledTask;
import static utils.TimeUtils.*;

public class PeriodicLateCheck extends scheduledTask{

    private static final long MAXIMUM_BREACH_TIME = 30*MINUTES_IN_MS;

    public void execute() {

        Runnable runnable = () -> {

            logScheduledTask("Checking for late orders");
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
                        e.printStackTrace();
                        System.err.println("An error occurred processing that command.");
                    }
                    logScheduledTask("Order #" + thisPreOrder.getOrderID() + " was deleted due to late customer");
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
        return isEntryTimeBreached;
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