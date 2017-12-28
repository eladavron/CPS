package entity;

import java.util.Objects;

/**
 * A sample class to be used as a param for other classes in order to indicate,
 * which parking-lot is in play.
 */

public class ParkingLotNumber {
    private Integer _parkingLotNumber;

    /**
     *  Simple constructor
     * @param parkingLotNumber - which parking-lot is this.
     */
    public ParkingLotNumber(Integer parkingLotNumber) {
        this._parkingLotNumber = parkingLotNumber;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber;
    }

    public void setParkingLotNumber(Integer parkingLotNumber) {
        this._parkingLotNumber = parkingLotNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLotNumber)) return false;
        ParkingLotNumber that = (ParkingLotNumber) o;
        return Objects.equals(_parkingLotNumber, that._parkingLotNumber);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_parkingLotNumber);
    }

    @Override
    public String toString() {
        return "parking Lot Number=" + _parkingLotNumber;
    }
//TODO : add a link to the existing parking lot (maybe as a param)

}
