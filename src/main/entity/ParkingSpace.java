package entity;

import java.util.Objects;

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
	public void setOccupyingorder(Order occupyingOrder) {
		this._occupyingOrder = occupyingOrder;
	}
	
	@Override
    public String toString() {
        return String.format("Parking space of customer id. : %s with order id. : %d: ", this._occupyingOrder.getCostumerID(), this._occupyingOrder.getOrderID());
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSpace)) return false;
        ParkingSpace parkingSpace = (ParkingSpace) o;
        return Objects.equals(_occupyingOrder, parkingSpace._occupyingOrder);
    }
}
