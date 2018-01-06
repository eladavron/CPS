package controller;

import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;

import java.util.ArrayList;
import java.util.Date;

import static utils.TimeUtils.addTimeToDate;

/**
 * Controller responsible of making new Subscriptions (full or regular), renew old ones,
 */
public class SubscriptionController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private ArrayList<Subscription> _subscriptionsList;

    private static SubscriptionController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private SubscriptionController() {
        this._subscriptionsList = new ArrayList<>();
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

    /**
     *  A new Regular Subscription
     * @param carID
     * @param expiration
     * @param regularEntryTime
     * @param regularExitTime
     * @param parkingLotNumber
     * @return
     */
    public Subscription addRegularSubscription(Integer carID, Date expiration, Date regularEntryTime, Date regularExitTime, Integer parkingLotNumber)
    {
        RegularSubscription newSub = new RegularSubscription(carID, expiration, regularEntryTime, regularExitTime, parkingLotNumber);
        this._subscriptionsList.add(newSub);
        return newSub;
    }

    /**
     *  A new Full Subscription
     * @param carID
     * @param expiration
     * @return
     */
    public Subscription addFullSubscription(Integer carID, Date expiration)
    {
        FullSubscription newSub = new FullSubscription(carID, expiration);
        this._subscriptionsList.add(newSub);
        return newSub;
    }

    //TODO : Add renew overload using UID
    /**
     *  Updates the expiration date of this subscription by 30 days from TODAY! (will overwrite remaining days)
     * @param subscriptionToRenew
     */
    public void renewSubscription(Subscription subscriptionToRenew)
    {
        subscriptionToRenew.setExpiration(addTimeToDate(new Date(), 30));
    }

}
