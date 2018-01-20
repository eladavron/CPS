package entity;


import java.util.ArrayList;
import java.util.Date;

import static entity.Subscription.SubscriptionType.FULL;

/**
 *  A simple class to notify that this Subscription is of type "Full"
 * "Now you can park! Anytime and Anywhere"
 */

public class FullSubscription extends Subscription {

    /**
     * ctor From DB entry
     * @param subsId
     * @param carsIDList
     * @param userId
     * @param endDate
     */
    public FullSubscription (int subsId, ArrayList<Integer> carsIDList, int userId, Date endDate){
        super( subsId,  carsIDList,  userId, endDate, FULL);
    }

    /**
     * Empty constructor for Ms. Jackson (I'm sorry, whoooo)
     */
    public FullSubscription() {
    }

    /**
     * ctor to DB entry
     * @param carsIDList
     * @param userId
     */
    public FullSubscription (int userId, ArrayList<Integer> carsIDList){
        super(userId, carsIDList, FULL);
    }

    @Override
    public String toString() {
        return "Full Subscription " + super.toString();
    }
}
