package entity;

public class Robot {


    /**
     * Private attributes
     */
    private Integer _parkingLot;
    private String _friendlyName;


    public Robot(Integer _parkingLot) {
        this._parkingLot = _parkingLot;
        this._friendlyName = "Friendly Robot for parking lot "+ _parkingLot;
    }

    /**
     * Getters and Setters
     */
    public Integer getParkingLot() {
        return _parkingLot;
    }

    public void setParkingLot(Integer parkingLot) {
        this._parkingLot = parkingLot;
    }

    public String getFriendlyName() {
        return _friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this._friendlyName = friendlyName;
    }

    @Override
    public String toString() {
        return getFriendlyName();
    }
}
