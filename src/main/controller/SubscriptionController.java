package controller;

import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static utils.TimeUtils.addTime;

/**
 * Controller responsible of making new Subscriptions (full or regular), renew old ones,
 */
public class SubscriptionController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private Map<Integer, Subscription> _subscriptionsList;

    private static SubscriptionController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private SubscriptionController() {
        this._subscriptionsList = new HashMap<>();
    }

    /**
     * The Static initializer constructs the instance at class
     * loading time; this is to simulate a more involved
     * construction process (it it were really simple, you'd just
     * use an initializer)
     */
    static {
        instance = new SubscriptionController();
    }

    /** Static 'instance' method */
    public static SubscriptionController getInstance() {
        return instance;
    }

    public Subscription getSubscription(Integer subscriptionID)
    {
        return _subscriptionsList.get(subscriptionID);
    }

    /**
     *  A new Regular Subscription
     * @param carID
     * @param regularEntryTime
     * @param regularExitTime
     * @param parkingLotNumber
     * @return
     */
    public Subscription addRegularSubscription(Integer carID, Date regularEntryTime, Date regularExitTime, Integer parkingLotNumber)
    {
        RegularSubscription newSub = new RegularSubscription(carID, regularEntryTime, regularExitTime, parkingLotNumber);
        this._subscriptionsList.put(newSub.getSubscriptionID() ,newSub);
        //TODO: add into DB as well.
        return newSub;
    }

    /**
     *  A new Full Subscription
     * @param carID
     * @return
     */
    public Subscription addFullSubscription(Integer carID)
    {
        FullSubscription newSub = new FullSubscription(carID);
        this._subscriptionsList.put(newSub.getSubscriptionID(), newSub);
        //TODO: add into DB as well.
        return newSub;
    }

    //TODO : Add renew overload using UID
    /**
     *  Updates the expiration date of this subscription by 30 days from TODAY! (will overwrite remaining days)
     * @param subscriptionToRenew
     */
    public void renewSubscription(Subscription subscriptionToRenew)
    {
        subscriptionToRenew.setExpiration(addTime(new Date(), TimeUtils.Units.DAYS, 30));
    }
    /**
     *  Since there is only 1 subscription per 1 carID and we map using subscriptionID and not carID this function will search
     *  for the an existing subscription with given carID
     * @param subscriptionList
     * @param carID
     * @return the given subscriptions IDs (in order to use with the Map class)
     */
    public ArrayList<Integer> findSubscriptionsByCarID(Map<Integer, Subscription> subscriptionList, Integer carID) {
        ArrayList<Integer> subscriptionsIDs = new ArrayList<>();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getCarID() == carID)
                subscriptionsIDs.add(subscription.getSubscriptionID());
        }
        return subscriptionsIDs;
    }
}