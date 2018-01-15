package entity;

import java.util.Objects;


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
	private Integer _depth;
	private Integer _width;
	private Integer _height;

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
		this._occupyingOrderID = null;
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

    public Integer getDepth() {
        return _depth;
    }

    public void setDepth(Integer depth) {
        this._depth = depth;
    }

    public Integer getWidth() {
        return _width;
    }

    public void setWidth(Integer width) {
        this._width = width;
    }

    public Integer getHeight() {
        return _height;
    }

    public void setHeight(Integer height) {
        this._height = height;
    }

    @Override
    public String toString() {
        return "ParkingSpace{" +
                "_occupyingOrderID=" + _occupyingOrderID +
                ", _status=" + _status +
                ", _depth=" + _depth +
                ", _width=" + _width +
                ", _height=" + _height +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSpace)) return false;
        ParkingSpace that = (ParkingSpace) o;
        return Objects.equals(_occupyingOrderID, that._occupyingOrderID) &&
                _status == that._status &&
                Objects.equals(_depth, that._depth) &&
                Objects.equals(_width, that._width) &&
                Objects.equals(_height, that._height);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_occupyingOrderID, _status, _depth, _width, _height);
    }
}
