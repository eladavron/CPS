package controller;

import entity.Billing;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public double calculateParkingCharge(Date entryTime, Date exitTime, Billing.priceList priceType){
        double minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(exitTime.getTime() - entryTime.getTime()));
        return (minutes * priceType.getPrice()) / 60 ;
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
}
