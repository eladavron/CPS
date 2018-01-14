package entity;

import java.util.Date;

/**
 * This Class represents a regular subscription of a customer's car.
 */
public class RegularSubscription extends Subscription {
    private String _regularExitTime; // if applicable. else "00:00". now as *String* for simplicity
    private final Integer _parkingLotNumber;

    /**
     * Class Constructor.
     *
     * @param carID      The subscription's car id.
     * @param parkingLotNumber the Subscription is only valid for that parking lot
     * @param regularExitTime   the car can only stay until it's regular exit time
     */
    public RegularSubscription(Integer carID, String regularExitTime, Integer parkingLotNumber) {
        super(carID, SubscriptionType.REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularExitTime = regularExitTime;
    }


    /**
     * Db c'tor
     * @param subsId
     * @param carId
     * @param userId
     * @param parkingLotNumber
     * @param endDate
     * @param regularExitTime
     */
    public RegularSubscription(Integer subsId, Integer carId, Integer userId, Integer parkingLotNumber, Date endDate,
                               String regularExitTime) {
        super(subsId,  carId,  userId, endDate, SubscriptionType.REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularExitTime = (regularExitTime == null) ? "00:00" : regularExitTime; // default when not presented
    }


    public String getRegularExitTime() {
        return _regularExitTime;
    }

    public void setRegularExitTime(String exitTime){
        _regularExitTime = exitTime;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber;
    }

    @Override
    public String toString() {
        return "Regular Subscription" +
                ", regular Exit Time=" + _regularExitTime +
                ", parking Lot Number=" + _parkingLotNumber +
                "," + super.toString();
    }

}
