package entity;

import java.util.Date;


/**
 *  A simple class to notify that this Subscription is a full
 *  "Now you can park! Anytime&Anywhere"
 */

public class FullSubscription extends Subscription {
    /**
     * Class Constructor.
     *
     * @param carID      The subscription's car id.
     */
    public FullSubscription(int carID) {
        super(carID, SubscriptionType.FULL);
    }

    @Override
    public String toString() {
        return "Full Subscription " + super.toString();
    }
}
