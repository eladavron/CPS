package entity;

import entity.Order;

/**
 * A sample class of how a Parking Space class will look like in the system.
 *
 */
public class ParkingSpace {
	/**
	 * The order which occupies the parking space.
	 */
	private Order _occupyingOrder;

	/**
	 * Class constructor
	 * @param occupyingOrder The order which occupies the parking space.
	 */
	public ParkingSpace(Order occupyingOrder) {
		this._occupyingOrder = occupyingOrder;
	}

	/**
	 * Get the order which occupies the parking space.
	 * @return The order which occupies the parking space.
	 */
	public Order getOccupyingorder() {
		return _occupyingOrder;
	}

	/**
	 * Set the order which occupies the parking space.
	 * @param _occupyingorder The order which occupies the parking space.
	 */
	public void setOccupyingorder(Order _occupyingorder) {
		this._occupyingOrder = _occupyingorder;
	}
	
}
