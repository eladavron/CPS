package entity;

import java.util.Date;

/**
 * A sample class of how a Subscription class will look like in the system.
 *
 */
public class Subscription {
	/**
	 * Private attributes of Subscription class.
	 */
	private int _carID;
	private Date _expiration;
	
	/**
	 * Class Constructor.
	 * @param carid The subscription's car id.
	 * @param expiration The expiration of the subscription.
	 */
	public Subscription(int carid, Date expiration) {
		this._carID = carid;
		this._expiration = expiration;
	}
	
	/**
	 * Get the subscription's car id
	 * @return The subscription's car id
	 */
	public int getCarid() {
		return _carID;
	}

	/**
	 * Set the subscription's car id
	 * @param _carid Car ID to set the subscription's car id
	 */
	public void setCarid(int _carid) {
		this._carID = _carid;
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
	 * @param _expiration The expiration of the subscription.
	 */
	public void setExpiration(Date _expiration) {
		this._expiration = _expiration;
	}

	
}
