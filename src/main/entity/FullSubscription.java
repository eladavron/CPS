package entity;


import java.util.Date;

/**
 *  A simple class to notify that this Subscription is a full
 *  "Now you can park! Anytime&Anywhere"
 */

public class FullSubscription extends Subscription {

    /**
     * ctor From DB entry
     * @param subsId
     * @param carId
     * @param userId
     * @param endDate
     */
    public FullSubscription (int subsId, int carId, int userId, Date endDate){
        super( subsId,  carId,  userId, endDate, SubscriptionType.FULL);
    }

    /**
     * ctor to DB entry
     * @param carId
     * @param userId
     */
    public FullSubscription (int userId, int carId){
        super(userId, carId, SubscriptionType.FULL);
    }

    @Override
    public String toString() {
        return "Full Subscription " + super.toString();
    }
}
