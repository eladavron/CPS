package entity;

import utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static utils.TimeUtils.addTime;

/**
 * A sample class of how a Subscription class will look like in the system.
 *
 */
public class Subscription {
	/**
	 * Private attributes of Subscription class.
	 */
    private static int _subscriptionUIDCounter = -1;



    private SubscriptionType _subscriptionType;
    private Integer _subscriptionID;
    //	private Integer _carID;
	private ArrayList<Integer> _carsIDList;
	private Date _expiration;
	private Integer _userID;
	public enum SubscriptionType{FULL, REGULAR, REGULAR_MULTIPLE}

    /**
     * Class constructor
     * @param userID User's ID
     * @param carsIDList User's cars ids list.
     * @param subscriptionType The type of the subscription.
     */
	public Subscription(Integer userID, ArrayList<Integer> carsIDList, SubscriptionType subscriptionType) {
	    this._userID =userID;
	    this._carsIDList = carsIDList;
		this._expiration = addTime(new Date(), TimeUtils.Units.DAYS, 28);
		this._subscriptionType = subscriptionType;
	}

	/**
	 * Ctor from DB for FULL
	 * @param subscriptionID
	 * @param carsIDList
	 * @param subscriptionType
	 */
	public Subscription(Integer subscriptionID, ArrayList<Integer> carsIDList,Integer userID, Date expiration, SubscriptionType subscriptionType)
	{
		this._subscriptionID = subscriptionID;
		this._carsIDList = carsIDList;
		this._userID = userID;
		this._expiration = expiration;
		this._subscriptionType = subscriptionType;
	}

	public Subscription() {
	}

	/**
	 * @return the _subscriptionID
	 */
	public Integer getSubscriptionID() {
		return _subscriptionID;
	}

	/**
	 * @param subscriptionID the _subscriptionID to set
	 */
	public void setSubscriptionID(Integer subscriptionID) {
		this._subscriptionID = subscriptionID;
	}

	/**
	 * Get the subscription's car id
	 * @return The subscription's car id
	 */
    public ArrayList<Integer> getCarsID() {
        return _carsIDList;
    }

	/**
	 * Set the subscription's cars ids list
	 * @param carsID Car ID to set the subscription's car id
	 */
    public void setCarsID(ArrayList<Integer> carsID) {
        this._carsIDList = carsID;
    }

	/**
	 * Get the expiration of the subscription.
	 * @return The expiration of the subscription.
	 */
	public Date getExpiration() {
		return _expiration;
	}

	/**
	 * Set the expiration of the subscription.
	 * @param expiration The expiration of the subscription.
	 */
	public void setExpiration(Date expiration) {
		this._expiration = expiration;
	}

	public void setSubscriptionType(SubscriptionType subscriptionType) {
		this._subscriptionType = subscriptionType;
	}

	public SubscriptionType getSubscriptionType() {
        return _subscriptionType;
    }

	public Integer getUserID() {
		return _userID;
	}

	public void setUserID(Integer userID) {
		this._userID = userID;
	}

    @Override
    public String toString() {
        String str =  "Subscription info: \n" +
                "subscription type=" + _subscriptionType +
                ", subscription ID=" + _subscriptionID +
                ", cars ids list: \n" ;
        for(Integer carID : this._carsIDList){
            str += carID + ", ";
        }
        str += ", expiration=" + _expiration +
        ", user ID=" + _userID;
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return _subscriptionType == that._subscriptionType &&
                Objects.equals(_subscriptionID, that._subscriptionID) &&
                Objects.equals(_carsIDList, that._carsIDList) &&
                Objects.equals(_expiration, that._expiration) &&
                Objects.equals(_userID, that._userID);
    }
}
