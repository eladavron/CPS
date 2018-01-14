package controller;

import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.Map;

import static controller.Controllers.dbController;
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
        // get all subscriptions from DB
        System.out.print("\tLoading subscriptions...");
        this._subscriptionsList = DBController.getInstance().getAllSubscriptions();
        System.out.println("Done!");
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
     * @param userID
     * @param carID
     * @param regularExitTime
     * @param parkingLotNumber
     * @return new Subscription obj
     */
    public Subscription addRegularSubscription(Integer userID, Integer carID, String regularExitTime, Integer parkingLotNumber)
    {
        RegularSubscription newSub = new RegularSubscription(userID, carID, regularExitTime, parkingLotNumber);
        //TODO: add into DB as well
        this._subscriptionsList.put(newSub.getSubscriptionID() ,newSub);
        return newSub;
    }

    /**
     *  A new Regular Subscription
     * @param rSubs
     * @return new Subscription id
     */
    public Integer addRegularSubscription(RegularSubscription rSubs)
    {
        //RegularSubscription newSub = new RegularSubscription(userID, carID, regularExitTime, parkingLotNumber);
        //TODO: add into DB as well
        if (dbController.insertSubscription(rSubs)) {
            this._subscriptionsList.put(rSubs.getSubscriptionID() ,rSubs);
            return rSubs.getSubscriptionID();
        }
        return -1;
    }

    /**
     *  A new Full Subscriptiong
     * @param userID
     * @param carID
     * @return new Subscription obj
     */
    public Subscription addFullSubscription(Integer userID, Integer carID)
    {
        FullSubscription newSub = new FullSubscription(userID, carID);
        //TODO: add into DB as well. // ID from db
        this._subscriptionsList.put(newSub.getSubscriptionID(), newSub);
        return newSub;
    }

    /**
     *  A new Full Subscriptiong
     * @param fSubs
     * @return new Subscription id
     */
    public Integer addFullSubscription(FullSubscription fSubs)
    {
        //FullSubscription newSub = new FullSubscription(userID, carID);
        //TODO: add into DB as well. // ID from db
        if (dbController.insertSubscription(fSubs)) {
            this._subscriptionsList.put(fSubs.getSubscriptionID(), fSubs);
            return fSubs.getSubscriptionID();
        }
        return -1;
    }


    //TODO : Add renew overload using UID
    /**
     *  Updates the expiration date of this subscription by 30 days from TODAY! (will overwrite remaining days)
     * @param subscriptionToRenew
     * @return True on success
     */
    public boolean renewSubscription(Subscription subscriptionToRenew)
    {
        if (dbController.renewSubscription(subscriptionToRenew))
        {
            subscriptionToRenew.setExpiration(addTime(subscriptionToRenew.getExpiration(), TimeUtils.Units.DAYS, 28));
            return true;
        }
        return false;
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