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
	private long _uID = 0;
	public static Map<ParkingSpace,Long> ParkingSpaceMatrix = new HashMap<ParkingSpace,Long>();
	
	/**
	 * Class constructor which increases the parking lot ids by 1.
	 */
	public ParkingLot() {
		this._uID++;
	}
	
	/**
	 * Get the parking lot id.
	 * @return Parking lot id.
	 */
	public long getUID() {
		return _uID;
	}
	
	/**
	 * Set the parking lot id.
	 * @param _uid Parking lot id.
	 */
	public void setUID(long uID) {
		this._uID = uID;
	}
	
	@Override
    public String toString() {
        return String.format("Parking lot No. %d", this._uID);
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        ParkingLot parkingLot = (ParkingLot) o;
        return (_uID == parkingLot._uID) ;
    }
}
