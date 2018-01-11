package entity;

import java.util.ArrayList;
import java.util.Objects;

import entity.Order;

/**
 * A sample class of how a Parking Space class will look like in the system.
 *
 */
public class ParkingSpace {
	/**
	 * _occupyingOrderID is the occupying order ID which occupies the parking space.
	 * _status is the parking space status.
	 */
	private Integer _occupyingOrderID;
	private ParkingStatus _status;

	/**
	 * FREE - Free parking space and can be ordered/occupied.
	 * ORDERED - Ordered parking space and cannot be used.
	 * UNAVAILABLE - Unavailable parking space for some reason.
	 * OCCUPIED - Occupied parking space.
	 */
	public enum ParkingStatus{
		FREE,
		ORDERED,
		UNAVAILABLE,
		OCCUPIED
	}

	/**
	 * Default constructor for initiating parking lot purposes. Setting its status to FREE.
	 */
	public ParkingSpace() {
		this._occupyingOrderID = -1;
		this._status = ParkingStatus.FREE;
	}

	/**
	 * Class constructor providing it the occupying order ID of the parking space and setting its status to OCCUPIED.
	 * @param occupyingOrderID The occupying order ID which occupies the parking space.
	 */
	public ParkingSpace(Integer occupyingOrderID) {
		this._occupyingOrderID = occupyingOrderID;
		this._status = ParkingStatus.OCCUPIED;
	}

	/**
	 * Setters and getters.
	 */
	public Integer getOccupyingOrderID() {
		return _occupyingOrderID;
	}

	public void setOccupyingOrderID(Integer occupyingOrderID) {
		this._occupyingOrderID = occupyingOrderID;
	}

	public ParkingStatus getStatus() {
		return _status;
	}

	public void setStatus(ParkingStatus status) {
		this._status = status;
	}

	@Override
    public String toString() {
        return String.format("Parking space of order id. : %d: ", this._occupyingOrderID);
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParkingSpace that = (ParkingSpace) o;
		return Objects.equals(_occupyingOrderID, that._occupyingOrderID) &&
				_status == that._status;
	}

}
