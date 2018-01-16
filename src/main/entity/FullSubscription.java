package entity;


import java.util.Date;
import java.util.LinkedHashMap;

import static entity.Subscription.SubscriptionType.FULL;

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
        super( subsId,  carId,  userId, endDate, FULL);
    }

    /**
     * A custom constructor for manual JSON Deserialization.
     * @param deserialize
     */
    public FullSubscription(LinkedHashMap deserialize)
    {
        super((Integer) deserialize.get("subscriptionID"),
                (Integer) deserialize.get("carID"),
                (Integer) deserialize.get("userID"),
                new Date((Long) deserialize.get("expiration")), FULL);
    }

    /**
     * ctor to DB entry
     * @param carId
     * @param userId
     */
    public FullSubscription (int userId, int carId){
        super(userId, carId, FULL);
    }

    @Override
    public String toString() {
        return "Full Subscription " + super.toString();
    }
}
