package controller;

import entity.Billing;
import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static controller.Controllers.billingController;
import static entity.Billing.priceList.MONTHLY_FULL_SUBSCRIPTION;
import static entity.Billing.priceList.MONTHLY_REGULAR_MULTIPLE_CARS_PER_SUBSCRIPTION;
import static entity.Billing.priceList.MONTHLY_REGULAR_SUBSCRIPTION;

/**
 *  A controller Class to handle all of the Billings.
 */
public class BillingController {
    private static BillingController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private BillingController() {
    }

    /**
     * The Static initializer constructs the instance at class
     * loading time; this is to simulate a more involved
     * construction process (it it were really simple, you'd just
     * use an initializer)
     */
    static {
        instance = new BillingController();
    }

    /** Static 'instance' method */
    public static BillingController getInstance() {
        return instance;
    }

    /**
     * Calculates price for parking according to type of parking and entry/exit times
     * @param entryTime The entry time of the car
     * @param exitTime The exit time of the car
     * @param priceType The hourly price according to the customer/order type
     * @return
     */
    public double calculateParkingCharge(Date entryTime, Date exitTime, Billing.priceList priceType){
        double minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(exitTime.getTime() - entryTime.getTime()));
        minutes = (minutes < 60) ? 60 : minutes; //minimum is 1 hour
        return minutes * priceType.getPrice() / 60;
    }

    public double getSubscriptionCost(Billing.priceList subscriptionType){
        return subscriptionType.getPrice();
    }

    /**
     *  'Empty Shell' of credit card payment system
     * @param creditCardType
     * @param expDate
     * @param creditNumber
     * @param NumberOfPayments
     * @param ID
     * @return
     */
    public Boolean payCharge(String creditCardType, Date expDate, Integer creditNumber,Integer NumberOfPayments, Integer ID){
        return true;
    }

    public double calculateChargeForSubscription(Subscription subscription)
    {
        if (subscription instanceof FullSubscription)
        {
            subscription.setCharge(billingController.getSubscriptionCost(MONTHLY_FULL_SUBSCRIPTION));
        }
        else if (subscription instanceof RegularSubscription)
        {
            int numCars = subscription.getCarsID().size();
            if (numCars == 1)
            {
                subscription.setCharge(billingController.getSubscriptionCost(MONTHLY_REGULAR_SUBSCRIPTION));
            }
            else
            {
                subscription.setCharge(numCars * billingController.getSubscriptionCost(MONTHLY_REGULAR_MULTIPLE_CARS_PER_SUBSCRIPTION));
            }
        }
        return subscription.getCharge();
    }
}
