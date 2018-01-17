package controller;

import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;
import utils.TimeUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.billingController;
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
     * @param userID
     * @param carsIDList
     * @param regularExitTime
     * @param parkingLotNumber
     * @return new Subscription obj
     */
    public Subscription addRegularSubscription(Integer userID, ArrayList<Integer> carsIDList, String regularExitTime, Integer parkingLotNumber)
    {
        RegularSubscription newSub = new RegularSubscription(userID, carsIDList, regularExitTime, parkingLotNumber);
        //TODO: add into DB as well
        this._subscriptionsList.put(newSub.getSubscriptionID() ,newSub);
        return newSub;
    }

    /**
     *  A new Regular Subscription
     * @param rSubs
     * @return new Subscription id
     */
    public Integer addRegularSubscription(RegularSubscription rSubs) throws SQLException
    {
        assignChargeBySubscriptionType(rSubs);
        if (dbController.insertSubscription(rSubs)) {
            this._subscriptionsList.put(rSubs.getSubscriptionID() ,rSubs);
            return rSubs.getSubscriptionID();
        }
        return -1;
    }

    /**
     *  A new Full Subscriptiong
     * @param userID
     * @param carsIDList
     * @return new Subscription obj
     */
    public Subscription addFullSubscription(Integer userID, ArrayList<Integer> carsIDList)
    {
        FullSubscription newSub = new FullSubscription(userID, carsIDList);
        assignChargeBySubscriptionType(newSub);
        //TODO: add into DB as well. // ID from db
        this._subscriptionsList.put(newSub.getSubscriptionID(), newSub);
        return newSub;
    }

    /**
     *  A new Full Subscriptiong
     * @param fSubs
     * @return new Subscription id
     */
    public Integer addFullSubscription(FullSubscription fSubs) throws SQLException
    {
        //FullSubscription newSub = new FullSubscription(userID, carID);
        //TODO: add into DB as well. // ID from db
        assignChargeBySubscriptionType(fSubs);
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
    public boolean renewSubscription(Subscription subscriptionToRenew) throws SQLException
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
            if (subscription.getCarsID().contains(carID))
                subscriptionsIDs.add(subscription.getSubscriptionID());
        }
        return subscriptionsIDs;
    }

    public void putAll(ArrayList<Object> objectsList)
    {
        for (Object obj : objectsList)
        {
            Subscription subs;
            if (obj instanceof RegularSubscription)
                subs = (RegularSubscription) obj;
            else
                subs = (FullSubscription) obj;
            this._subscriptionsList.put(subs.getSubscriptionID(), subs);
        }
    }

    public void assignChargeBySubscriptionType(Subscription subscription)
    {
        subscription.setCharge(billingController.calculateChargeForSubscription(subscription));
    }
}