package entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A sample class of how a Parking Lot class will look like in the system.
 *
 */
public class ParkingLot {
	/**
	 * _uid is the parking lot id.
	 * _uIDDispatcher is the static global ID dispatcher. //TODO: Replace with DB-given ID
	 * ParkingSpaceMatrix is a matrix which holds the parking spot's details.
	 * _height is the parking lot's height.
	 * _width is the parking lot's width.
	 * _depth is the parking lot's depth.
	 */
	private static Integer _uIDDispatcher = 1;
	private Integer _uID;
	private String _location;
	private ParkingSpace _parkingSpaceMatrix[][][] ;
	private Integer _height;
	private Integer _width;
	private Integer _depth;

	/**
	 * Class constructor which increases the parking lot ids by 1.
	 */
	public ParkingLot() {
		this._uID = _uIDDispatcher++;
	}

	/**
	 * Class constructor which initiates the parking lot with the required dimensions and details.
	 * @param h Parking lot height.
	 * @param w Parking lot width.
	 * @param d Parking lot depth.
	 * @param location Parking lot location name.
	 */
	public ParkingLot(Integer h, Integer w, Integer d, String location){
		this._uID = _uIDDispatcher++;
		this._location = location;
		this._height = h;
		this._width = w;
		this._depth = d;
		this._parkingSpaceMatrix = new ParkingSpace[h][w][d];
	}

	/**
	 * Setters and getters.
	 */
	public Integer getUID() {
		return _uID;
	}

	public void setUID(Integer uID) {
		this._uID = uID;
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String location) {
		this._location = location;
	}

	public Integer getHeight() {
		return _height;
	}

	public void setHeight(Integer height) {
		this._height = height;
	}

	public Integer getWidth() {
		return _width;
	}

	public void setWidth(Integer width) {
		this._width = width;
	}

	public Integer getDepth() {
		return _depth;
	}

	public void setDepth(Integer depth) {
		this._depth = depth;
	}

	public static Integer getUIDDispatcher() {
		return _uIDDispatcher;
	}

	public static void setUIDDispatcher(Integer UIDDispatcher) {
		ParkingLot._uIDDispatcher = UIDDispatcher;
	}

	public ParkingSpace[][][] getParkingSpaceMatrix() {
		return _parkingSpaceMatrix;
	}

	public void setParkingSpaceMatrix(ParkingSpace[][][] parkingSpaceMatrix) {
		this._parkingSpaceMatrix = parkingSpaceMatrix;
	}

	@Override
	public String toString() {
		return String.format("%d. %s", this._uID, this._location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParkingLot that = (ParkingLot) o;
		return Objects.equals(_uID, that._uID) &&
				Objects.equals(_location, that._location) &&
				Objects.equals(_height, that._height) &&
				Objects.equals(_width, that._width) &&
				Objects.equals(_depth, that._depth) &&
				Arrays.equals(_parkingSpaceMatrix, that._parkingSpaceMatrix);
	}
}
