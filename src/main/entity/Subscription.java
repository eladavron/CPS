package entity;

import java.util.Date;

import static utils.TimeUtils.addTimeToDate;

/**
 * A sample class of how a Subscription class will look like in the system.
 *
 */
public class Subscription {
	/**
	 * Private attributes of Subscription class.
	 */
    private static int _subscriptionUIDCounter = 0;

    private Integer _subscriptionID;
	private Integer _carID;
	private Date _expiration;

	@Override
	public String toString() {
		return "Subscription " +
				"car ID=" + _carID +
				", expiration=" + _expiration;
	}

	/**
	 * Class Constructor.
	 * @param carID The subscription's car id.
	 *
	 */
	public Subscription(Integer carID) {
	    this._subscriptionID = _subscriptionUIDCounter++;
		this._carID = carID;
		this._expiration = addTimeToDate(new Date(), 30);
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


}
