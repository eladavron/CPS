package entity;

import java.util.HashMap;
import java.util.Map;

/**
 * A sample class of how a Parking Lot class will look like in the system.
 *
 */
public class ParkingLot {
	/**
	 * _uid is the parking lot id.
	 * ParkingSpaceMatrix is a map (matrix) which holds the parking spot's details
	 */
	private long _uid = 0;
	public static Map<ParkingSpace,Long> ParkingSpaceMatrix = new HashMap<ParkingSpace,Long>();
	
	/**
	 * Class constructor which increases the parking lot ids by 1.
	 */
	public ParkingLot() {
		this._uid++;
	}
	
	/**
	 * Get the parking lot id.
	 * @return Parking lot id.
	 */
	public long getUID() {
		return _uid;
	}
	
	/**
	 * Set the parking lot id.
	 * @param _uid Parking lot id.
	 */
	public void setUID(long _uid) {
		this._uid = _uid;
	}
	
	
}
