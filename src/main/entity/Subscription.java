package entity;

import utils.TimeUtils;

import java.util.Date;

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



    private final SubscriptionType _subscriptionType;
    private Integer _subscriptionID;
	private Integer _carID;
	private Date _expiration;
	private Integer _userID;

	public enum SubscriptionType{FULL, REGULAR, REGULAR_MULTIPLE}

	/**
	 * Class Constructor.
	 * @param carID The subscription's car id.
	 *
	 */
	public Subscription(Integer userID, Integer carID, SubscriptionType subscriptionType) {
	    this._userID =userID;
		this._carID = carID;
		this._expiration = addTime(new Date(), TimeUtils.Units.DAYS, 28);
		this._subscriptionType = subscriptionType;
	}

	/**
	 * Ctor from DB for FULL
	 * @param subscriptionId
	 * @param carID
	 * @param subscriptionType
	 */
	public Subscription(Integer subscriptionId, Integer carID,Integer userID, Date expiration, SubscriptionType subscriptionType)
	{
		this._subscriptionID = subscriptionId;
		this._carID = carID;
		this._userID = userID;
		this._expiration = expiration;
		this._subscriptionType = subscriptionType;
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
	public int getCarID() {
		return _carID;
	}

	/**
	 * Set the subscription's car id
	 * @param carID Car ID to set the subscription's car id
	 */
	public void setCarID(int carID) {
		this._carID = carID;
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
		return "Subscription " +
				"car ID=" + _carID +
				", expiration=" + _expiration +
                ", subscription Type=" + _subscriptionType;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;
        Subscription subscription = (Subscription) o;
        return (_subscriptionUIDCounter == subscription._subscriptionID) &&
        		(_carID.equals(subscription._carID)) &&
        		(_expiration == subscription._expiration);
    }
}
