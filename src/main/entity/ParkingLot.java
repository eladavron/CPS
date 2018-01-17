package entity;

import java.util.Arrays;
import java.util.Objects;

/**
 * A sample class of how a Parking Lot class will look like in the system.
 *
 */
public class ParkingLot {
	/**
	 * _parkingLotID is the parking lot id.
	 * _parkingLotManagerID Parking lot's manager ID.
	 * _location Parking lot name.
	 * ParkingSpaceMatrix is a matrix which holds the parking spot's details.
	 * _height is the parking lot's height.
	 * _width is the parking lot's width.
	 * _depth is the parking lot's depth.
	 * _widthNumOccupied Current width occupied slots indicator.
	 * _heightNumOccupied Current height occupied slots indicator.
	 * _depthNumOccupied Current depth occupied slots indicator.
	 */
	private Integer _parkingLotManagerID;
	private Integer _parkingLotID;
	private String _location;
	private ParkingSpace _parkingSpaceMatrix[][][] ;
	private Integer _height;
	private Integer _width;
	private Integer _depth;

	private Integer _widthNumOccupied = 1 ;
	private Integer _heightNumOccupied = 1 ;
	private Integer _depthNumOccupied = 1;
	private boolean _isFullState = false;


	/**
	 * Default class constructor.
	 */
	public ParkingLot() {
	}

	/**
	 * Class constructor which initiates the parking lot with the required dimensions and details.
	 * @param h Parking lot height.
	 * @param w Parking lot width.
	 * @param d Parking lot depth.
	 * @param location Parking lot location name.
	 */
	public ParkingLot(Integer h, Integer w, Integer d, String location){
		this._location = location;
		// First height,width,depth is ignored and never accessed.
		// height = rows , width = cols.
		this._height = h;
		this._width = w;
		this._depth = d;
		this._parkingSpaceMatrix = new ParkingSpace[_depth+1][_width+1][_height+1];
	}


	public ParkingLot(Integer parkingLotID, String location, Integer rows, Integer columns, Integer depth, Integer parkingLotManagerId) {
		this._location = location;
		this._parkingLotManagerID = parkingLotManagerId;
		this._height = rows;
		this._width = columns;
		this._depth = depth;
		this._parkingSpaceMatrix = new ParkingSpace[depth+1][columns+1][rows+1];
		this._parkingLotID = parkingLotID;
	}
	/**
	 * Setters and getters.
	 */
	public Integer getParkingLotID() {
		return _parkingLotID;
	}

	public void setParkingLotID(Integer uID) {
		this._parkingLotID = uID;
	}

	public boolean getIsFullState() {
		return _isFullState;
	}

	public void setIsFullState(boolean isFull) {
		this._isFullState = isFull;
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

	public ParkingSpace[][][] getParkingSpaceMatrix() {
		return _parkingSpaceMatrix;
	}

	public void setParkingSpaceMatrix(ParkingSpace[][][] parkingSpaceMatrix) {
		this._parkingSpaceMatrix = parkingSpaceMatrix;
	}

	public Integer getWidthNumOccupied() {
		return _widthNumOccupied;
	}

	public void setWidthNumOccupied(Integer widthNumOccupied) {
		this._widthNumOccupied = widthNumOccupied;
	}

	public Integer getHeightNumOccupied() {
		return _heightNumOccupied;
	}

	public void setHeightNumOccupied(Integer heightNumOccupied) {
		this._heightNumOccupied = heightNumOccupied;
	}

	public Integer getDepthNumOccupied() {
		return _depthNumOccupied;
	}

	public void setDepthNumOccupied(Integer depthNumOccupied) {
		this._depthNumOccupied = depthNumOccupied;
	}

	public Integer getParkingLotManagerID() {
		return _parkingLotManagerID;
	}

	public void setParkingLotManagerID(Integer parkingLotManagerID) {
		this._parkingLotManagerID = parkingLotManagerID;
	}

	@Override
	public String toString() {
		return String.format("%d. %s", this._parkingLotID, this._location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParkingLot that = (ParkingLot) o;
		return Objects.equals(_parkingLotManagerID, that._parkingLotManagerID) &&
				Objects.equals(_parkingLotID, that._parkingLotID) &&
				Objects.equals(_location, that._location) &&
				Arrays.equals(_parkingSpaceMatrix, that._parkingSpaceMatrix) &&
				Objects.equals(_height, that._height) &&
				Objects.equals(_width, that._width) &&
				Objects.equals(_depth, that._depth) &&
				Objects.equals(_widthNumOccupied, that._widthNumOccupied) &&
				Objects.equals(_heightNumOccupied, that._heightNumOccupied) &&
				Objects.equals(_depthNumOccupied, that._depthNumOccupied);
	}
}
