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
	 * _uIDDispatcher is the static global ID dispatcher. //TODO: Replace with DB-given ID
	 * ParkingSpaceMatrix is a map (matrix) which holds the parking spot's details
	 */
	private static Integer _uIDDispatcher = 1;
	private Integer _uID;
	private String _location;
	public static Map<ParkingSpace,Long> ParkingSpaceMatrix = new HashMap<ParkingSpace,Long>();
	
	/**
	 * Class constructor which increases the parking lot ids by 1.
	 */
	public ParkingLot() {
		this._uID = _uIDDispatcher++;
	}

	/**
	 * A constructor with a location description.
	 * @param location The name of the location of the parking lot.
	 */
	public ParkingLot(String location)
	{
		this();
		_location = location;
	}

	/**
	 * Get the parking lot id.
	 * @return Parking lot id.
	 */
	public Integer getUID() {
		return _uID;
	}

	/**
	 * Set the parking lot id.
	 * @param uID Parking lot id.
	 */
	public void setUID(Integer uID) {
		this._uID = uID;
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String location) {
		this._location = location;
	}

	@Override
    public String toString() {
        return String.format("%d. %s", this._uID, this._location);
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        ParkingLot parkingLot = (ParkingLot) o;
        return (_uID.equals(parkingLot._uID)) ;
    }
}
